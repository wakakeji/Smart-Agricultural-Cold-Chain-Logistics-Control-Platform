package com.coldchain.modules.system.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

/**
 * 权限 JSON 解析工具
 */
public final class PermissionUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PermissionUtils() {
    }

    public static List<String> parse(String permissionsJson) {
        if (permissionsJson == null || permissionsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(permissionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
