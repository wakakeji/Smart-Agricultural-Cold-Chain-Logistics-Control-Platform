package com.coldchain.security;

import com.coldchain.config.ColdChainProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 签发与解析
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final ColdChainProperties properties;

    public String createToken(Long userId, String username, String role, List<String> permissions, boolean rememberMe) {
        long ttl = rememberMe ? properties.getJwt().getRememberExpireSeconds()
                : properties.getJwt().getExpireSeconds();
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long expireSeconds(boolean rememberMe) {
        return rememberMe ? properties.getJwt().getRememberExpireSeconds()
                : properties.getJwt().getExpireSeconds();
    }

    private SecretKey secretKey() {
        byte[] keyBytes = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : pad(keyBytes));
    }

    private byte[] pad(byte[] src) {
        byte[] out = new byte[32];
        System.arraycopy(src, 0, out, 0, Math.min(src.length, 32));
        return out;
    }
}
