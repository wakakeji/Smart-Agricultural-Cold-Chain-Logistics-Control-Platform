package com.coldchain.modules.facility.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地图设施列表项
 */
@Data
@Builder
public class FacilityItemVO {

    private Long id;
    private String name;
    private String type;
    private Double lng;
    private Double lat;
    private String status;
    private Double currentTemp;
    private Double currentHumidity;
    private Double capacity;
    private Double usedCapacity;
    private Double loadRate;
    private String address;
    /** 车辆车牌（车辆类型时与 name 一致或单独展示） */
    private String plateNo;
    private String driverName;
    private String driverPhone;
    private Double speed;
    private LocalDateTime updateTime;
}
