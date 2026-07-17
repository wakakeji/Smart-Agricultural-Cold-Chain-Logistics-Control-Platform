package com.coldchain.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    private String roleCode;
    private String phone;
    private String email;
    private String avatar;
    /** 0禁用 1启用 2锁定 */
    private Integer status;
    private Long deptId;
    private Integer loginFailCount;
    private LocalDateTime lockTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
