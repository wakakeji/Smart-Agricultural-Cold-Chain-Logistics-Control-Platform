package com.coldchain.modules.facility.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 地图设施列表响应
 */
@Data
@Builder
public class FacilityListVO {

    private List<FacilityItemVO> coldStorages;
    private List<FacilityItemVO> vehicles;
    private Map<String, Integer> total;
}
