package com.bubua12.mcp.nacos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Start Application
 *
 * @author bubua12
 * @since 2025/9/15 9:55
 */
@Slf4j
@SpringBootApplication
public class NacosWebfluxMCPServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosWebfluxMCPServerApplication.class, args);
        log.info("NacosWebfluxMCPServerApplication Start Success... ...");
    }
}
