package com.coldchain.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图表数据点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartPointVO {

    private String name;
    private double value;
}
