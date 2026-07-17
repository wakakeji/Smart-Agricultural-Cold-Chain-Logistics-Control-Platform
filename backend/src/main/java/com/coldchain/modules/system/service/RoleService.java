package com.coldchain.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.system.entity.SysRole;
import com.coldchain.modules.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色查询业务
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper roleMapper;

    public List<SysRole> listEnabled() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getRoleId));
    }
}
