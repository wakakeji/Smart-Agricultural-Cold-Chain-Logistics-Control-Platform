package com.coldchain.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.config.ColdChainProperties;
import com.coldchain.modules.auth.dto.LoginRequest;
import com.coldchain.modules.auth.dto.LoginResponse;
import com.coldchain.modules.auth.dto.UserInfoVO;
import com.coldchain.modules.system.entity.SysRole;
import com.coldchain.modules.system.entity.SysUser;
import com.coldchain.modules.system.mapper.SysRoleMapper;
import com.coldchain.modules.system.mapper.SysUserMapper;
import com.coldchain.modules.system.util.PermissionUtils;
import com.coldchain.security.JwtTokenProvider;
import com.coldchain.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证业务：登录、登出、获取当前用户
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final ColdChainProperties properties;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            throw new BizException(401, "用户名或密码错误");
        }
        // 检查锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            throw new BizException(423, "账号已锁定，请稍后再试");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "账号已禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            onLoginFail(user);
            throw new BizException(401, "用户名或密码错误");
        }
        // 登录成功重置失败计数
        user.setLoginFailCount(0);
        user.setLockTime(null);
        user.setStatus(1);
        userMapper.updateById(user);

        List<String> permissions = loadPermissions(user.getRoleCode());
        boolean remember = Boolean.TRUE.equals(req.getRememberMe());
        String token = jwtTokenProvider.createToken(
                user.getUserId(), user.getUsername(), user.getRoleCode(), permissions, remember);
        long expiresIn = jwtTokenProvider.expireSeconds(remember);
        tokenService.store(user.getUserId(), token, expiresIn);
        log.info("用户登录成功: {} role={}", user.getUsername(), user.getRoleCode());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userInfo(toUserInfo(user, permissions))
                .build();
    }

    public void logout() {
        LoginUser loginUser = currentLoginUser();
        if (loginUser != null) {
            tokenService.remove(loginUser.getUserId());
            log.info("用户登出: {}", loginUser.getUsername());
        }
    }

    public UserInfoVO currentUserInfo() {
        LoginUser loginUser = currentLoginUser();
        if (loginUser == null) {
            throw new BizException(401, "未登录");
        }
        SysUser user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        return toUserInfo(user, loadPermissions(user.getRoleCode()));
    }

    private void onLoginFail(SysUser user) {
        int fail = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        fail++;
        user.setLoginFailCount(fail);
        int max = properties.getAuth().getMaxFailCount();
        if (fail >= max) {
            user.setStatus(2);
            user.setLockTime(LocalDateTime.now().plusMinutes(properties.getAuth().getLockMinutes()));
            log.warn("账号锁定: {} 失败{}次", user.getUsername(), fail);
        }
        userMapper.updateById(user);
    }

    private List<String> loadPermissions(String roleCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        return role == null ? List.of() : PermissionUtils.parse(role.getPermissions());
    }

    private UserInfoVO toUserInfo(SysUser user, List<String> permissions) {
        return UserInfoVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRoleCode())
                .avatar(user.getAvatar())
                .permissions(permissions)
                .build();
    }

    private LoginUser currentLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }
}
