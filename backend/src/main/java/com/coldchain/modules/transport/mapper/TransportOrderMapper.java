package com.coldchain.modules.transport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.transport.entity.TransportOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransportOrderMapper extends BaseMapper<TransportOrder> {
}
