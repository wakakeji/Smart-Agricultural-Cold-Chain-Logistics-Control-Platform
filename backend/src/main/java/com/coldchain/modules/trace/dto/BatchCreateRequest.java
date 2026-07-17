package com.coldchain.modules.trace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建批次请求
 */
@Data
public class BatchCreateRequest {

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotBlank(message = "产地不能为空")
    private String origin;

    @NotNull(message = "生产日期不能为空")
    private LocalDate produceDate;

    @NotNull(message = "保质期不能为空")
    @Min(value = 1, message = "保质期至少1天")
    private Integer shelfLife;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @NotBlank(message = "单位不能为空")
    private String unit;
}
