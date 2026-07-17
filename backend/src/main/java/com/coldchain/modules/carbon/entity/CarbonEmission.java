package com.coldchain.modules.carbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("carbon_emission")
public class CarbonEmission {
    @TableId(type = IdType.AUTO)
    private Long carbonId;
    private String sourceType;
    private Long sourceId;
    private String sourceName;
    private Double emissionValue;
    private String unit;
    private LocalDateTime calcTime;
    private String period;
    private String calcMethod;
}
