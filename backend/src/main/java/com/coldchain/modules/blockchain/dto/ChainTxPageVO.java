package com.coldchain.modules.blockchain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChainTxPageVO {
    private ChainOverviewVO overview;
    private List<ChainTxVO> records;
    private long total;
    private long page;
    private long size;
}
