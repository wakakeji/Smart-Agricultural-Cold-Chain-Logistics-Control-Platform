package com.coldchain.modules.monitor.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实时数据传感器卡片
 */
@Data
@Builder
public class SensorCardVO {

    private Long sensorId;
    private Long facilityId;
    private String facilityName;
    private String name;
    private String type;
    private Double temperature;
    private Double humidity;
    private Double co2;
    private Integer battery;
    /** ONLINE / OFFLINE / ERROR */
    private String status;
    private LocalDateTime updateTime;
    private List<TrendPointVO> trendData;
}
