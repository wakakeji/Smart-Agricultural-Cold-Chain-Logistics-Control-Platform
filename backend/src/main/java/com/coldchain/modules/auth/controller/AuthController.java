package com.coldchain.modules.auth.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.auth.dto.LoginRequest;
import com.coldchain.modules.auth.dto.LoginResponse;
import com.coldchain.modules.auth.dto.UserInfoVO;
import com.coldchain.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/user-info")
    public R<UserInfoVO> userInfo() {
        return R.ok(authService.currentUserInfo());
    }
}
