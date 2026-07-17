package com.coldchain.modules.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 传感器列表响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorListVO {

    private List<SensorCardVO> records;
    private long total;
}
