package com.coldchain.modules.apimanage.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.apimanage.service.ApiManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "API管理")
@RestController
@RequestMapping("/api/api-manage")
@RequiredArgsConstructor
public class ApiManageController {

    private final ApiManageService apiManageService;

    @GetMapping("/list")
    @Operation(summary = "标准 API 目录")
    public R<List<Map<String, Object>>> list() {
        return R.ok(apiManageService.listApis());
    }

    @GetMapping("/keys")
    @Operation(summary = "密钥列表")
    public R<List<Map<String, Object>>> keys() {
        return R.ok(apiManageService.listKeys());
    }

    @PostMapping("/key/generate")
    @Operation(summary = "生成 API Key")
    public R<Map<String, Object>> generate(@RequestBody(required = false) Map<String, String> body) {
        String name = body == null ? null : body.get("name");
        return R.ok(apiManageService.generateKey(name));
    }

    @GetMapping("/stats")
    @Operation(summary = "调用统计")
    public R<Map<String, Object>> stats() {
        return R.ok(apiManageService.stats());
    }
}
