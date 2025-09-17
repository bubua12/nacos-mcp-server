package com.bubua12.mcp.nacos.utils;

import java.util.Objects;

/**
 * 校验工具类
 *
 * @author bubua12
 * @since 2025/9/17 15:12
 */
public class ValidateChecker {

    public static void checkNonNull(Object object, String errorMessage) {
        if (Objects.isNull(object)) {
            throw new RuntimeException(errorMessage);
        }
    }

}
