package com.coldchain.modules.trace.service;

import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.mapper.BlockchainTxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 模拟区块链存证：SHA-256 哈希写入 MySQL
 */
@Service
@RequiredArgsConstructor
public class MockBlockchainService {

    private final BlockchainTxMapper blockchainTxMapper;

    public BlockchainTx chain(String bizType, String bizId, String payload) {
        String dataHash = sha256(payload);
        String txHash = "0x" + sha256(bizType + "|" + bizId + "|" + dataHash + "|" + System.nanoTime());
        BlockchainTx tx = new BlockchainTx();
        tx.setTxHash(txHash);
        tx.setBlockNumber(System.currentTimeMillis() / 1000);
        tx.setDataHash(dataHash);
        tx.setBizType(bizType);
        tx.setBizId(bizId);
        tx.setChainStatus("CONFIRMED");
        tx.setCreateTime(LocalDateTime.now());
        blockchainTxMapper.insert(tx);
        return tx;
    }

    public String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("哈希计算失败", e);
        }
    }
}
