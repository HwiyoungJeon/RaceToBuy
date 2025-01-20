package com.jh.productservice.service.product;


import com.jh.common.constant.ErrorCode;
import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.exception.BusinessException;
import com.jh.productservice.domain.product.dto.EventInfoDTO;
import com.jh.productservice.domain.product.dto.ProductWithEventDTO;
import com.jh.productservice.domain.product.entity.Event;
import com.jh.productservice.domain.product.entity.EventProduct;
import com.jh.productservice.domain.product.entity.Product;
import com.jh.productservice.domain.product.repository.EventProductRepository;
import com.jh.productservice.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RFuture;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EventProductRepository eventProductRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STOCK_KEY_PREFIX = "stock:product:";

    private final RedissonClient redissonClient;

    /**
     * 커서 기반 페이지네이션을 사용하여 상품 목록을 조회합니다.
     *
     * @param cursor 현재 페이지를 결정하는 커서 값 (0 이하일 경우 첫 페이지로 처리)
     * @param size   한 페이지에 표시할 상품 수
     * @return PagedResponseDTO<ProductWithEventDTO> 상품 목록 및 다음 커서 정보
     * <p>
     * 동작 방식:
     * - 전체 상품 데이터를 정렬하여 조회합니다.
     * - 커서 값과 페이지 크기를 기반으로 데이터를 슬라이싱합니다.
     * - 반환 데이터에 다음 커서를 포함하여 클라이언트가 다음 페이지를 요청할 수 있도록 합니다.
     */
    @Override
    public PagedResponseDTO<ProductWithEventDTO> getProductsWithCursor(Long cursor, int size) {
        // 커서가 0일 경우 1로 설정
        if (cursor == null || cursor <= 0) {
            cursor = 1L;
        }

        // 현재 커서를 계산하여 시작 위치 결정
        int startIndex = (int) ((cursor - 1) * size);

        // 데이터 전체를 정렬하여 가져오기
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "productId"));

        // 현재 커서에 맞는 데이터 필터링 (인덱스 기준 슬라이싱)
        List<Product> paginatedProducts = products.stream()
                .skip(startIndex)  // 커서 기반으로 시작점 이동
                .limit(size)       // 사이즈만큼 데이터 가져오기
                .collect(Collectors.toList());

        // 상품 목록 -> DTO 변환
        List<ProductWithEventDTO> productDtos = paginatedProducts.stream()
                .map(this::mapProductToDto)
                .map(productWithEventDTO -> {

                    Integer stockInRedis = getStockFromRedis(productWithEventDTO.productId());

                    // Redis에 재고가 없으면 DB에서 조회하고 Redis에 저장
                    if (stockInRedis == null) {
                        stockInRedis = productWithEventDTO.stockQuantity();  // DB에서 가져온 재고 사용
                        saveStockToRedis(productWithEventDTO.productId(), stockInRedis);  // Redis에 재고 저장
                    }

//                    int stockQuantity = (stockInRedis != null) ? stockInRedis : productWithEventDTO.stockQuantity();
                    System.out.println("####rdis : = " + stockInRedis);
                    System.out.println("####ridasf : = " + productWithEventDTO.productId());
                    // 종료된 이벤트를 제외
                    List<EventInfoDTO> activeEvents = productWithEventDTO.events().stream()
                            .filter(eventInfoDTO -> eventInfoDTO.endDate().isAfter(LocalDateTime.now())) // 종료된 이벤트 제외
                            .collect(Collectors.toList());
                    return new ProductWithEventDTO(
                            productWithEventDTO.productId(),
                            productWithEventDTO.productName(),
                            productWithEventDTO.price(),
//                            productWithEventDTO.stockQuantity(),
                            stockInRedis,
                            activeEvents
                    );
                })
                .collect(Collectors.toList());

        // 다음 커서 계산
        boolean hasMore = paginatedProducts.size() > startIndex + size;

        return new PagedResponseDTO<>(cursor, size, productDtos, hasMore);
    }

    /**
     * 특정 상품의 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 고유 ID
     * @return ProductWithEventDTO 상품의 상세 정보와 관련 이벤트 정보
     * <p>
     * 동작 방식:
     * - 상품 ID로 상품 데이터를 조회합니다.
     * - 상품이 존재하지 않을 경우 적절한 예외를 반환합니다.
     */
    @Override
    public ProductWithEventDTO getProductById(Long productId) {
        // 특정 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 상품 -> DTO 변환
        return mapProductToDto(product);
    }


    /**
     * 공통 매핑 로직: Product -> ProductWithEventDTO
     *
     * @param product 변환할 Product 엔티티
     * @return ProductWithEventDTO 변환된 상품 DTO
     * <p>
     * 동작 방식:
     * - 상품 엔티티를 DTO로 변환합니다.
     * - 관련된 이벤트 목록을 이벤트 DTO로 변환하여 포함합니다.
     */
    private ProductWithEventDTO mapProductToDto(Product product) {
        List<EventInfoDTO> events = product.getEventProducts().stream()
                .map(this::mapEventProductToEventInfoDto) // 이벤트 매핑 메서드 사용
                .collect(Collectors.toList());

        return new ProductWithEventDTO(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getStockQuantity(),
                events
        );
    }

    /**
     * 공통 매핑 로직: EventProduct -> EventInfoDTO
     *
     * @param eventProduct 변환할 EventProduct 엔티티
     * @return EventInfoDTO 변환된 이벤트 DTO
     * <p>
     * 동작 방식:
     * - 이벤트와 상품 데이터를 사용하여 할인 가격과 차이를 계산합니다.
     * - 이벤트 정보를 DTO로 변환하여 반환합니다.
     */
    private EventInfoDTO mapEventProductToEventInfoDto(EventProduct eventProduct) {
        Event event = eventProduct.getEvent();
        Product product = eventProduct.getProduct();

        BigDecimal discountPrice = product.getPrice()
                .multiply(BigDecimal.valueOf(1 - (eventProduct.getDiscountRate().doubleValue() / 100)));
        BigDecimal priceDifference = product.getPrice().subtract(discountPrice);

        return new EventInfoDTO(
                event.getEventId(),
                event.getEventName(),
                eventProduct.getDiscountRate().doubleValue(),
                discountPrice,
                priceDifference,
                event.getStartDate(),  // startDate
                event.getEndDate()
        );
    }


    // [수정 전]
