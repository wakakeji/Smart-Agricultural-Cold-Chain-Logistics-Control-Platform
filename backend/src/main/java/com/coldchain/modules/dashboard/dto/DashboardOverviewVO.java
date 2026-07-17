package com.coldchain.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 指挥大屏总览数据
 */
@Data
@Builder
public class DashboardOverviewVO {

    private Map<String, KpiItemVO> kpi;
    private Map<String, List<ChartPointVO>> charts;
}
