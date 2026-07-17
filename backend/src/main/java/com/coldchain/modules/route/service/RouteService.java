package com.coldchain.modules.route.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 路线规划 RP-001（Mock 多方案）
 */
@Service
public class RouteService {

    public Map<String, Object> plan(String origin, String destination) {
        String from = (origin == null || origin.isBlank()) ? "南宁" : origin;
        String to = (destination == null || destination.isBlank()) ? "广州" : destination;
        List<Map<String, Object>> routes = new ArrayList<>();
        routes.add(route("A", "高速优先", from, to, 620, 7.5, 95.0, 88.0, 1.0));
        routes.add(route("B", "时效均衡", from, to, 680, 8.2, 97.0, 92.0, 0.85));
        routes.add(route("C", "品质优先", from, to, 710, 9.0, 98.5, 96.0, 0.7));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("origin", from);
        result.put("destination", to);
        result.put("recommend", "B");
        result.put("routes", routes);
        result.put("traffic", traffic());
        return result;
    }

    public List<Map<String, Object>> traffic() {
        return List.of(
                Map.of("road", "G80 广昆高速", "level", "轻度拥堵", "speed", 68),
                Map.of("road", "G7211 南友高速", "level", "畅通", "speed", 95),
                Map.of("road", "城市绕城", "level", "中度拥堵", "speed", 35)
        );
    }

    private Map<String, Object> route(String id, String name, String from, String to,
                                      int distanceKm, double hours, double qualityRetention,
                                      double score, double risk) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("routeId", id);
        m.put("name", name);
        m.put("distanceKm", distanceKm);
        m.put("durationHours", hours);
        m.put("qualityRetention", qualityRetention);
        m.put("score", score);
        m.put("congestionRisk", risk);
        m.put("fuelCost", Math.round(distanceKm * 1.8));
        m.put("pathPoints", pathPoints(from, to, id));
        m.put("waypoints", List.of(from, "贵港", "梧州", to));
        return m;
    }

    private List<Map<String, Double>> pathPoints(String from, String to, String id) {
        double baseLng = 108.3 + id.charAt(0) * 0.01;
        double baseLat = 22.8 + id.charAt(0) * 0.005;
        List<Map<String, Double>> pts = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            Map<String, Double> p = new LinkedHashMap<>();
            p.put("lng", Math.round((baseLng + i * 0.12) * 1e6) / 1e6);
            p.put("lat", Math.round((baseLat + i * 0.05) * 1e6) / 1e6);
            pts.add(p);
        }
        return pts;
    }
}
