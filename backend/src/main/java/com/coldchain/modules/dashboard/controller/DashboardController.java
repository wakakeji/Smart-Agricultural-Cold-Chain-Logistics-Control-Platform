package com.coldchain.modules.dashboard.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.dashboard.dto.DashboardOverviewVO;
import com.coldchain.modules.dashboard.dto.RealtimeAlarmVO;
import com.coldchain.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指挥大屏接口 VM-001
 */
@Tag(name = "指挥大屏")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "大屏总览")
    @GetMapping("/overview")
    public R<DashboardOverviewVO> overview() {
        return R.ok(dashboardService.overview());
    }

    @Operation(summary = "实时告警滚动列表")
    @GetMapping("/realtime-alarms")
    public R<List<RealtimeAlarmVO>> realtimeAlarms(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(dashboardService.realtimeAlarms(limit));
    }
}
