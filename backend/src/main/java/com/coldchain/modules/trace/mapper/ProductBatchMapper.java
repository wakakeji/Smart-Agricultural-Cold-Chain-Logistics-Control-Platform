package com.coldchain.modules.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.modules.trace.entity.ProductBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品批次 Mapper
 */
@Mapper
public interface ProductBatchMapper extends BaseMapper<ProductBatch> {
}
