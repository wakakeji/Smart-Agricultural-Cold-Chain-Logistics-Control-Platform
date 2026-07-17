package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 追溯时间轴节点
 */
@Data
@Builder
public class TraceNodeVO {

    private Long traceId;
    private String operation;
    private String operationLabel;
    private String operator;
    private String location;
    private Double temp;
    private Double humidity;
    private String opTime;
    private String txHash;
}
