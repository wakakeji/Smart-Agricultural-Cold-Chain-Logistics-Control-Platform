package com.coldchain.config;

import com.coldchain.modules.facility.service.FacilitySeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时确保地图演示设施数量达标
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class FacilitySeedRunner implements CommandLineRunner {

    private final FacilitySeedService facilitySeedService;

    @Override
    public void run(String... args) {
        try {
            facilitySeedService.ensureSeedData();
        } catch (Exception e) {
            log.warn("设施种子初始化跳过: {}", e.getMessage());
        }
    }
}
