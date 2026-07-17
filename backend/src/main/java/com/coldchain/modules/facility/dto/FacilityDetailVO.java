package com.coldchain.modules.facility.dto;

import com.coldchain.modules.facility.entity.SensorDevice;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 设施详情
 */
@Data
@Builder
public class FacilityDetailVO {

    private FacilityItemVO facility;
    private List<SensorDevice> sensors;
}
