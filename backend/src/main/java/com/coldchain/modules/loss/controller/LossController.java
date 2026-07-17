package com.coldchain.modules.loss.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.loss.service.LossService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "损耗分析")
@RestController
@RequestMapping("/api/loss")
@RequiredArgsConstructor
public class LossController {

    private final LossService lossService;

    @GetMapping("/overview")
    @Operation(summary = "损耗概览")
    public R<Map<String, Object>> overview() {
        return R.ok(lossService.overview());
    }

    @GetMapping("/trend")
    @Operation(summary = "损耗趋势")
    public R<List<Map<String, Object>>> trend() {
        return R.ok(lossService.trend());
    }

    @GetMapping("/detail")
    @Operation(summary = "损耗明细")
    public R<List<Map<String, Object>>> detail() {
        return R.ok(lossService.detail());
    }
}
