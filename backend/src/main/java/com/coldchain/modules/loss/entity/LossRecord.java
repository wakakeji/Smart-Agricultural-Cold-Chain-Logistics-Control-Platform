package com.coldchain.modules.loss.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("loss_record")
public class LossRecord {
    @TableId(type = IdType.AUTO)
    private Long lossId;
    private String batchNo;
    private String productName;
    private Integer totalQuantity;
    private Integer lossQuantity;
    private Double lossRate;
    private String lossType;
    private String lossReason;
    private Double cost;
    private LocalDate reportDate;
    private LocalDateTime createTime;
}
