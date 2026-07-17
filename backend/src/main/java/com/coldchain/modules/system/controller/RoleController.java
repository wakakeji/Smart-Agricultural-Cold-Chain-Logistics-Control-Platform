package com.coldchain.modules.system.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.system.entity.SysRole;
import com.coldchain.modules.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色接口
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "启用角色列表")
    @GetMapping
    public R<List<SysRole>> list() {
        return R.ok(roleService.listEnabled());
    }
}
