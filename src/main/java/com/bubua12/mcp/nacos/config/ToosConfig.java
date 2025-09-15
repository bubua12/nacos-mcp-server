package com.bubua12.mcp.nacos.config;

import com.bubua12.mcp.nacos.service.NacosMetaService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * import tools
 *
 * @author bubua12
 * @since 2025/9/15 10:21
 */
@Configuration
public class ToosConfig {

    @Bean
    public ToolCallbackProvider nacosTools(NacosMetaService nacosMetaService) {
        return MethodToolCallbackProvider.builder().toolObjects(nacosMetaService).build();
    }
}
