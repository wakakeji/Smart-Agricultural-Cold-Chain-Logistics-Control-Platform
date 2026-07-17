package com.coldchain.modules.loss.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.loss.entity.LossRecord;
import com.coldchain.modules.loss.mapper.LossRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LossService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM-dd");
    private final LossRecordMapper lossRecordMapper;

    public Map<String, Object> overview() {
        List<LossRecord> all = lossRecordMapper.selectList(null);
        double avgRate = all.isEmpty() ? 0 : all.stream().mapToDouble(LossRecord::getLossRate).average().orElse(0);
        double totalCost = all.stream().mapToDouble(r -> r.getCost() == null ? 0 : r.getCost()).sum();
        int lossQty = all.stream().mapToInt(r -> r.getLossQuantity() == null ? 0 : r.getLossQuantity()).sum();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("avgLossRate", Math.round(avgRate * 100) / 100.0);
        m.put("totalLossQty", lossQty);
        m.put("totalCost", Math.round(totalCost * 100) / 100.0);
        m.put("recordCount", all.size());
        m.put("byType", byType(all));
        return m;
    }

    public List<Map<String, Object>> trend() {
        List<LossRecord> all = lossRecordMapper.selectList(new LambdaQueryWrapper<LossRecord>()
                .orderByAsc(LossRecord::getReportDate));
        return all.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", r.getReportDate() == null ? "-" : r.getReportDate().format(DAY));
            m.put("lossRate", r.getLossRate());
            m.put("lossQuantity", r.getLossQuantity());
            m.put("cost", r.getCost());
            return m;
        }).toList();
    }

    public List<Map<String, Object>> detail() {
        return lossRecordMapper.selectList(new LambdaQueryWrapper<LossRecord>()
                        .orderByDesc(LossRecord::getReportDate))
                .stream().map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("lossId", r.getLossId());
                    m.put("batchNo", r.getBatchNo());
                    m.put("productName", r.getProductName());
                    m.put("totalQuantity", r.getTotalQuantity());
                    m.put("lossQuantity", r.getLossQuantity());
                    m.put("lossRate", r.getLossRate());
                    m.put("lossType", r.getLossType());
                    m.put("lossReason", r.getLossReason());
                    m.put("cost", r.getCost());
                    m.put("reportDate", r.getReportDate() == null ? null : r.getReportDate().toString());
                    return m;
                }).toList();
    }

    private List<Map<String, Object>> byType(List<LossRecord> all) {
        return all.stream().collect(Collectors.groupingBy(LossRecord::getLossType)).entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("type", e.getKey());
                    m.put("count", e.getValue().size());
                    m.put("avgRate", Math.round(e.getValue().stream().mapToDouble(LossRecord::getLossRate).average().orElse(0) * 100) / 100.0);
                    return m;
                }).toList();
    }
}
