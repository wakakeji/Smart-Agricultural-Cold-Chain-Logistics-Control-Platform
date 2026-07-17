package com.coldchain.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO)
    private Long dictId;
    private String dictType;
    private String dictCode;
    private String dictLabel;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
