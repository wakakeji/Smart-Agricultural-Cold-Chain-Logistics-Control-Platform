package com.coldchain.modules.carbon.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.carbon.service.CarbonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "碳排放")
@RestController
@RequestMapping("/api/carbon")
@RequiredArgsConstructor
public class CarbonController {

    private final CarbonService carbonService;

    @GetMapping("/overview")
    @Operation(summary = "碳排放概览")
    public R<Map<String, Object>> overview() {
        return R.ok(carbonService.overview());
    }

    @GetMapping("/trend")
    @Operation(summary = "排放趋势")
    public R<List<Map<String, Object>>> trend() {
        return R.ok(carbonService.trend());
    }

    @GetMapping("/detail")
    @Operation(summary = "排放明细")
    public R<List<Map<String, Object>>> detail() {
        return R.ok(carbonService.detail());
    }
}
