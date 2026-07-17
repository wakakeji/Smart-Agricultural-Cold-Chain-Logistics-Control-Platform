package com.coldchain.modules.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 车辆轨迹点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackPointVO {

    private Double lng;
    private Double lat;
    private Double speed;
    private LocalDateTime reportTime;
}
