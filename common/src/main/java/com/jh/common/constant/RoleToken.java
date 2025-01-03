package com.jh.common.constant;


import com.jh.common.exception.JwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RoleToken {
    VIP("vip"),
    USER("user"),
    ADMIN("admin");

    private final String name;

    public static RoleToken findByName(String name) {
        return Arrays.stream(RoleToken.values())
                .filter(role -> role.name.equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new JwtException(ErrorCode.INVALID_ROLE));
    }
}
