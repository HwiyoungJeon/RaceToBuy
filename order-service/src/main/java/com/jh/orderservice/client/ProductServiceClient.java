package com.jh.orderservice.client;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.client.dto.EventInfoDTO;
import com.jh.orderservice.client.dto.ProductResponse;
import com.jh.orderservice.client.dto.StockUpdateRequest;
import com.jh.orderservice.config.FeignClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "product-service",
    configuration = FeignClientConfig.class
)
public interface ProductServiceClient {

    Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    @GetMapping("/products/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/check-stock")
    Boolean checkStock(
        @PathVariable("productId") Long productId,
        @RequestParam(value = "quantity", required = true) Integer quantity
    );

    @PostMapping("/products/stock/decrease")
    @Retryable(maxAttempts = 3)
    ApiResponse<Boolean> decreaseStock(@RequestBody StockUpdateRequest request);

    @PostMapping("/products/stock/increase")
    ApiResponse<Boolean> increaseStock(@RequestBody StockUpdateRequest request);

    @GetMapping("/products/events/{eventId}")
    ApiResponse<EventInfoDTO> getEventInfo(
        @PathVariable("eventId") Long eventId,
        @RequestParam("productId") Long productId
    );
}