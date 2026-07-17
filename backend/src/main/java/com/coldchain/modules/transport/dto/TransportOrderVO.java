package com.coldchain.modules.transport.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransportOrderVO {
    private Long orderId;
    private String orderNo;
    private Long batchId;
    private Long vehicleId;
    private String vehicleName;
    private String route;
    private String status;
    private String startTime;
    private Double currentTemp;
    private Double currentHumidity;
    private Double lng;
    private Double lat;
    private Double speed;
    private Integer progress;
}
