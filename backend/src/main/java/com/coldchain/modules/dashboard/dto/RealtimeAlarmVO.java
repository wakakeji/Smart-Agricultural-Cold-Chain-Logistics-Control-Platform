package com.coldchain.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 大屏滚动告警项
 */
@Data
@Builder
public class RealtimeAlarmVO {

    private Long alarmId;
    private String type;
    private String level;
    private String sourceName;
    private String content;
    private String status;
    private LocalDateTime createTime;
}
