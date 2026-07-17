package com.coldchain.modules.predict.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.predict.service.PredictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "品质预测")
@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
public class PredictController {

    private final PredictService predictService;

    @Operation(summary = "执行品质预测")
    @GetMapping("/quality")
    public R<Map<String, Object>> quality(@RequestParam(required = false) Long batchId,
                                          @RequestParam(required = false) String batchNo) {
        return R.ok(predictService.predict(batchId, batchNo));
    }

    @Operation(summary = "预测历史")
    @GetMapping("/history")
    public R<List<Map<String, Object>>> history(@RequestParam(required = false) Long batchId) {
        return R.ok(predictService.history(batchId));
    }

    @Operation(summary = "模型信息")
    @GetMapping("/model-info")
    public R<Map<String, Object>> modelInfo() {
        return R.ok(predictService.modelInfo());
    }
}
