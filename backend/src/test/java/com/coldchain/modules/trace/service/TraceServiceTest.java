package com.coldchain.modules.trace.service;

import com.coldchain.modules.trace.dto.TraceVerifyVO;
import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.entity.ProductBatch;
import com.coldchain.modules.trace.mapper.BlockchainTxMapper;
import com.coldchain.modules.trace.mapper.ProductBatchMapper;
import com.coldchain.modules.trace.mapper.TraceRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 追溯验证单元测试
 */
@ExtendWith(MockitoExtension.class)
class TraceServiceTest {

    @Mock
    private ProductBatchMapper productBatchMapper;
    @Mock
    private TraceRecordMapper traceRecordMapper;
    @Mock
    private BlockchainTxMapper blockchainTxMapper;
    @Mock
    private MockBlockchainService mockBlockchainService;
    @InjectMocks
    private TraceService traceService;

    @Test
    void verifyPassWhenHashMatches() {
        ProductBatch batch = new ProductBatch();
        batch.setBatchNo("20260717000001");
        batch.setTxHash("0xabc");
        BlockchainTx tx = new BlockchainTx();
        tx.setTxHash("0xabc");
        tx.setDataHash("ddd");
        tx.setBlockNumber(100L);
        tx.setBizId("20260717000001");
        when(productBatchMapper.selectOne(any())).thenReturn(batch);
        when(blockchainTxMapper.selectOne(any())).thenReturn(tx);

        TraceVerifyVO vo = traceService.verify("20260717000001", null);
        assertTrue(vo.isValid());
        assertEquals("0xabc", vo.getTxHash());
    }
}
