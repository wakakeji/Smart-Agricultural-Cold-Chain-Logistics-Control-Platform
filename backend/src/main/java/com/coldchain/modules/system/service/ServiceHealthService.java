package com.coldchain.modules.system.service;

import com.coldchain.config.ColdChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 中间件健康检查（系统/服务监控）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHealthService {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;
    private final ConnectionFactory rabbitConnectionFactory;
    private final MongoTemplate mongoTemplate;
    private final ColdChainProperties properties;

    public Map<String, Object> checkAll() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mysql", probe("MySQL", this::checkMysql));
        result.put("redis", probe("Redis", this::checkRedis));
        result.put("rabbitmq", probe("RabbitMQ", this::checkRabbit));
        result.put("mongodb", probe("MongoDB", this::checkMongo));
        result.put("influxdb", probe("InfluxDB", this::checkInflux));
        long up = result.values().stream().filter(v -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) v;
            return Boolean.TRUE.equals(m.get("up"));
        }).count();
        result.put("summary", Map.of("total", 5, "up", up, "down", 5 - up));
        return result;
    }

    @FunctionalInterface
    private interface HealthChecker {
        void check() throws Exception;
    }

    private Map<String, Object> probe(String name, HealthChecker checker) {
        long start = System.currentTimeMillis();
        try {
            checker.check();
            return Map.of("name", name, "up", true, "latencyMs", System.currentTimeMillis() - start, "message", "OK");
        } catch (Exception e) {
            log.warn("{} 健康检查失败: {}", name, e.getMessage());
            return Map.of("name", name, "up", false, "latencyMs", System.currentTimeMillis() - start,
                    "message", e.getMessage() == null ? "连接失败" : e.getMessage());
        }
    }

    private void checkMysql() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            if (!c.isValid(2)) {
                throw new IllegalStateException("连接无效");
            }
        }
    }

    private void checkRedis() {
        var factory = redisTemplate.getConnectionFactory();
        if (factory == null) {
            throw new IllegalStateException("RedisConnectionFactory 为空");
        }
        try (var conn = factory.getConnection()) {
            String pong = conn.ping();
            if (pong == null) {
                throw new IllegalStateException("PING 无响应");
            }
        }
    }

    private void checkRabbit() {
        try (var conn = rabbitConnectionFactory.createConnection()) {
            if (!conn.isOpen()) {
                throw new IllegalStateException("连接未打开");
            }
        }
    }

    private void checkMongo() {
        mongoTemplate.executeCommand("{ ping: 1 }");
    }

    /** InfluxDB 2.x 健康探测，不依赖 Token */
    private void checkInflux() throws Exception {
        String url = properties.getInflux().getUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("未配置 InfluxDB URL");
        }
        HttpURLConnection conn = (HttpURLConnection) URI.create(url + "/health").toURL().openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        if (code < 200 || code >= 400) {
            throw new IllegalStateException("HTTP " + code);
        }
    }
}
