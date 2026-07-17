package com.coldchain.modules.dataquality.service;

import com.coldchain.modules.facility.entity.SensorDevice;
import com.coldchain.modules.facility.mapper.SensorDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据质量 EM-005（基于传感器状态聚合 + Mock 流指标）
 * 传感器 status：1=在线 0=离线
 */
@Service
@RequiredArgsConstructor
public class DataQualityService {

    private final SensorDeviceMapper sensorDeviceMapper;

    public Map<String, Object> overview() {
        List<SensorDevice> sensors = sensorDeviceMapper.selectList(null);
        long total = sensors.size();
        long online = sensors.stream().filter(s -> s.getStatus() != null && s.getStatus() == 1).count();
        double onlineRate = total == 0 ? 0 : Math.round(online * 1000.0 / total) / 10.0;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double completeness = Math.min(99.9, onlineRate + 1.5);
        double accuracy = 98.2 + r.nextDouble();
        double timeliness = 97.5 + r.nextDouble();
        double latencyMs = 80 + r.nextDouble() * 40;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sensorTotal", total);
        m.put("sensorOnline", online);
        m.put("onlineRate", onlineRate);
        m.put("completeness", Math.round(completeness * 10) / 10.0);
        m.put("accuracy", Math.round(accuracy * 10) / 10.0);
        m.put("timeliness", Math.round(timeliness * 10) / 10.0);
        m.put("avgLatencyMs", Math.round(latencyMs));
        m.put("p99LatencyMs", Math.round(latencyMs * 2.1));
        m.put("score", Math.round((completeness + accuracy + timeliness) / 3 * 10) / 10.0);
        m.put("trend", buildTrend());
        m.put("issues", List.of(
                Map.of("type", "DELAY", "count", 3, "desc", "部分车辆传感器上报延迟 > 30s"),
                Map.of("type", "MISSING", "count", 1, "desc", "北海冷库某通道湿度点位缺失"),
                Map.of("type", "OUTLIER", "count", 2, "desc", "温度尖峰已自动过滤")
        ));
        return m;
    }

    private List<Map<String, Object>> buildTrend() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("hour", String.format("%02d:00", (24 - i) % 24));
            p.put("completeness", 96 + (11 - i) % 3);
            p.put("latencyMs", 90 + i * 2);
            list.add(p);
        }
        return list;
    }
}
