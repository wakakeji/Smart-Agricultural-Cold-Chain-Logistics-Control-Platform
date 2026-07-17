package com.coldchain.modules.blockchain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.blockchain.dto.ChainOverviewVO;
import com.coldchain.modules.blockchain.dto.ChainVerifyRequest;
import com.coldchain.modules.blockchain.dto.ChainVerifyVO;
import com.coldchain.modules.system.service.DictService;
import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.mapper.BlockchainTxMapper;
import com.coldchain.modules.trace.service.MockBlockchainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private BlockchainTxMapper blockchainTxMapper;
    @Mock
    private MockBlockchainService mockBlockchainService;
    @Mock
    private DictService dictService;
    @InjectMocks
    private BlockchainService blockchainService;

    @Test
    void overviewUsesDisplayFloorFromDict() {
        when(blockchainTxMapper.selectCount(null)).thenReturn(10L);
        BlockchainTx latest = new BlockchainTx();
        latest.setBlockNumber(100L);
        when(blockchainTxMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);
        when(dictService.configLong(eq("blockchain.display_total"), anyLong())).thenReturn(28456L);
        when(dictService.configLong(eq("blockchain.node_count"), anyLong())).thenReturn(7L);
        when(dictService.label("network_status", "HEALTHY")).thenReturn("正常");

        ChainOverviewVO vo = blockchainService.overview();
        assertEquals(28456L, vo.getTotalTx());
        assertEquals(7, vo.getNodeCount());
        assertEquals("正常", vo.getNetworkStatus());
    }

    @Test
    void verifyFindsByTxHash() {
        BlockchainTx tx = new BlockchainTx();
        tx.setTxHash("0xabc");
        tx.setBlockNumber(9L);
        when(blockchainTxMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(tx);
        ChainVerifyRequest req = new ChainVerifyRequest();
        req.setTxHash("0xabc");
        ChainVerifyVO vo = blockchainService.verify(req);
        assertTrue(vo.isVerified());
    }
}
