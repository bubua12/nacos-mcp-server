package com.bubua12.mcp.nacos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 *
 * @author bubua12
 * @since 2025/9/15 10:20
 */
@Service
@Slf4j
public class NacosMetaService {
    private final WebClient webClient;

    public NacosMetaService(@Value("${nacos.server}") String nacosServer) {
        this.webClient = WebClient.builder()
                .baseUrl(nacosServer + "/nacos")
                .build();
    }

    @Tool(description = "获取Nacos实例的所有名称空间")
    public Mono<String> getNacosNamespaces() {
        log.info("开始调用这个方法");
        return webClient.get()
                .uri("/v1/console/namespaces")
                .headers(headers -> headers.setBasicAuth("nacos", "nacos"))
                .retrieve()
                .bodyToMono(String.class);
    }



    @Tool(description = "获取Nacos指定名称空间下的所有配置")
    public Mono<String> listNacosConfigByNamespace(
            @ToolParam(description = "输入参数，Nacos的名称空间，获取Nacos指定名称空间下的所有配置") String namespace) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/cs/configs")
                        .queryParam("search", "accurate")
                        .queryParam("dataId", "")
                        .queryParam("group", "")
                        .queryParam("appName", "")
                        .queryParam("namespaceId", namespace)
                        .queryParam("pageNo", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("accessToken", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuYWNvcyIsImV4cCI6MTc1Nzk1MjgyMn0.m0Di3cgCF4sWTY4sid3WgofK2gmbiUYZdlBifZL6qTnI90YST_dFXNC1vNer0sZ4")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }


}
