package com.coldchain.modules.blockchain.controller;

import com.coldchain.common.api.R;
import com.coldchain.common.page.PageQuery;
import com.coldchain.modules.blockchain.dto.*;
import com.coldchain.modules.blockchain.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "区块链存证")
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @Operation(summary = "网络概览")
    @GetMapping("/overview")
    public R<ChainOverviewVO> overview() {
        return R.ok(blockchainService.overview());
    }

    @Operation(summary = "存证交易分页")
    @GetMapping("/txs")
    public R<ChainTxPageVO> txs(PageQuery query,
                                @RequestParam(required = false) String bizType,
                                @RequestParam(required = false) String status) {
        return R.ok(blockchainService.page(query, bizType, status));
    }

    @Operation(summary = "哈希验证")
    @PostMapping("/verify")
    public R<ChainVerifyVO> verify(@RequestBody ChainVerifyRequest request) {
        return R.ok(blockchainService.verify(request));
    }

    @Operation(summary = "网络拓扑节点")
    @GetMapping("/topology")
    public R<List<BlockchainService.MapNode>> topology() {
        return R.ok(blockchainService.topology());
    }
}
