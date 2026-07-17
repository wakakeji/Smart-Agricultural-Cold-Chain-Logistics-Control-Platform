package com.coldchain.modules.dashboard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 兼容旧调用：演示数据已迁至 sql/04_business_seed.sql，不再在代码中写死插入。
 */
@Slf4j
@Service
public class DashboardSeedService {

    public void ensureSeedData() {
        // no-op：请通过 sql/import.ps1 导入业务种子数据
    }
}
