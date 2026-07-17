package com.coldchain.modules.alarm.controller;

import com.coldchain.common.api.R;
import com.coldchain.common.page.PageQuery;
import com.coldchain.common.page.PageResult;
import com.coldchain.modules.alarm.dto.AlarmDetailVO;
import com.coldchain.modules.alarm.dto.AlarmHandleRequest;
import com.coldchain.modules.alarm.dto.AlarmStatsVO;
import com.coldchain.modules.alarm.entity.AlarmRecord;
import com.coldchain.modules.alarm.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预警管理接口 VM-003
 */
@Tag(name = "预警管理")
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "告警分页列表")
    @GetMapping("/list")
    public R<PageResult<AlarmRecord>> list(PageQuery query,
                                           @RequestParam(required = false) String level,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String startTime,
                                           @RequestParam(required = false) String endTime) {
        return R.ok(alarmService.page(query, level, status, type, startTime, endTime));
    }

    @Operation(summary = "告警详情（含推送通道/工单演示字段）")
    @GetMapping("/{id}")
    public R<AlarmDetailVO> detail(@PathVariable Long id) {
        return R.ok(alarmService.detailVo(id));
    }

    @Operation(summary = "处理单条告警")
    @PutMapping("/{id}/handle")
    public R<Void> handle(@PathVariable Long id, @Valid @RequestBody AlarmHandleRequest request) {
        alarmService.handle(id, request);
        return R.ok();
    }

    @Operation(summary = "批量处理告警")
    @PutMapping("/batch-handle")
    public R<Void> batchHandle(@Valid @RequestBody AlarmHandleRequest request) {
        alarmService.batchHandle(request);
        return R.ok();
    }

    @Operation(summary = "告警统计")
    @GetMapping("/stats")
    public R<AlarmStatsVO> stats(@RequestParam(required = false) String startTime,
                                 @RequestParam(required = false) String endTime) {
        return R.ok(alarmService.stats(startTime, endTime));
    }
}
