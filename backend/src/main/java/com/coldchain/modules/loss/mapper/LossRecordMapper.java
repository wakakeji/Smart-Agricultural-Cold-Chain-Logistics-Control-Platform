package com.coldchain.modules.loss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.loss.entity.LossRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LossRecordMapper extends BaseMapper<LossRecord> {
}
