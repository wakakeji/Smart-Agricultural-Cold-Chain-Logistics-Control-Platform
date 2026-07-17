package com.coldchain.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token 会话服务（Redis 存储）
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String KEY_PREFIX = "token:";

    private final StringRedisTemplate redisTemplate;

    public void store(Long userId, String token, long expireSeconds) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, token, Duration.ofSeconds(expireSeconds));
    }

    public boolean isTokenValid(Long userId, String token) {
        String cached = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return token != null && token.equals(cached);
    }

    public void remove(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
