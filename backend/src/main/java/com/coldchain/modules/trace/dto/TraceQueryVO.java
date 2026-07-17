package com.coldchain.modules.trace.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 追溯查询完整结果
 */
@Data
@Builder
public class TraceQueryVO {

    private TraceProductVO product;
    private List<TraceNodeVO> timeline;
    private TraceChainVO blockchain;
}
