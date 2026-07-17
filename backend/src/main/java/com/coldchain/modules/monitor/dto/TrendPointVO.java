package com.coldchain.modules.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势数据点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO {

    private String time;
    private double value;
}
