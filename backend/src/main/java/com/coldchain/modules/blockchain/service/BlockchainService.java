package com.coldchain.modules.blockchain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.common.page.PageQuery;
import com.coldchain.modules.blockchain.dto.*;
import com.coldchain.modules.system.service.DictService;
import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.mapper.BlockchainTxMapper;
import com.coldchain.modules.trace.service.MockBlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 区块链存证 QT-004（MySQL 模拟链）
 */
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BlockchainTxMapper blockchainTxMapper;
    private final MockBlockchainService mockBlockchainService;
    private final DictService dictService;

    public ChainOverviewVO overview() {
        Long cnt = blockchainTxMapper.selectCount(null);
        long real = cnt == null ? 0 : cnt;
        long displayFloor = dictService.configLong("blockchain.display_total", real);
        int nodeCount = (int) dictService.configLong("blockchain.node_count", 7);
        BlockchainTx latest = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                .orderByDesc(BlockchainTx::getBlockNumber).last("LIMIT 1"));
        long block = latest == null || latest.getBlockNumber() == null ? 0L : latest.getBlockNumber();
        return ChainOverviewVO.builder()
                .currentBlock(block)
                .totalTx(Math.max(real, displayFloor))
                .nodeCount(nodeCount)
                .consensus("PBFT")
                .networkStatus(dictService.label("network_status", "HEALTHY"))
                .build();
    }

    public ChainTxPageVO page(PageQuery query, String bizType, String status) {
        LambdaQueryWrapper<BlockchainTx> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(bizType)) {
            qw.eq(BlockchainTx::getBizType, bizType);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(BlockchainTx::getChainStatus, status);
        }
        qw.orderByDesc(BlockchainTx::getCreateTime);
        Page<BlockchainTx> page = blockchainTxMapper.selectPage(new Page<>(query.getPage(), query.getSize()), qw);
        long realTotal = page.getTotal();
        long displayFloor = dictService.configLong("blockchain.display_total", realTotal);
        return ChainTxPageVO.builder()
                .overview(overview())
                .records(page.getRecords().stream().map(this::toVo).toList())
                .total(Math.max(realTotal, displayFloor))
                .page(page.getCurrent())
                .size(page.getSize())
                .build();
    }

    public ChainVerifyVO verify(ChainVerifyRequest req) {
        BlockchainTx tx = null;
        if (StringUtils.hasText(req.getTxHash())) {
            tx = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                    .eq(BlockchainTx::getTxHash, req.getTxHash()).last("LIMIT 1"));
        }
        if (tx == null && StringUtils.hasText(req.getDataHash())) {
            tx = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                    .eq(BlockchainTx::getDataHash, req.getDataHash()).last("LIMIT 1"));
        }
        if (tx == null && StringUtils.hasText(req.getOriginalData())) {
            String hash = DigestUtils.md5DigestAsHex(req.getOriginalData().getBytes(StandardCharsets.UTF_8));
            String sha = mockBlockchainService.sha256(req.getOriginalData());
            tx = blockchainTxMapper.selectOne(new LambdaQueryWrapper<BlockchainTx>()
                    .eq(BlockchainTx::getDataHash, sha).last("LIMIT 1"));
            if (tx == null) {
                return ChainVerifyVO.builder().verified(false).message("未找到匹配存证，原始数据哈希=" + hash).build();
            }
        }
        if (tx == null) {
            return ChainVerifyVO.builder().verified(false).message("请提供 dataHash / txHash / originalData").build();
        }
        return ChainVerifyVO.builder()
                .verified(true)
                .txHash(tx.getTxHash())
                .blockNumber(tx.getBlockNumber())
                .message("数据完整，未被篡改（模拟链）")
                .build();
    }

    public List<MapNode> topology() {
        return List.of(
                MapNode.of("node-1", "共识节点-南宁", "leader"),
                MapNode.of("node-2", "共识节点-柳州", "peer"),
                MapNode.of("node-3", "共识节点-桂林", "peer"),
                MapNode.of("node-4", "观察节点-广州", "observer"),
                MapNode.of("node-5", "观察节点-深圳", "observer"),
                MapNode.of("node-6", "观察节点-北海", "observer"),
                MapNode.of("node-7", "归档节点-玉林", "archive")
        );
    }

    private ChainTxVO toVo(BlockchainTx t) {
        return ChainTxVO.builder()
                .txId(t.getTxId())
                .txHash(t.getTxHash())
                .blockNumber(t.getBlockNumber())
                .bizType(t.getBizType())
                .bizId(t.getBizId())
                .dataHash(t.getDataHash())
                .chainStatus(t.getChainStatus())
                .createTime(t.getCreateTime() == null ? null : t.getCreateTime().format(FMT))
                .confirmations(12)
                .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor(staticName = "of")
    public static class MapNode {
        private String id;
        private String name;
        private String role;
    }
}
