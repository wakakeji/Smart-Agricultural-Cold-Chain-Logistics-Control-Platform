package com.coldchain.modules.trace.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.trace.dto.TraceNodeVO;
import com.coldchain.modules.trace.dto.TraceQueryVO;
import com.coldchain.modules.trace.dto.TraceVerifyVO;
import com.coldchain.modules.trace.service.TraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者 H5 适配接口 QT-006
 */
@Tag(name = "消费者H5")
@RestController
@RequestMapping("/api/h5")
@RequiredArgsConstructor
public class H5TraceController {

    private final TraceService traceService;

    @Operation(summary = "H5 追溯")
    @GetMapping("/trace")
    public R<Map<String, Object>> h5Trace(@RequestParam String batchNo) {
        TraceQueryVO q = traceService.query(batchNo, null);
        TraceVerifyVO v = traceService.verify(batchNo, q.getBlockchain() == null ? null : q.getBlockchain().getTxHash());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productName", q.getProduct().getProductName());
        m.put("batchNo", q.getProduct().getBatchNo());
        m.put("origin", q.getProduct().getOrigin());
        m.put("produceDate", q.getProduct().getProduceDate());
        m.put("shelfLife", q.getProduct().getShelfLife());
        m.put("qualityScore", 92);
        m.put("traceCount", q.getTimeline() == null ? 0 : q.getTimeline().size());
        m.put("timeline", q.getTimeline());
        m.put("tempHistory", tempHistory(q.getTimeline()));
        m.put("txHash", q.getBlockchain() == null ? null : q.getBlockchain().getTxHash());
        m.put("verified", v.isValid());
        m.put("verifyMessage", v.getMessage());
        return R.ok(m);
    }

    private List<Map<String, Object>> tempHistory(List<TraceNodeVO> timeline) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (timeline == null) {
            return list;
        }
        for (TraceNodeVO n : timeline) {
            if (n.getTemp() == null) {
                continue;
            }
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("time", n.getOpTime() == null ? "" : n.getOpTime().substring(5, 10));
            p.put("temp", n.getTemp());
            list.add(p);
        }
        return list;
    }
}
