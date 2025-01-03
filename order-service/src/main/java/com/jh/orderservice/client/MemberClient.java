package com.jh.orderservice.client;

import com.jh.common.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service")
public interface MemberClient {

    @GetMapping("/users/{id}")
    ApiResponse<MemberResponse> getMemberById(@PathVariable("id") Long id);
}
