package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 创建批次响应
 */
@Data
@Builder
public class BatchCreateResultVO {

    private Long batchId;
    private String batchNo;
    private String qrCodeUrl;
    private String txHash;
    /** CHAINED / PENDING */
    private String status;
}
