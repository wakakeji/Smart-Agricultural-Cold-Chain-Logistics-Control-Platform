package com.coldchain.modules.system.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限解析单元测试
 */
class PermissionUtilsTest {

    @Test
    void parseValidJson() {
        List<String> list = PermissionUtils.parse("[\"map:read\",\"trace:read\"]");
        assertEquals(2, list.size());
        assertTrue(list.contains("map:read"));
    }

    @Test
    void parseBlankReturnsEmpty() {
        assertTrue(PermissionUtils.parse(null).isEmpty());
        assertTrue(PermissionUtils.parse("").isEmpty());
        assertTrue(PermissionUtils.parse("not-json").isEmpty());
    }
}
