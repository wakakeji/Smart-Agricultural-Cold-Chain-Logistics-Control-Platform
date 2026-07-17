package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 二维码信息
 */
@Data
@Builder
public class QrCodeVO {

    private Long batchId;
    private String batchNo;
    private String qrContent;
    private String qrCodeUrl;
    private String txHash;
}
