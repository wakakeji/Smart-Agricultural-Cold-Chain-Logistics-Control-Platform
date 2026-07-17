package com.coldchain.modules.carbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.carbon.entity.CarbonEmission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarbonEmissionMapper extends BaseMapper<CarbonEmission> {
}
