package com.jh.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 일관된 직렬화 전략 사용
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // key는 String으로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // value는 JSON으로 직렬화
        redisTemplate.setValueSerializer(jsonSerializer);

        // hashKey와 hashValue 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jsonSerializer);

        return redisTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // LocalDateTime 처리
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO 8601 형식
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

        // null 값이나 빈 값 직렬화하지 않도록 설정
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 빈 컬렉션을 직렬화하지 않도록 설정 (빈 배열도 포함)
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 타입 정보 포함
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

        return objectMapper;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}