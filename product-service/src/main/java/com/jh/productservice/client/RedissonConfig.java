package com.jh.productservice.client;


import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setPassword(null);

        // **StringCodec** 추가
        config.setCodec(new StringCodec());

        return Redisson.create(config);
    }

    // 분산 잠금 (예시)
    public RLock getLock(RedissonClient redissonClient, Long productId) {
        String lockKey = "stockLock:" + productId;
        return redissonClient.getLock(lockKey);
    }
}