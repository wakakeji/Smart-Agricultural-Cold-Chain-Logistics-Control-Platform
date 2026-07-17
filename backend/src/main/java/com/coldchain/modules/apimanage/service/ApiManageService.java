package com.coldchain.modules.apimanage.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * API 管理 API-001（内存注册表 + 密钥生成）
 */
@Service
public class ApiManageService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, Map<String, Object>> keys = new ConcurrentHashMap<>();

    public List<Map<String, Object>> listApis() {
        return List.of(
                api("认证-登录", "POST", "/api/auth/login", "用户登录", "认证"),
                api("设施-列表", "GET", "/api/facilities", "设施列表", "设施"),
                api("大屏-概览", "GET", "/api/dashboard/overview", "指挥大屏", "大屏"),
                api("告警-列表", "GET", "/api/alarm/list", "告警列表", "告警"),
                api("监测-传感器", "GET", "/api/monitor/sensors", "实时传感器", "监测"),
                api("追溯-赋码", "POST", "/api/batch/create", "批次赋码", "追溯"),
                api("追溯-查询", "GET", "/api/trace/query", "追溯查询", "追溯"),
                api("链上-存证", "GET", "/api/blockchain/txs", "链上存证", "区块链"),
                api("AI-品质预测", "GET", "/api/predict/quality", "品质预测", "AI"),
                api("运输-监控", "GET", "/api/transport/ongoing", "运输监控", "运输"),
                api("统计-损耗", "GET", "/api/loss/overview", "损耗概览", "统计"),
                api("统计-碳排", "GET", "/api/carbon/overview", "碳排放", "统计")
        );
    }

    public Map<String, Object> generateKey(String name) {
        String key = "ck_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("keyId", UUID.randomUUID().toString().substring(0, 8));
        item.put("name", name == null || name.isBlank() ? "默认应用" : name);
        item.put("apiKey", key);
        item.put("status", "启用");
        item.put("qpsLimit", 100);
        item.put("createTime", LocalDateTime.now().format(FMT));
        item.put("callCount", 0);
        keys.put(key, item);
        return item;
    }

    public List<Map<String, Object>> listKeys() {
        if (keys.isEmpty()) {
            generateKey("演示应用");
        }
        return new ArrayList<>(keys.values());
    }

    public Map<String, Object> stats() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("apiCount", listApis().size());
        m.put("keyCount", listKeys().size());
        m.put("todayCalls", 12000 + r.nextInt(3000));
        m.put("successRate", 99.2);
        m.put("avgLatencyMs", 46);
        m.put("docsUrl", "/swagger-ui.html");
        return m;
    }

    private Map<String, Object> api(String code, String method, String path, String name, String module) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("method", method);
        m.put("path", path);
        m.put("name", name);
        m.put("module", module);
        m.put("status", "在线");
        m.put("version", "v1");
        return m;
    }
}
