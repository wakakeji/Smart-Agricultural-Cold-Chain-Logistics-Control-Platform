package com.coldchain.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.common.exception.BizException;
import com.coldchain.common.page.PageQuery;
import com.coldchain.common.page.PageResult;
import com.coldchain.modules.system.dto.UserSaveRequest;
import com.coldchain.modules.system.entity.SysUser;
import com.coldchain.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户管理业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<SysUser> page(PageQuery query, String keyword, String roleCode) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (StringUtils.hasText(roleCode)) {
            qw.eq(SysUser::getRoleCode, roleCode);
        }
        qw.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = userMapper.selectPage(new Page<>(query.getPage(), query.getSize()), qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public void create(UserSaveRequest req) {
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (cnt != null && cnt > 0) {
            throw new BizException("用户名已存在");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            throw new BizException("密码不能为空");
        }
        SysUser user = new SysUser();
        fill(user, req);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setLoginFailCount(0);
        userMapper.insert(user);
        log.info("新增用户: {}", user.getUsername());
    }

    public void update(Long id, UserSaveRequest req) {
        SysUser user = requireUser(id);
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername())
                .ne(SysUser::getUserId, id));
        if (cnt != null && cnt > 0) {
            throw new BizException("用户名已存在");
        }
        fill(user, req);
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        userMapper.updateById(user);
        log.info("更新用户: {}", user.getUsername());
    }

    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态仅支持 0/1");
        }
        SysUser user = requireUser(id);
        user.setStatus(status);
        if (status == 1) {
            user.setLoginFailCount(0);
            user.setLockTime(null);
        }
        userMapper.updateById(user);
        log.info("更新用户状态: {} -> {}", user.getUsername(), status);
    }

    private void fill(SysUser user, UserSaveRequest req) {
        user.setUsername(req.getUsername());
        user.setRealName(req.getRealName());
        user.setRoleCode(req.getRoleCode());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }
}
