package com.coldchain.modules.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.trace.entity.TraceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 追溯记录 Mapper
 */
@Mapper
public interface TraceRecordMapper extends BaseMapper<TraceRecord> {
}
