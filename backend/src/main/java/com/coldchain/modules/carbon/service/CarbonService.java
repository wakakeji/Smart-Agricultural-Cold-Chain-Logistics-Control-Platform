package com.coldchain.modules.carbon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.carbon.entity.CarbonEmission;
import com.coldchain.modules.carbon.mapper.CarbonEmissionMapper;
import com.coldchain.modules.dashboard.service.DashboardSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarbonService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final CarbonEmissionMapper carbonEmissionMapper;
    private final DashboardSeedService dashboardSeedService;

    public Map<String, Object> overview() {
        dashboardSeedService.ensureSeedData();
        List<CarbonEmission> all = carbonEmissionMapper.selectList(null);
        double total = all.stream().mapToDouble(c -> c.getEmissionValue() == null ? 0 : c.getEmissionValue()).sum();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalEmission", Math.round(total * 10) / 10.0);
        m.put("unit", "kg CO2e");
        m.put("method", "IPCC");
        m.put("period", all.isEmpty() ? "2026-Q2" : all.get(0).getPeriod());
        m.put("bySource", bySource(all));
        m.put("recordCount", all.size());
        return m;
    }

    public List<Map<String, Object>> trend() {
        dashboardSeedService.ensureSeedData();
        // 演示：按来源展开为 7 日趋势
        List<CarbonEmission> all = carbonEmissionMapper.selectList(null);
        double base = all.stream().mapToDouble(c -> c.getEmissionValue() == null ? 0 : c.getEmissionValue()).sum() / 7;
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("day", "D-" + i);
            p.put("emission", Math.round((base * (0.9 + (6 - i) * 0.03)) * 10) / 10.0);
            trend.add(p);
        }
        return trend;
    }

    public List<Map<String, Object>> detail() {
        dashboardSeedService.ensureSeedData();
        return carbonEmissionMapper.selectList(new LambdaQueryWrapper<CarbonEmission>()
                        .orderByDesc(CarbonEmission::getCalcTime))
                .stream().map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("carbonId", c.getCarbonId());
                    m.put("sourceType", c.getSourceType());
                    m.put("sourceName", c.getSourceName());
                    m.put("emissionValue", c.getEmissionValue());
                    m.put("unit", c.getUnit());
                    m.put("period", c.getPeriod());
                    m.put("calcMethod", c.getCalcMethod());
                    m.put("calcTime", c.getCalcTime() == null ? null : c.getCalcTime().format(FMT));
                    return m;
                }).toList();
    }

    private List<Map<String, Object>> bySource(List<CarbonEmission> all) {
        return all.stream().collect(Collectors.groupingBy(CarbonEmission::getSourceType)).entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("sourceType", e.getKey());
                    m.put("value", Math.round(e.getValue().stream().mapToDouble(CarbonEmission::getEmissionValue).sum() * 10) / 10.0);
                    return m;
                }).toList();
    }
}
