package com.coldchain.modules.blockchain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChainVerifyVO {
    private boolean verified;
    private String txHash;
    private Long blockNumber;
    private String message;
}
