package com.coldchain.modules.dataquality.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.dataquality.service.DataQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "数据质量")
@RestController
@RequestMapping("/api/data-quality")
@RequiredArgsConstructor
public class DataQualityController {

    private final DataQualityService dataQualityService;

    @GetMapping("/overview")
    @Operation(summary = "数据质量概览")
    public R<Map<String, Object>> overview() {
        return R.ok(dataQualityService.overview());
    }
}
