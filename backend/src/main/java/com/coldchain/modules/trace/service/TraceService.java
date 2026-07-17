package com.coldchain.modules.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.modules.trace.dto.*;
import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.entity.ProductBatch;
import com.coldchain.modules.trace.entity.TraceRecord;
import com.coldchain.modules.trace.mapper.BlockchainTxMapper;
import com.coldchain.modules.trace.mapper.ProductBatchMapper;
import com.coldchain.modules.trace.mapper.TraceRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全链条追溯查询 QT-003
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, String> OP_LABEL = Map.of(
            "PRODUCE", "生产赋码",
            "STORE_IN", "冷库入库",
            "STORE_OUT", "冷库出库",
            "TRANSPORT", "冷链运输",
            "ARRIVE", "到达收货",
            "RETAIL", "零售上架",
            "SALE", "终端销售",
            "生产赋码", "生产赋码"
    );

    private final ProductBatchMapper productBatchMapper;
    private final TraceRecordMapper traceRecordMapper;
    private final BlockchainTxMapper blockchainTxMapper;
    private final MockBlockchainService mockBlockchainService;

    @Transactional
    public TraceQueryVO query(String batchNo, String productName) {
        ProductBatch batch = findBatch(batchNo, productName);
        ensureDemoTimeline(batch);
        normalizeProduceNode(batch);
        List<TraceRecord> records = traceRecordMapper.selectList(new LambdaQueryWrapper<TraceRecord>()
                .eq(TraceRecord::getBatchId, batch.getBatchId())
                .orderByAsc(TraceRecord::getOpTime));
        BlockchainTx tx = null;
        if (StringUtils.hasText(batch.getTxHash())) {
            tx = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                    .eq(BlockchainTx::getTxHash, batch.getTxHash())
                    .last("LIMIT 1"));
        }
        return TraceQueryVO.builder()
                .product(toProduct(batch))
                .timeline(records.stream().map(this::toNode).toList())
                .blockchain(toChain(batch, tx))
                .build();
    }

    public TraceVerifyVO verify(String batchNo, String txHash) {
        if (!StringUtils.hasText(batchNo) && !StringUtils.hasText(txHash)) {
            throw new BizException("请提供批次号或交易哈希");
        }
        ProductBatch batch = null;
        if (StringUtils.hasText(batchNo)) {
            batch = productBatchMapper.selectOne(new LambdaQueryWrapper<ProductBatch>()
                    .eq(ProductBatch::getBatchNo, batchNo));
        }
        BlockchainTx tx = null;
        if (StringUtils.hasText(txHash)) {
            tx = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                    .eq(BlockchainTx::getTxHash, txHash).last("LIMIT 1"));
        } else if (batch != null && StringUtils.hasText(batch.getTxHash())) {
            tx = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                    .eq(BlockchainTx::getTxHash, batch.getTxHash()).last("LIMIT 1"));
        }
        if (tx == null) {
            return TraceVerifyVO.builder().valid(false).message("未找到链上存证记录")
                    .batchNo(batchNo).txHash(txHash).build();
        }
        if (batch != null && StringUtils.hasText(batch.getTxHash())
                && !batch.getTxHash().equals(tx.getTxHash())) {
            return TraceVerifyVO.builder().valid(false).message("批次哈希与链上记录不一致")
                    .batchNo(batch.getBatchNo()).txHash(tx.getTxHash())
                    .dataHash(tx.getDataHash()).blockNumber(tx.getBlockNumber()).build();
        }
        return TraceVerifyVO.builder().valid(true).message("存证验证通过（模拟链）")
                .batchNo(batch != null ? batch.getBatchNo() : tx.getBizId())
                .txHash(tx.getTxHash()).dataHash(tx.getDataHash())
                .blockNumber(tx.getBlockNumber()).build();
    }

    private ProductBatch findBatch(String batchNo, String productName) {
        if (StringUtils.hasText(batchNo)) {
            ProductBatch batch = productBatchMapper.selectOne(new LambdaQueryWrapper<ProductBatch>()
                    .eq(ProductBatch::getBatchNo, batchNo.trim()));
            if (batch == null) {
                throw new BizException("未找到批次：" + batchNo);
            }
            return batch;
        }
        if (StringUtils.hasText(productName)) {
            List<ProductBatch> list = productBatchMapper.selectList(new LambdaQueryWrapper<ProductBatch>()
                    .like(ProductBatch::getProductName, productName.trim())
                    .orderByDesc(ProductBatch::getCreateTime)
                    .last("LIMIT 1"));
            if (list.isEmpty()) {
                throw new BizException("未找到产品：" + productName);
            }
            return list.get(0);
        }
        throw new BizException("请输入批次号或产品名称");
    }

    /** 演示：若仅有生产节点，自动补齐冷链全链路节点 */
    private void ensureDemoTimeline(ProductBatch batch) {
        Long cnt = traceRecordMapper.selectCount(new LambdaQueryWrapper<TraceRecord>()
                .eq(TraceRecord::getBatchId, batch.getBatchId()));
        if (cnt != null && cnt >= 4) {
            return;
        }
        LocalDateTime base = batch.getProduceDate() != null ? batch.getProduceDate() : LocalDateTime.now().minusDays(2);
        normalizeProduceNode(batch);
        List<Object[]> nodes = new ArrayList<>();
        nodes.add(new Object[]{"STORE_IN", "东盟冷库", "南宁东盟开发区", 2.5, 88.0, base.plusHours(6)});
        nodes.add(new Object[]{"TRANSPORT", "冷链物流公司", "南宁→广州", 3.0, 85.0, base.plusHours(22)});
        nodes.add(new Object[]{"ARRIVE", "广州江南市场", "广州市白云区", 4.0, 82.0, base.plusHours(30)});
        for (Object[] n : nodes) {
            String op = (String) n[0];
            String payload = batch.getBatchNo() + "|" + op + "|" + n[5];
            var tx = mockBlockchainService.chain("TRACE", batch.getBatchNo() + "-" + op, payload);
            TraceRecord r = new TraceRecord();
            r.setBatchId(batch.getBatchId());
            r.setOperation(op);
            r.setOperator((String) n[1]);
            r.setLocation((String) n[2]);
            r.setTemp((Double) n[3]);
            r.setHumidity((Double) n[4]);
            r.setOpTime((LocalDateTime) n[5]);
            r.setTxHash(tx.getTxHash());
            r.setCreateTime(LocalDateTime.now());
            traceRecordMapper.insert(r);
        }
        batch.setStatus(2);
        productBatchMapper.updateById(batch);
        log.info("已补齐演示追溯链路 batchNo={}", batch.getBatchNo());
    }

    /** 校正生产赋码节点时间与操作码，保证时间轴从生产开始 */
    private void normalizeProduceNode(ProductBatch batch) {
        LocalDateTime base = batch.getProduceDate() != null ? batch.getProduceDate() : LocalDateTime.now().minusDays(2);
        List<TraceRecord> existing = traceRecordMapper.selectList(new LambdaQueryWrapper<TraceRecord>()
                .eq(TraceRecord::getBatchId, batch.getBatchId()));
        for (TraceRecord er : existing) {
            boolean produce = "PRODUCE".equals(er.getOperation()) || "生产赋码".equals(er.getOperation());
            if (!produce) {
                continue;
            }
            boolean need = !"PRODUCE".equals(er.getOperation())
                    || er.getOpTime() == null
                    || er.getOpTime().isAfter(base.plusHours(1));
            if (need) {
                er.setOperation("PRODUCE");
                er.setOpTime(base);
                traceRecordMapper.updateById(er);
            }
        }
    }

    private TraceProductVO toProduct(ProductBatch b) {
        return TraceProductVO.builder()
                .productName(b.getProductName())
                .batchNo(b.getBatchNo())
                .origin(b.getOrigin())
                .produceDate(b.getProduceDate() == null ? null : b.getProduceDate().toLocalDate().toString())
                .shelfLife(b.getShelfLife())
                .quantity(b.getQuantity())
                .unit(b.getUnit())
                .status(b.getStatus())
                .build();
    }

    private TraceNodeVO toNode(TraceRecord r) {
        return TraceNodeVO.builder()
                .traceId(r.getTraceId())
                .operation(r.getOperation())
                .operationLabel(OP_LABEL.getOrDefault(r.getOperation(), r.getOperation()))
                .operator(r.getOperator())
                .location(r.getLocation())
                .temp(r.getTemp())
                .humidity(r.getHumidity())
                .opTime(r.getOpTime() == null ? null : r.getOpTime().format(FMT))
                .txHash(r.getTxHash())
                .build();
    }

    private TraceChainVO toChain(ProductBatch batch, BlockchainTx tx) {
        if (tx == null) {
            return TraceChainVO.builder()
                    .txHash(batch.getTxHash())
                    .confirmations(0)
                    .chainStatus("UNKNOWN")
                    .build();
        }
        return TraceChainVO.builder()
                .txHash(tx.getTxHash())
                .blockNumber(tx.getBlockNumber())
                .chainTime(tx.getCreateTime() == null ? null : tx.getCreateTime().format(FMT))
                .confirmations(12)
                .chainStatus(tx.getChainStatus())
                .dataHash(tx.getDataHash())
                .build();
    }
}
