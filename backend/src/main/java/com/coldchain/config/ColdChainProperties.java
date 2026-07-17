package com.coldchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 平台自定义配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "coldchain")
public class ColdChainProperties {

    private Jwt jwt = new Jwt();
    private Auth auth = new Auth();
    private Influx influx = new Influx();
    private Map map = new Map();

    @Data
    public static class Jwt {
        private String secret;
        private long expireSeconds = 86400;
        private long rememberExpireSeconds = 604800;
    }

    @Data
    public static class Auth {
        private int maxFailCount = 5;
        private int lockMinutes = 15;
    }

    @Data
    public static class Influx {
        private String url;
        private String org;
        private String bucket;
        private String token;
    }

    @Data
    public static class Map {
        private String baiduAk;
    }
}
