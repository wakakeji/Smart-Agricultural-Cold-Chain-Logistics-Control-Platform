package com.coldchain.modules.blockchain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChainTxVO {
    private Long txId;
    private String txHash;
    private Long blockNumber;
    private String bizType;
    private String bizId;
    private String dataHash;
    private String chainStatus;
    private String createTime;
    private int confirmations;
}