// - 원래는 DECRBY 등의 로직이 섞여 있었거나, 재고가 충분하면 차감까지 해버렸을 수도 있음
//   혹은 checkStock 자체가 없을 수도 있었음

    // [수정 후]
    public boolean checkStock(Long productId, Integer quantity) {
        if (productId == null || quantity == null) {
            System.err.println("[checkStock] Invalid arguments: productId or quantity is null.");
            return false;
        }

        String stockKey = STOCK_KEY_PREFIX + productId;

        // [수정] 차감 없이 재고 충분 여부만 판단하는 Lua 스크립트
        String luaScript = """
                    local stockKey = KEYS[1]
                    local quantity = tonumber(ARGV[1])
                    if quantity == nil then
                        return -1
                    end
                    local currentStock = redis.call('GET', stockKey)
                    if not currentStock then
                        return -1  -- 키가 없으면 부족으로 간주
                    end
                    local stockVal = tonumber(currentStock)
                    if not stockVal then
                        return -1  -- 숫자 아님
                    end
                    if stockVal < quantity then
                        return -1  -- 부족
                    end
                    return 1      -- 충분
                """;

        // [기존] Redisson evalAsync 호출부는 같지만, 스크립트만 변경되었음
        RScript rScript = redissonClient.getScript();
        RFuture<Long> resultFuture = rScript.evalAsync(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(stockKey),
                String.valueOf(quantity)
        );

        try {
            Long result = resultFuture.get();
            System.out.println("[checkStock] Script result = " + result);

            // [수정] result == 1이면 충분, 나머지는 false
            return (result != null && result > 0);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
            return false;
        }
    }


    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        // Redis와 DB에서 차감 처리
        boolean isStockUpdated = decreaseStockWithConcurrencyControl(productId, quantity);

        // 차감이 안 된 경우 에러 처리
        if (!isStockUpdated) {
            throw new BusinessException(ErrorCode.STOCK_UPDATE_FAILED);
        }
    }


    @Override
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {

        Integer stockInRedis = getStockFromRedis(productId);
        if (stockInRedis == null) {
            stockInRedis = 0; // Redis에 재고가 없으면 0으로 설정
        }

        // Redis에서 재고 증가
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, stockInRedis + quantity);


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.updateStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        // Redis에 다시 업데이트
        saveStockToRedis(productId, product.getStockQuantity());
    }

    @Override
    public EventInfoDTO getEventInfo(Long eventId, Long productId) {
        EventProduct eventProduct = eventProductRepository.findByEvent_EventIdAndProduct_ProductId(eventId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        return mapEventProductToEventInfoDto(eventProduct);
    }

    private void saveStockToRedis(Long productId, int quantity) {
        String redisKey = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().set(redisKey, quantity);
    }

    // [추가/수정] 새로운 or 변경된 메서드
    @Transactional
    public boolean decreaseStockWithConcurrencyControl(Long productId, int quantity) {
        String redisKey = STOCK_KEY_PREFIX + productId;

        // [수정] Lua 스크립트: 재고를 실제로 차감(DECRBY)
        String luaScript = """
                    -- 수량 파라미터(문자열) 디버깅
                    local quantityStr = ARGV[1]
                    redis.call('SET', 'debug_quantityArg', quantityStr)  -- 디버깅: 원본 ARGV[1] 저장
                
                    local quantity = tonumber(quantityStr)
                    if not quantity then
                        -- 변환 실패 시
                        redis.call('SET', 'debug_quantityError', 'Failed to convert ARGV[1] to number.')
                        return -2 -- 수량 변환 실패
                    end
                
                    local stockKey = KEYS[1]
                    local currentStockStr = redis.call('GET', stockKey)
                    if not currentStockStr then
                        -- 키가 없으면 0으로 초기화
                        redis.call('SET', stockKey, 0)
                        currentStockStr = "0"
                    end
                
                    -- 재고 문자열 디버깅
                    redis.call('SET', 'debug_currentStockStr', currentStockStr)
                
                    local currentStock = tonumber(currentStockStr)
                    if not currentStock then
                        redis.call('SET', 'debug_stockError', 'Current stock is not a valid number.')
                        return -3 -- 재고 값이 숫자 아님
                    end
                
                    if currentStock < quantity then
                        redis.call('SET', 'debug_stockError', 'Not enough stock!')
                        return -1 -- 재고 부족
                    end
                
                    -- 충분하면 차감
                    local newStock = redis.call('DECRBY', stockKey, quantity)
                    redis.call('SET', 'debug_newStock', newStock)  -- 차감 후 재고 값 디버깅
                    return newStock
                """;

        String argStr = String.valueOf(quantity);
        System.out.println(">>> Final ARGV[1] = " + argStr);  // 여기서 직접 콘솔에 어떤 값이 찍히는지 확인

        RScript rScript = redissonClient.getScript();
        RFuture<Long> redisFuture = rScript.evalAsync(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(redisKey),
                argStr
        );
        try {
            Long redisResult = redisFuture.get();
            System.out.println("[decreaseStockWithConcurrencyControl] Redis result = " + redisResult);

            if (redisResult != null && redisResult >= 0) {
                // [수정] Redis 차감 성공 시 → DB 차감
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
                if (product.getStockQuantity() < quantity) {
                    throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
                }

                product.reduceStock(quantity);
                productRepository.save(product);

                return true;
            } else {
                // -1(재고 부족), -2, -3, null 등
                System.err.println("[decreaseStock] Redis script returned " + redisResult);
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
            return false;
        }
    }


    /**
     * Redis에서 재고를 가져옵니다.
     *
     * @param productId 상품 ID
     * @return 현재 재고 수량
     */
    private Integer getStockFromRedis(Long productId) {
        String redisKey = STOCK_KEY_PREFIX + productId;
        return (Integer) redisTemplate.opsForValue().get(redisKey);
    }

}