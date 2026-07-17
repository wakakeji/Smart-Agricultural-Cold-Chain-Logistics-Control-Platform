package com.coldchain.modules.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品批次实体
 */
@Data
@TableName("product_batch")
public class ProductBatch {

    @TableId(type = IdType.AUTO)
    private Long batchId;
    private String batchNo;
    private String productName;
    private String origin;
    private Long producerId;
    private LocalDateTime produceDate;
    private Integer shelfLife;
    private Integer quantity;
    private String unit;
    private String qrCode;
    private String txHash;
    /** 0待上链 1已上链 2已追溯 */
    private Integer status;
    private LocalDateTime createTime;
}
