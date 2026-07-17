package com.coldchain.modules.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模拟区块链交易记录
 */
@Data
@TableName("blockchain_tx")
public class BlockchainTx {

    @TableId(type = IdType.AUTO)
    private Long txId;
    private String txHash;
    private Long blockNumber;
    private String dataHash;
    private String bizType;
    private String bizId;
    private String chainStatus;
    private LocalDateTime createTime;
}
