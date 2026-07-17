package com.coldchain.modules.predict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.modules.dashboard.service.DashboardSeedService;
import com.coldchain.modules.predict.entity.QualityPrediction;
import com.coldchain.modules.predict.mapper.QualityPredictionMapper;
import com.coldchain.modules.trace.entity.ProductBatch;
import com.coldchain.modules.trace.mapper.ProductBatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 品质预测 AI-001（规则/公式模拟 XGBoost）
 */
@Service
@RequiredArgsConstructor
public class PredictService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final QualityPredictionMapper qualityPredictionMapper;
    private final ProductBatchMapper productBatchMapper;
    private final DashboardSeedService dashboardSeedService;

    public Map<String, Object> predict(Long batchId, String batchNo) {
        dashboardSeedService.ensureSeedData();
        ProductBatch batch = resolveBatch(batchId, batchNo);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double base = 94.0 - (batch.getBatchId() % 7) * 0.6;
        double score = Math.round((base + (r.nextDouble() - 0.5) * 2) * 10) / 10.0;
        int shelf = Math.max(24, 96 - (int) (batch.getBatchId() % 40));
        QualityPrediction p = new QualityPrediction();
        p.setBatchId(batch.getBatchId());
        p.setPredictTime(LocalDateTime.now());
        p.setQualityScore(score);
        p.setRemainingShelfLife(shelf);
        p.setModelVersion("xgb-v1-stub");
        p.setConfidence(0.88 + r.nextDouble() * 0.08);
        p.setCreateTime(LocalDateTime.now());
        qualityPredictionMapper.insert(p);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchId", batch.getBatchId());
        result.put("batchNo", batch.getBatchNo());
        result.put("productName", batch.getProductName());
        result.put("qualityScore", score);
        result.put("remainingShelfLife", shelf);
        result.put("modelVersion", p.getModelVersion());
        result.put("confidence", Math.round(p.getConfidence() * 1000) / 1000.0);
        result.put("predictTime", p.getPredictTime().format(FMT));
        result.put("riskLevel", score >= 90 ? "LOW" : score >= 80 ? "MEDIUM" : "HIGH");
        result.put("curve", buildCurve(score));
        return result;
    }

    public List<Map<String, Object>> history(Long batchId) {
        dashboardSeedService.ensureSeedData();
        List<QualityPrediction> list = qualityPredictionMapper.selectList(new LambdaQueryWrapper<QualityPrediction>()
                .eq(batchId != null, QualityPrediction::getBatchId, batchId)
                .orderByAsc(QualityPrediction::getPredictTime)
                .last("LIMIT 50"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (QualityPrediction p : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("predId", p.getPredId());
            m.put("batchId", p.getBatchId());
            m.put("qualityScore", p.getQualityScore());
            m.put("remainingShelfLife", p.getRemainingShelfLife());
            m.put("confidence", p.getConfidence());
            m.put("modelVersion", p.getModelVersion());
            m.put("predictTime", p.getPredictTime() == null ? null : p.getPredictTime().format(FMT));
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> modelInfo() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", "ColdChain-XGBoost-Stub");
        m.put("version", "xgb-v1-stub");
        m.put("algorithm", "XGBoost (Java stub)");
        m.put("features", List.of("avgTemp", "avgHumidity", "transportHours", "vibration", "storageDays"));
        m.put("trainSamples", 12800);
        m.put("mae", 1.8);
        m.put("r2", 0.93);
        m.put("updatedAt", "2026-07-01");
        return m;
    }

    private ProductBatch resolveBatch(Long batchId, String batchNo) {
        if (batchId != null) {
            ProductBatch b = productBatchMapper.selectById(batchId);
            if (b != null) return b;
        }
        if (batchNo != null && !batchNo.isBlank()) {
            ProductBatch b = productBatchMapper.selectOne(new LambdaQueryWrapper<ProductBatch>()
                    .eq(ProductBatch::getBatchNo, batchNo));
            if (b != null) return b;
        }
        ProductBatch any = productBatchMapper.selectOne(new LambdaQueryWrapper<ProductBatch>()
                .orderByDesc(ProductBatch::getBatchId).last("LIMIT 1"));
        if (any == null) {
            throw new BizException("无可用批次，请先赋码创建");
        }
        return any;
    }

    private List<Map<String, Object>> buildCurve(double score) {
        List<Map<String, Object>> curve = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("hour", i * 4);
            p.put("score", Math.round((score - i * 0.35) * 10) / 10.0);
            curve.add(p);
        }
        return curve;
    }
}
