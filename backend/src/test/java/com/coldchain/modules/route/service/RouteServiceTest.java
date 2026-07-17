package com.coldchain.modules.route.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouteServiceTest {

    private final RouteService routeService = new RouteService();

    @Test
    void planReturnsThreeRoutes() {
        Map<String, Object> plan = routeService.plan("南宁", "广州");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> routes = (List<Map<String, Object>>) plan.get("routes");
        assertEquals(3, routes.size());
        assertEquals("B", plan.get("recommend"));
    }
}
