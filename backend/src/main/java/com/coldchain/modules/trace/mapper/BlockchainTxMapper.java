package com.coldchain.modules.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.trace.entity.BlockchainTx;
import org.apache.ibatis.annotations.Mapper;

/**
 * 区块链交易 Mapper
 */
@Mapper
public interface BlockchainTxMapper extends BaseMapper<BlockchainTx> {
}
