package com.coldchain.modules.transport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("transport_order")
public class TransportOrder {
    @TableId(type = IdType.AUTO)
    private Long orderId;
    private String orderNo;
    private Long batchId;
    private Long vehicleId;
    private Long driverId;
    private String route;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
