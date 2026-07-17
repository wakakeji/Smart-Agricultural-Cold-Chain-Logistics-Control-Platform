package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 追溯产品信息
 */
@Data
@Builder
public class TraceProductVO {

    private String productName;
    private String batchNo;
    private String origin;
    private String produceDate;
    private Integer shelfLife;
    private Integer quantity;
    private String unit;
    private Integer status;
}
