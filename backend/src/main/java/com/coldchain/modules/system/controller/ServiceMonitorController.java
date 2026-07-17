package com.coldchain.modules.system.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.system.service.ServiceHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统/服务监控接口
 */
@Tag(name = "系统服务监控")
@RestController
@RequestMapping("/api/system/health")
@RequiredArgsConstructor
public class ServiceMonitorController {

    private final ServiceHealthService healthService;

    @Operation(summary = "中间件健康检查")
    @GetMapping
    public R<Map<String, Object>> health() {
        return R.ok(healthService.checkAll());
    }
}
