package com.coldchain.modules.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 追溯节点记录
 */
@Data
@TableName("trace_record")
public class TraceRecord {

    @TableId(type = IdType.AUTO)
    private Long traceId;
    private Long batchId;
    private String operation;
    private String operator;
    private String location;
    private Double temp;
    private Double humidity;
    private LocalDateTime opTime;
    private String txHash;
    private LocalDateTime createTime;
}
