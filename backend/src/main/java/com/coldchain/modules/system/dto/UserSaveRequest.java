package com.coldchain.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户新增/更新请求
 */
@Data
public class UserSaveRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50)
    private String username;

    /** 新增必填；更新时可空表示不改密码 */
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "角色不能为空")
    private String roleCode;

    private String phone;
    private String email;
    private Integer status = 1;
}
