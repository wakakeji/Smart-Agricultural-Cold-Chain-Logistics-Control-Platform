package com.coldchain.security;

import com.coldchain.config.ColdChainProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具单元测试
 */
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        ColdChainProperties props = new ColdChainProperties();
        props.getJwt().setSecret("unit-test-secret-key-32bytes-min!!");
        props.getJwt().setExpireSeconds(3600);
        props.getJwt().setRememberExpireSeconds(7200);
        provider = new JwtTokenProvider(props);
    }

    @Test
    void createAndParseToken() {
        String token = provider.createToken(1001L, "admin", "admin", List.of("*:*:*"), false);
        assertNotNull(token);
        Claims claims = provider.parse(token);
        assertEquals("1001", claims.getSubject());
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("admin", claims.get("role", String.class));
        assertEquals(3600, provider.expireSeconds(false));
        assertEquals(7200, provider.expireSeconds(true));
    }
}
