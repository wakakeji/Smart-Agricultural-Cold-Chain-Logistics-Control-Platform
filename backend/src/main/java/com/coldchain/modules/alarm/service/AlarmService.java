package com.coldchain.modules.alarm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.common.exception.BizException;
import com.coldchain.common.page.PageQuery;
import com.coldchain.common.page.PageResult;
import com.coldchain.modules.alarm.dto.AlarmDetailVO;
import com.coldchain.modules.alarm.dto.AlarmHandleRequest;
import com.coldchain.modules.alarm.dto.AlarmStatsVO;
import com.coldchain.modules.alarm.entity.AlarmRecord;
import com.coldchain.modules.alarm.mapper.AlarmRecordMapper;
import com.coldchain.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警管理业务 VM-003
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final List<String> HANDLE_STATUS = List.of("RESOLVED", "IGNORED", "PROCESSING");

    private final AlarmRecordMapper alarmRecordMapper;

    public PageResult<AlarmRecord> page(PageQuery query, String level, String status, String type,
                                        String startTime, String endTime) {
        LambdaQueryWrapper<AlarmRecord> qw = buildQuery(level, status, type, startTime, endTime);
        qw.orderByDesc(AlarmRecord::getCreateTime);
        Page<AlarmRecord> page = alarmRecordMapper.selectPage(new Page<>(query.getPage(), query.getSize()), qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AlarmRecord detail(Long id) {
        AlarmRecord record = alarmRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException("告警不存在");
        }
        return record;
    }

    /** 详情 + VM-003 推送/工单演示信息 */
    public AlarmDetailVO detailVo(Long id) {
        AlarmRecord record = detail(id);
        List<String> channels = notifyChannels(record.getLevel());
        boolean archived = "RESOLVED".equals(record.getStatus()) || "IGNORED".equals(record.getStatus());
        return AlarmDetailVO.builder()
                .alarm(record)
                .workOrderNo("WO-" + record.getAlarmId())
                .assignee(archived && StringUtils.hasText(record.getHandler()) ? record.getHandler() : "值班负责人")
                .notifyChannels(channels)
                .notifyDesc(buildNotifyDesc(record.getLevel(), channels))
                .ruleEngine("Drools规则引擎（演示）+ 极光推送/短信网关模拟通道")
                .archived(archived)
                .build();
    }

    @Transactional
    public void handle(Long id, AlarmHandleRequest req) {
        validateStatus(req.getStatus());
        AlarmRecord record = detail(id);
        applyHandle(record, req.getStatus(), req.getHandleRemark());
        alarmRecordMapper.updateById(record);
        List<String> channels = notifyChannels(record.getLevel());
        log.info("处理告警 id={} status={} by={} notifyChannels={}（模拟短信/APP）",
                id, req.getStatus(), record.getHandler(), channels);
    }

    @Transactional
    public void batchHandle(AlarmHandleRequest req) {
        if (req.getIds() == null || req.getIds().isEmpty()) {
            throw new BizException("请选择要处理的告警");
        }
        validateStatus(req.getStatus());
        for (Long id : req.getIds()) {
            AlarmRecord record = alarmRecordMapper.selectById(id);
            if (record == null) {
                continue;
            }
            applyHandle(record, req.getStatus(), req.getHandleRemark());
            alarmRecordMapper.updateById(record);
        }
        log.info("批量处理告警 count={} status={}", req.getIds().size(), req.getStatus());
    }

    public AlarmStatsVO stats(String startTime, String endTime) {
        LambdaQueryWrapper<AlarmRecord> base = buildQuery(null, null, null, startTime, endTime);
        List<AlarmRecord> all = alarmRecordMapper.selectList(base);
        Map<String, Long> byLevel = new LinkedHashMap<>();
        Map<String, Long> byType = new LinkedHashMap<>();
        long pending = 0, processing = 0, resolved = 0, ignored = 0;
        for (AlarmRecord a : all) {
            byLevel.merge(a.getLevel(), 1L, Long::sum);
            byType.merge(a.getType(), 1L, Long::sum);
            switch (a.getStatus() == null ? "" : a.getStatus()) {
                case "PENDING" -> pending++;
                case "PROCESSING" -> processing++;
                case "RESOLVED" -> resolved++;
                case "IGNORED" -> ignored++;
                default -> { }
            }
        }
        return AlarmStatsVO.builder()
                .total(all.size())
                .pending(pending)
                .processing(processing)
                .resolved(resolved)
                .ignored(ignored)
                .byLevel(byLevel)
                .byType(byType)
                .build();
    }

    private void applyHandle(AlarmRecord record, String status, String remark) {
        record.setStatus(status);
        record.setHandleRemark(remark);
        record.setHandler(currentHandlerName());
        record.setHandleTime(LocalDateTime.now());
    }

    /** 紧急：短信+APP；重要：短信+APP；普通：仅APP */
    private List<String> notifyChannels(String level) {
        if ("CRITICAL".equalsIgnoreCase(level) || "EMERGENCY".equalsIgnoreCase(level)) {
            return List.of("SMS", "APP");
        }
        if ("WARNING".equalsIgnoreCase(level) || "IMPORTANT".equalsIgnoreCase(level)) {
            return List.of("SMS", "APP");
        }
        return List.of("APP");
    }

    private String buildNotifyDesc(String level, List<String> channels) {
        String joined = String.join("+", channels);
        return "级别 " + level + " → 推送通道 " + joined + "（演示环境不真实下发，仅记录规则）";
    }

    private void validateStatus(String status) {
        if (!HANDLE_STATUS.contains(status)) {
            throw new BizException("状态仅支持 RESOLVED/IGNORED/PROCESSING");
        }
    }

    private LambdaQueryWrapper<AlarmRecord> buildQuery(String level, String status, String type,
                                                       String startTime, String endTime) {
        LambdaQueryWrapper<AlarmRecord> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            qw.eq(AlarmRecord::getLevel, level);
        }
        if (StringUtils.hasText(status)) {
            // 前端“已处理”可传 RESOLVED,IGNORED
            if (status.contains(",")) {
                qw.in(AlarmRecord::getStatus, List.of(status.split(",")));
            } else {
                qw.eq(AlarmRecord::getStatus, status);
            }
        }
        if (StringUtils.hasText(type)) {
            qw.eq(AlarmRecord::getType, type);
        }
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (StringUtils.hasText(startTime)) {
            qw.ge(AlarmRecord::getCreateTime, LocalDateTime.parse(startTime, fmt));
        }
        if (StringUtils.hasText(endTime)) {
            qw.le(AlarmRecord::getCreateTime, LocalDateTime.parse(endTime, fmt));
        }
        return qw;
    }

    private String currentHandlerName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
        }
        return "系统";
    }
}
