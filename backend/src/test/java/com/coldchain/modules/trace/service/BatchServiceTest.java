package com.coldchain.modules.trace.service;

import com.coldchain.modules.trace.dto.BatchCreateRequest;
import com.coldchain.modules.trace.dto.BatchCreateResultVO;
import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.entity.ProductBatch;
import com.coldchain.modules.trace.mapper.ProductBatchMapper;
import com.coldchain.modules.trace.mapper.TraceRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批次赋码单元测试
 */
@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private ProductBatchMapper productBatchMapper;
    @Mock
    private TraceRecordMapper traceRecordMapper;
    @Mock
    private MockBlockchainService mockBlockchainService;
    @InjectMocks
    private BatchService batchService;

    @Test
    void createGeneratesBatchNoAndChains() {
        when(productBatchMapper.selectCount(any())).thenReturn(0L);
        doAnswer(inv -> {
            ProductBatch b = inv.getArgument(0);
            b.setBatchId(100L);
            return 1;
        }).when(productBatchMapper).insert(any(ProductBatch.class));

        BlockchainTx tx = new BlockchainTx();
        tx.setTxHash("0xabc123");
        when(mockBlockchainService.chain(eq("BATCH"), anyString(), anyString())).thenReturn(tx);

        BatchCreateRequest req = new BatchCreateRequest();
        req.setProductName("武鸣沃柑");
        req.setOrigin("广西南宁武鸣");
        req.setProduceDate(LocalDate.of(2026, 7, 15));
        req.setShelfLife(30);
        req.setQuantity(500);
        req.setUnit("箱");

        BatchCreateResultVO result = batchService.create(req);
        assertEquals("CHAINED", result.getStatus());
        assertEquals("0xabc123", result.getTxHash());
        assertTrue(result.getBatchNo().startsWith("2026"));
        assertTrue(result.getQrCodeUrl().contains(result.getBatchNo()));
        verify(traceRecordMapper).insert(any());
        verify(productBatchMapper, atLeastOnce()).updateById(any());
    }
}
