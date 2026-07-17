package com.coldchain.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录用户信息视图
 */
@Data
@Builder
public class UserInfoVO {

    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String avatar;
    private List<String> permissions;
}
