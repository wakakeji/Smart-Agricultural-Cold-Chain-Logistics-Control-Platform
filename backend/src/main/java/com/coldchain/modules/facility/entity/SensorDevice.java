package com.coldchain.modules.facility.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 传感器设备实体
 */
@Data
@TableName("sensor_device")
public class SensorDevice {

    @TableId(type = IdType.AUTO)
    private Long sensorId;
    private Long facilityId;
    private String name;
    private String type;
    private String model;
    private Integer status;
    private LocalDate installDate;
    private LocalDateTime lastMaintain;
    private LocalDateTime createTime;
}
