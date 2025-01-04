package com.jh.orderservice.client;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.client.dto.ProductResponse;
import com.jh.orderservice.client.dto.StockUpdateRequest;
import com.jh.orderservice.client.dto.EventInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/products/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable Long productId);

    @GetMapping("/products/{productId}/check-stock")
    ApiResponse<Boolean> checkStock(@PathVariable Long productId, @RequestParam Integer quantity);

    @PostMapping("/products/stock/decrease")
    ApiResponse<?> decreaseStock(@RequestBody StockUpdateRequest request);
    
    @PostMapping("/products/stock/increase")
    ApiResponse<?> increaseStock(@RequestBody StockUpdateRequest request);
    
    @GetMapping("/products/events/{eventId}")
    ApiResponse<EventInfoDTO> getEventInfo(@PathVariable Long eventId, @RequestParam Long productId);
} 