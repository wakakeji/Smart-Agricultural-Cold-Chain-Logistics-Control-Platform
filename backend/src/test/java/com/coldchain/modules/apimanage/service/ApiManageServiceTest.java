package com.coldchain.modules.apimanage.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiManageServiceTest {

    private final ApiManageService service = new ApiManageService();

    @Test
    void catalogHasTwelveApis() {
        assertEquals(12, service.listApis().size());
    }

    @Test
    void generateKey() {
        Map<String, Object> key = service.generateKey("demo");
        assertTrue(String.valueOf(key.get("apiKey")).startsWith("ck_"));
        assertEquals("demo", key.get("name"));
    }
}
