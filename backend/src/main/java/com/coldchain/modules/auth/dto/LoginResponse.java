package com.coldchain.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private UserInfoVO userInfo;
}
