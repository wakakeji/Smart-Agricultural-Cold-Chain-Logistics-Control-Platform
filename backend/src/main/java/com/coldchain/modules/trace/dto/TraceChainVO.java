package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 链上存证摘要
 */
@Data
@Builder
public class TraceChainVO {

    private String txHash;
    private Long blockNumber;
    private String chainTime;
    private Integer confirmations;
    private String chainStatus;
    private String dataHash;
}
