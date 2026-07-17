package com.coldchain.modules.route.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

/**
 * 路线规划 RP-001
 * 说明：验收环境未接真实高德/百度驾车路径 API，采用「起终点 + 策略」规则引擎生成多方案，
 * 更换起终点会改变里程/时长/途经点/路况；接入正式路径规划服务后可替换本实现。
 */
@Service
public class RouteService {

    private static final Map<String, double[]> CITY_COORDS = Map.ofEntries(
            Map.entry("南宁", new double[]{108.37, 22.82}),
            Map.entry("柳州", new double[]{109.41, 24.33}),
            Map.entry("桂林", new double[]{110.29, 25.27}),
            Map.entry("北海", new double[]{109.12, 21.48}),
            Map.entry("玉林", new double[]{110.15, 22.63}),
            Map.entry("百色", new double[]{106.62, 23.90}),
            Map.entry("钦州", new double[]{108.62, 21.97}),
            Map.entry("梧州", new double[]{111.28, 23.48}),
            Map.entry("贵港", new double[]{109.60, 23.11}),
            Map.entry("广州", new double[]{113.26, 23.13})
    );

    public Map<String, Object> plan(String origin, String destination) {
        String from = (origin == null || origin.isBlank()) ? "南宁" : origin.trim();
        String to = (destination == null || destination.isBlank()) ? "广州" : destination.trim();
        int seed = hash(from + "|" + to);
        double dist = estimateDistanceKm(from, to);
        List<String> viaFast = viaPoints(from, to, seed, 0);
        List<String> viaBalanced = viaPoints(from, to, seed, 1);
        List<String> viaQuality = viaPoints(from, to, seed, 2);

        List<Map<String, Object>> routes = new ArrayList<>();
        routes.add(route("A", "高速优先", from, to, dist * 0.92, dist / 85.0, 94.0 + (seed % 3), 86 + seed % 5, 0.9 + (seed % 10) * 0.01, viaFast));
        routes.add(route("B", "时效均衡", from, to, dist * 1.05, dist / 78.0, 96.0 + (seed % 2), 90 + seed % 6, 0.75 + (seed % 8) * 0.01, viaBalanced));
        routes.add(route("C", "品质优先", from, to, dist * 1.12, dist / 72.0, 97.5 + (seed % 2) * 0.5, 93 + seed % 5, 0.55 + (seed % 7) * 0.02, viaQuality));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("origin", from);
        result.put("destination", to);
        result.put("recommend", "B");
        result.put("mode", "RULE_ENGINE");
        result.put("note", "当前为规则引擎模拟方案（未接第三方实时导航）。更换起终点会重算里程、时长、途经点与路况。");
        result.put("routes", routes);
        result.put("traffic", trafficFor(from, to, seed));
        return result;
    }

    public List<Map<String, Object>> traffic() {
        return trafficFor("南宁", "广州", 1);
    }

    private List<Map<String, Object>> trafficFor(String from, String to, int seed) {
        String corridor = from + "→" + to;
        String[] levels = {"畅通", "轻度拥堵", "中度拥堵"};
        return List.of(
                Map.of("road", "干线高速（" + corridor + "）", "level", levels[seed % 3], "speed", 90 - (seed % 5) * 8),
                Map.of("road", "联络线", "level", levels[(seed + 1) % 3], "speed", 70 - (seed % 4) * 6),
                Map.of("road", "城区道路", "level", levels[(seed + 2) % 3], "speed", 40 - (seed % 3) * 5)
        );
    }

    private Map<String, Object> route(String id, String name, String from, String to,
                                      double distanceKm, double hours, double qualityRetention,
                                      double score, double risk, List<String> waypoints) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("routeId", id);
        m.put("name", name);
        m.put("distanceKm", Math.round(distanceKm));
        m.put("durationHours", Math.round(hours * 10) / 10.0);
        m.put("qualityRetention", Math.round(qualityRetention * 10) / 10.0);
        m.put("score", Math.min(99, Math.round(score)));
        m.put("congestionRisk", Math.round(risk * 100) / 100.0);
        m.put("fuelCost", Math.round(distanceKm * 1.8));
        m.put("pathPoints", pathPoints(from, to, id));
        m.put("waypoints", waypoints);
        return m;
    }

    private List<String> viaPoints(String from, String to, int seed, int strategy) {
        List<String> hubs = new ArrayList<>(List.of("贵港", "梧州", "玉林", "钦州", "柳州"));
        hubs.removeIf(h -> h.equals(from) || h.equals(to));
        Collections.rotate(hubs, seed + strategy);
        List<String> path = new ArrayList<>();
        path.add(from);
        if (!hubs.isEmpty()) {
            path.add(hubs.get(0));
            if (strategy > 0 && hubs.size() > 1) {
                path.add(hubs.get(1));
            }
        }
        path.add(to);
        return path;
    }

    private double estimateDistanceKm(String from, String to) {
        double[] a = CITY_COORDS.getOrDefault(from, new double[]{108.3, 22.8});
        double[] b = CITY_COORDS.getOrDefault(to, new double[]{113.2, 23.1});
        double dx = (b[0] - a[0]) * 95;
        double dy = (b[1] - a[1]) * 110;
        double straight = Math.sqrt(dx * dx + dy * dy);
        return Math.max(80, straight * 1.35);
    }

    private List<Map<String, Double>> pathPoints(String from, String to, String id) {
        double[] a = CITY_COORDS.getOrDefault(from, new double[]{108.3, 22.8});
        double[] b = CITY_COORDS.getOrDefault(to, new double[]{113.2, 23.1});
        double bend = (id.charAt(0) - 'A') * 0.08;
        List<Map<String, Double>> pts = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            Map<String, Double> p = new LinkedHashMap<>();
            p.put("lng", Math.round((a[0] + (b[0] - a[0]) * t + Math.sin(t * Math.PI) * bend) * 1e6) / 1e6);
            p.put("lat", Math.round((a[1] + (b[1] - a[1]) * t + Math.cos(t * Math.PI) * bend * 0.5) * 1e6) / 1e6);
            pts.add(p);
        }
        return pts;
    }

    private int hash(String s) {
        CRC32 crc = new CRC32();
        crc.update(s.getBytes(StandardCharsets.UTF_8));
        return (int) (Math.abs(crc.getValue()) % 100);
    }
}
