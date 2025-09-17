package com.bubua12.mcp.nacos;

import com.alibaba.fastjson.JSON;
import com.bubua12.mcp.nacos.service.NacosMCPService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit Tests
 *
 * @author bubua12
 * @since 2025/9/17 11:00
 */
@SpringBootTest
public class NacosServiceTests {
    @Resource
    private NacosMCPService nacosService;

    @Test
    public void test01() {
        System.out.println(nacosService.getConfig("common.properties", "order", "dev"));
    }

    @Test
    public void test02() {
        String json = JSON.toJSONString(nacosService.getConfigHistory("common.properties", "order", "dev"));
        System.out.println();
        System.out.println(json);
    }

    @Test
    public void test03() {
        System.out.println();
        System.out.println(nacosService.listConfigsByNamespace("dev"));
    }

    @Test
    public void test04() {
        System.out.println(nacosService.getCurrentNodeHealth());
    }
}

