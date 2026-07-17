package com.coldchain.modules.alarm.dto;

import com.coldchain.modules.alarm.entity.AlarmRecord;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 告警详情（含 VM-003 推送/工单演示字段）
 */
@Data
@Builder
public class AlarmDetailVO {

    private AlarmRecord alarm;
    /** 工单号 */
    private String workOrderNo;
    /** 自动分发负责人 */
    private String assignee;
    /** 推送通道：SMS / APP */
    private List<String> notifyChannels;
    /** 推送说明（演示） */
    private String notifyDesc;
    /** 规则引擎说明 */
    private String ruleEngine;
    /** 归档状态 */
    private boolean archived;
}
