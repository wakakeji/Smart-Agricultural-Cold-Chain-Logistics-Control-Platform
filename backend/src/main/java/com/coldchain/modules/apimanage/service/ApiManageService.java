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
                api("AUTH-LOGIN", "POST", "/api/auth/login", "用户登录", "auth"),
                api("FACILITY-LIST", "GET", "/api/facilities", "设施列表", "facility"),
                api("DASHBOARD", "GET", "/api/dashboard/overview", "指挥大屏", "dashboard"),
                api("ALARM-LIST", "GET", "/api/alarm/list", "告警列表", "alarm"),
                api("MONITOR", "GET", "/api/monitor/sensors", "实时传感器", "monitor"),
                api("BATCH-CREATE", "POST", "/api/batch/create", "批次赋码", "trace"),
                api("TRACE-QUERY", "GET", "/api/trace/query", "追溯查询", "trace"),
                api("CHAIN-TX", "GET", "/api/blockchain/txs", "链上存证", "blockchain"),
                api("PREDICT", "GET", "/api/predict/quality", "品质预测", "ai"),
                api("TRANSPORT", "GET", "/api/transport/ongoing", "运输监控", "transport"),
                api("LOSS", "GET", "/api/loss/overview", "损耗概览", "stats"),
                api("CARBON", "GET", "/api/carbon/overview", "碳排放", "stats")
        );
    }

    public Map<String, Object> generateKey(String name) {
        String key = "ck_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("keyId", UUID.randomUUID().toString().substring(0, 8));
        item.put("name", name == null || name.isBlank() ? "默认应用" : name);
        item.put("apiKey", key);
        item.put("status", "ACTIVE");
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
        m.put("status", "ONLINE");
        m.put("version", "v1");
        return m;
    }
}
