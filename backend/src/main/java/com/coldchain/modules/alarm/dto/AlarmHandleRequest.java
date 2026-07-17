package com.coldchain.modules.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 单条/批量告警处理请求
 */
@Data
public class AlarmHandleRequest {

    /** RESOLVED / IGNORED / PROCESSING */
    @NotBlank(message = "处理状态不能为空")
    private String status;

    private String handleRemark;

    /** 批量处理时使用 */
    private java.util.List<Long> ids;
}
