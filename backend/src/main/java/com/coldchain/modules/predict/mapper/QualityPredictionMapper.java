package com.coldchain.modules.predict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.predict.entity.QualityPrediction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QualityPredictionMapper extends BaseMapper<QualityPrediction> {
}
