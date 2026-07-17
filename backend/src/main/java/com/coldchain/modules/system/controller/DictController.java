package com.coldchain.modules.system.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.system.entity.SysDict;
import com.coldchain.modules.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据字典")
@RestController
@RequestMapping("/api/system/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/map")
    @Operation(summary = "全部字典 Map(type -> code -> label)")
    public R<Map<String, Map<String, String>>> map() {
        return R.ok(dictService.allAsMap());
    }

    @GetMapping
    @Operation(summary = "按类型查询字典项")
    public R<List<SysDict>> list(@RequestParam(required = false) String type) {
        return R.ok(dictService.listByType(type));
    }

    @GetMapping("/options")
    @Operation(summary = "下拉选项")
    public R<List<Map<String, String>>> options(@RequestParam String type) {
        return R.ok(dictService.options(type));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新缓存")
    public R<Void> refresh() {
        dictService.refresh();
        return R.ok();
    }
}
