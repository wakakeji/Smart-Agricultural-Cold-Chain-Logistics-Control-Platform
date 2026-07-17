package com.coldchain.modules.blockchain.dto;

import lombok.Data;

@Data
public class ChainVerifyRequest {
    private String dataHash;
    private String txHash;
    private String originalData;
}
