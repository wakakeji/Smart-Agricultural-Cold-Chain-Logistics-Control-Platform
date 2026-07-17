package com.coldchain.modules.system.controller;

import com.coldchain.common.api.R;
import com.coldchain.common.page.PageQuery;
import com.coldchain.common.page.PageResult;
import com.coldchain.modules.system.dto.UserSaveRequest;
import com.coldchain.modules.system.entity.SysUser;
import com.coldchain.modules.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理接口
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户分页")
    @GetMapping
    public R<PageResult<SysUser>> page(PageQuery query,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String roleCode) {
        return R.ok(userService.page(query, keyword, roleCode));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public R<Void> create(@Valid @RequestBody UserSaveRequest request) {
        userService.create(request);
        return R.ok();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveRequest request) {
        userService.update(id, request);
        return R.ok();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.updateStatus(id, body.get("status"));
        return R.ok();
    }
}
