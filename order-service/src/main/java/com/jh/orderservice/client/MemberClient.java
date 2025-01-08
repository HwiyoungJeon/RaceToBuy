package com.jh.orderservice.client;

import com.jh.common.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.jh.orderservice.config.FeignClientConfig;

@FeignClient(
    name = "USER-SERVICE",
    path = "/users",
    configuration = FeignClientConfig.class
)
public interface MemberClient {

    @GetMapping("/{id}")
    ApiResponse<MemberResponse> getMemberById(@PathVariable("id") Long id);
}
