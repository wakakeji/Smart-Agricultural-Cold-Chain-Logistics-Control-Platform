package com.coldchain.config;

import com.coldchain.modules.dashboard.service.DashboardSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时补充大屏演示数据
 */
@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class DashboardSeedRunner implements CommandLineRunner {

    private final DashboardSeedService dashboardSeedService;

    @Override
    public void run(String... args) {
        try {
            dashboardSeedService.ensureSeedData();
        } catch (Exception e) {
            log.warn("大屏种子初始化跳过: {}", e.getMessage());
        }
    }
}
