package com.coldchain.modules.trace.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.trace.dto.TraceQueryVO;
import com.coldchain.modules.trace.dto.TraceVerifyVO;
import com.coldchain.modules.trace.service.TraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 追溯查询接口 QT-003
 */
@Tag(name = "追溯查询")
@RestController
@RequestMapping("/api/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;

    @Operation(summary = "追溯查询")
    @GetMapping("/query")
    public R<TraceQueryVO> query(@RequestParam(required = false) String batchNo,
                                 @RequestParam(required = false) String productName) {
        return R.ok(traceService.query(batchNo, productName));
    }

    @Operation(summary = "区块链存证验证")
    @GetMapping("/verify")
    public R<TraceVerifyVO> verify(@RequestParam(required = false) String batchNo,
                                   @RequestParam(required = false) String txHash) {
        return R.ok(traceService.verify(batchNo, txHash));
    }
}
