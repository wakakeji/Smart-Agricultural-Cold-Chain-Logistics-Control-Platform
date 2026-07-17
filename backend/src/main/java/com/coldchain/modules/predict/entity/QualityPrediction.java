package com.coldchain.modules.predict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quality_prediction")
public class QualityPrediction {
    @TableId(type = IdType.AUTO)
    private Long predId;
    private Long batchId;
    private LocalDateTime predictTime;
    private Double qualityScore;
    private Integer remainingShelfLife;
    private String modelVersion;
    private Double confidence;
    private LocalDateTime createTime;
}
