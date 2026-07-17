package com.coldchain.modules.alarm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 告警统计
 */
@Data
@Builder
public class AlarmStatsVO {

    private long total;
    private long pending;
    private long processing;
    private long resolved;
    private long ignored;
    private Map<String, Long> byLevel;
    private Map<String, Long> byType;
}
