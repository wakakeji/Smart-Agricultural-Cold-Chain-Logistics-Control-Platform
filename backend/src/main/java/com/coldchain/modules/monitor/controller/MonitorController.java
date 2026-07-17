package com.coldchain.modules.monitor.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.monitor.dto.SensorListVO;
import com.coldchain.modules.monitor.dto.TrendPointVO;
import com.coldchain.modules.monitor.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 实时环境监测接口 EM-001
 */
@Tag(name = "实时环境监测")
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "传感器实时列表")
    @GetMapping("/sensors")
    public R<SensorListVO> sensors(@RequestParam(required = false) Long facilityId,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "false") boolean withTrend) {
        return R.ok(monitorService.listSensors(facilityId, type, status, withTrend));
    }

    @Operation(summary = "传感器历史趋势")
    @GetMapping("/sensor/{id}/history")
    public R<List<TrendPointVO>> history(@PathVariable Long id,
                                         @RequestParam(defaultValue = "24") int hours) {
        return R.ok(monitorService.history(id, hours));
    }

    @Operation(summary = "设施筛选列表")
    @GetMapping("/facilities")
    public R<List<Map<String, Object>>> facilities() {
        return R.ok(monitorService.facilities());
    }
}
