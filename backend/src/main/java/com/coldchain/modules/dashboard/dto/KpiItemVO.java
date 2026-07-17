package com.coldchain.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPI 指标项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiItemVO {

    private double value;
    private String unit;
    private String trend;
    private double change;
}
