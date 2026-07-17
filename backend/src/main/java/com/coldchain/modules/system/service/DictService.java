package com.coldchain.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.system.entity.SysDict;
import com.coldchain.modules.system.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据字典：从数据库读取中文标签，禁止前端写死枚举文案
 */
@Service
@RequiredArgsConstructor
public class DictService {

    private final SysDictMapper sysDictMapper;
    private volatile Map<String, Map<String, String>> cache;

    public Map<String, Map<String, String>> allAsMap() {
        ensureCache();
        return cache;
    }

    public List<SysDict> listByType(String type) {
        return sysDictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(StringUtils.hasText(type), SysDict::getDictType, type)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSortOrder));
    }

    public String label(String type, String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        ensureCache();
        Map<String, String> m = cache.get(type);
        if (m == null) {
            return code;
        }
        return m.getOrDefault(code, code);
    }

    public long configLong(String code, long defaultVal) {
        String v = label("sys_config", code);
        if (v == null || v.equals(code)) {
            return defaultVal;
        }
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public void refresh() {
        cache = null;
        ensureCache();
    }

    private void ensureCache() {
        if (cache != null) {
            return;
        }
        synchronized (this) {
            if (cache != null) {
                return;
            }
            Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
            List<SysDict> all = sysDictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                    .eq(SysDict::getStatus, 1)
                    .orderByAsc(SysDict::getSortOrder));
            for (SysDict d : all) {
                map.computeIfAbsent(d.getDictType(), k -> new LinkedHashMap<>())
                        .put(d.getDictCode(), d.getDictLabel());
            }
            cache = map;
        }
    }

    public List<Map<String, String>> options(String type) {
        List<Map<String, String>> list = new ArrayList<>();
        for (SysDict d : listByType(type)) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("value", d.getDictCode());
            item.put("label", d.getDictLabel());
            list.add(item);
        }
        return list;
    }
}
