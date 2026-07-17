package com.coldchain.modules.suggestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("decision_suggestion")
public class DecisionSuggestion {
    @TableId(type = IdType.AUTO)
    private Long sugId;
    private String type;
    private String title;
    private String content;
    private String priority;
    private String status;
    private Long relatedBatchId;
    private LocalDateTime createTime;
}
