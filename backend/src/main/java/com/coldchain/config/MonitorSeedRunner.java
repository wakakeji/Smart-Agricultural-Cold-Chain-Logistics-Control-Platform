package com.coldchain.config;

import com.coldchain.modules.monitor.service.MonitorSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时补齐实时监控传感器
 */
@Slf4j
@Component
@Order(25)
@RequiredArgsConstructor
public class MonitorSeedRunner implements CommandLineRunner {

    private final MonitorSeedService monitorSeedService;

    @Override
    public void run(String... args) {
        try {
            monitorSeedService.ensureSensors();
        } catch (Exception e) {
            log.warn("传感器种子初始化跳过: {}", e.getMessage());
        }
    }
}
