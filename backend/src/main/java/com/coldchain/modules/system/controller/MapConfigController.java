package com.coldchain.modules.system.controller;

import com.coldchain.common.api.R;
import com.coldchain.config.ColdChainProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 地图配置（向前端下发百度 AK，避免写死在前端源码）
 */
@Tag(name = "地图配置")
@RestController
@RequestMapping("/api/system/map-config")
@RequiredArgsConstructor
public class MapConfigController {

    private final ColdChainProperties properties;

    @GetMapping
    @Operation(summary = "获取地图配置")
    public R<Map<String, Object>> config() {
        String ak = properties.getMap() == null ? null : properties.getMap().getBaiduAk();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("provider", StringUtils.hasText(ak) ? "baidu" : "mock");
        m.put("baiduAk", ak == null ? "" : ak);
        m.put("enabled", StringUtils.hasText(ak));
        return R.ok(m);
    }
}
