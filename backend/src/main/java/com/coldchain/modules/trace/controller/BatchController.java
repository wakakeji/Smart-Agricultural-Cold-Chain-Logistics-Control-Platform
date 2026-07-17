package com.coldchain.modules.trace.controller;

import com.coldchain.common.api.R;
import com.coldchain.common.page.PageQuery;
import com.coldchain.common.page.PageResult;
import com.coldchain.modules.trace.dto.BatchCreateRequest;
import com.coldchain.modules.trace.dto.BatchCreateResultVO;
import com.coldchain.modules.trace.dto.QrCodeVO;
import com.coldchain.modules.trace.entity.ProductBatch;
import com.coldchain.modules.trace.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 批次赋码接口 QT-002
 */
@Tag(name = "批次赋码")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @Operation(summary = "创建批次并赋码上链")
    @PostMapping("/create")
    public R<BatchCreateResultVO> create(@Valid @RequestBody BatchCreateRequest request) {
        return R.ok(batchService.create(request));
    }

    @Operation(summary = "批次列表")
    @GetMapping("/list")
    public R<PageResult<ProductBatch>> list(PageQuery query,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String keyword) {
        return R.ok(batchService.page(query, status, keyword));
    }

    @Operation(summary = "获取批次二维码内容")
    @GetMapping("/{id}/qr-code")
    public R<QrCodeVO> qrCode(@PathVariable Long id) {
        return R.ok(batchService.qrCode(id));
    }
}
