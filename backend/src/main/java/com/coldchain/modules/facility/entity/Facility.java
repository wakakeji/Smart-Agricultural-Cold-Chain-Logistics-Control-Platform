package com.coldchain.modules.facility.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 冷库 / 冷藏车设施实体
 */
@Data
@TableName("facility")
public class Facility {

    @TableId(type = IdType.AUTO)
    private Long facilityId;
    private String name;
    /** COLD_STORAGE / REFRIGERATED_VEHICLE */
    private String type;
    private Double lng;
    private Double lat;
    /** ONLINE / OFFLINE / ALARM */
    private String status;
    private Double capacity;
    private Double usedCapacity;
    private String address;
    private Double currentTemp;
    private Double currentHumidity;
    private Double loadRate;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
