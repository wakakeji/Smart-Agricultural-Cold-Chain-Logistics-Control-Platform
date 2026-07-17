package com.coldchain.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.system.entity.SysUser;
import com.coldchain.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 开发环境：确保演示账号密码为 Abc@123456（避免 SQL 中哈希与编码器不一致）
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevPasswordInitRunner implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Abc@123456";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            var users = userMapper.selectList(new LambdaQueryWrapper<>());
            if (users.isEmpty()) {
                log.warn("sys_user 无数据，请先执行 sql/01_schema.sql 与 sql/02_mock_data.sql");
                return;
            }
            String encoded = passwordEncoder.encode(DEMO_PASSWORD);
            for (SysUser user : users) {
                if (!passwordEncoder.matches(DEMO_PASSWORD, user.getPassword())) {
                    user.setPassword(encoded);
                    userMapper.updateById(user);
                    log.info("已重置演示密码: {}", user.getUsername());
                }
            }
        } catch (Exception e) {
            log.warn("演示密码初始化跳过（数据库暂不可用）: {}", e.getMessage());
        }
    }
}
