package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 链上验证结果
 */
@Data
@Builder
public class TraceVerifyVO {

    private boolean valid;
    private String message;
    private String batchNo;
    private String txHash;
    private String dataHash;
    private Long blockNumber;
}
