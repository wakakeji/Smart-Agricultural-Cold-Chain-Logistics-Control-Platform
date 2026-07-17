package com.coldchain.modules.blockchain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChainOverviewVO {
    private long currentBlock;
    private long totalTx;
    private int nodeCount;
    private String consensus;
    private String networkStatus;
}
