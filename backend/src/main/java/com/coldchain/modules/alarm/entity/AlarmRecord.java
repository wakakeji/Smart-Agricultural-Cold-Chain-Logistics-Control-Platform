package com.coldchain.modules.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警记录实体
 */
@Data
@TableName("alarm_record")
public class AlarmRecord {

    @TableId(type = IdType.AUTO)
    private Long alarmId;
    private String type;
    private String level;
    private Long sourceId;
    private String sourceName;
    private String content;
    private Double currentValue;
    private Double threshold;
    private String status;
    private String handler;
    private String handleRemark;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}
