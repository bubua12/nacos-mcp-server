package com.bubua12.mcp.nacos.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * Nacos元数据服务，提供与Nacos集群交互的工具方法
 *
 * @author bubua12
 * @since 2025/9/15 10:20
 */
@Service
@Slf4j
public class NacosMetaService {
    private final WebClient webClient;
    private final String username;
    private final String nacosServer;
    private final String password;

    public NacosMetaService(
            @Value("${nacos.server}") String nacosServer,
            @Value("${nacos.username:nacos}") String username,
            @Value("${nacos.password:nacos}") String password) {
        this.nacosServer = nacosServer;
        this.username = username;
        this.password = password;
        this.webClient = WebClient.builder().baseUrl(nacosServer + "/nacos").codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)).build();
        log.info("NacosMetaService 初始化完成，Nacos服务地址: {}", nacosServer);
    }

    /**
     * 创建Nacos NamingService实例
     */
    private NamingService createNamingService() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", nacosServer.replace("/nacos", ""));
            properties.setProperty("namespace", "");
            properties.setProperty("username", username);
            properties.setProperty("password", password);

            return NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error("创建Nacos NamingService失败", e);
            throw new RuntimeException("创建Nacos客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取Nacos中的所有命名空间列表
     *
     * @return 获取Nacos中的所有命名空间列表
     */
    @Tool(description = "获取Nacos中的所有命名空间列表")
    public String getNacosNamespaces() {
        log.info("开始获取 Nacos 命名空间列表");

        try {
            // 阻塞执行，确保触发请求
            return webClient.get().uri("/v1/console/namespaces").headers(headers -> headers.setBasicAuth(username, password)).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, resp -> Mono.error(new RuntimeException("Nacos认证失败，请检查用户名和密码"))).bodyToMono(String.class).doOnNext(res -> log.info("获取命名空间列表成功，响应: {}", res)).doOnError(err -> log.error("获取命名空间列表失败", err)).onErrorMap(WebClientResponseException.class, ex -> new RuntimeException("调用Nacos命名空间API失败: " + ex.getMessage(), ex)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).filter(throwable -> !(throwable instanceof RuntimeException))).block();
        } catch (Exception e) {
            log.error("调用 Nacos 命名空间API异常: {}", e.getMessage(), e);
            return "调用失败: " + e.getMessage();
        }
    }


    @Tool(description = "获取Nacos指定名称空间下的所有配置")
    public Mono<String> listNacosConfigByNamespace(@ToolParam(description = "输入参数，Nacos的名称空间ID，获取Nacos指定名称空间下的所有配置") String namespace) {
        log.info("开始获取命名空间 [{}] 下的配置列表", namespace);
        return webClient.get().uri(uriBuilder -> uriBuilder.path("/v1/cs/configs").queryParam("search", "accurate").queryParam("dataId", "").queryParam("group", "").queryParam("appName", "").queryParam("namespaceId", StringUtils.hasText(namespace) ? namespace : "").queryParam("pageNo", 1).queryParam("pageSize", 50).build()).headers(headers -> headers.setBasicAuth(username, password)).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, response -> Mono.error(new RuntimeException("Nacos认证失败，请检查用户名和密码"))).bodyToMono(String.class).doOnNext(response -> log.info("获取命名空间 [{}] 配置成功，响应: {}", namespace, response)).doOnError(error -> log.error("获取命名空间 [{}] 配置失败", namespace, error)).onErrorMap(WebClientResponseException.class, ex -> new RuntimeException("调用Nacos配置API失败: " + ex.getMessage(), ex)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).filter(throwable -> !(throwable instanceof RuntimeException)));
    }

    @Tool(description = "获取Nacos指定配置的详细内容")
    public Mono<String> getNacosConfig(@ToolParam(description = "配置的DataId") String dataId, @ToolParam(description = "配置的Group，默认为DEFAULT_GROUP") String group, @ToolParam(description = "命名空间ID，为空则查询public命名空间") String namespace) {
        log.info("开始获取配置详情 - dataId: [{}], group: [{}], namespace: [{}]", dataId, group, namespace);

        if (!StringUtils.hasText(dataId)) {
            return Mono.error(new IllegalArgumentException("dataId不能为空"));
        }

        String finalGroup = StringUtils.hasText(group) ? group : "DEFAULT_GROUP";
        String finalNamespace = StringUtils.hasText(namespace) ? namespace : "";

        return webClient.get().uri(uriBuilder -> uriBuilder.path("/v1/cs/configs").queryParam("dataId", dataId).queryParam("group", finalGroup).queryParam("tenant", finalNamespace).build()).headers(headers -> headers.setBasicAuth(username, password)).retrieve().onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new RuntimeException("配置不存在：" + dataId))).onStatus(HttpStatus.UNAUTHORIZED::equals, response -> Mono.error(new RuntimeException("Nacos认证失败，请检查用户名和密码"))).bodyToMono(String.class).doOnNext(response -> log.info("获取配置 [{}] 成功，内容长度: {}", dataId, response.length())).doOnError(error -> log.error("获取配置 [{}] 失败", dataId, error)).onErrorMap(WebClientResponseException.class, ex -> new RuntimeException("调用Nacos配置API失败: " + ex.getMessage(), ex)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).filter(throwable -> !(throwable instanceof RuntimeException)));
    }

    /**
     * 获取服务的实例详情
     * @param serviceName 服务名称
     */
    @Tool(description = "根据服务名称获取Nacos服务名称下注册的实例详情")
    public String getServiceInstances(@ToolParam(description = "服务名称，根据服务名称获取其下面的实例") String serviceName) {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", "192.168.1.242:8848");
            properties.setProperty("namespace", "");
            properties.setProperty("username", "nacos");
            properties.setProperty("password", "nacos");

            NamingService namingService = NacosFactory.createNamingService(properties);

            // 获取所有健康的实例
            List<Instance> instances = namingService.getAllInstances(serviceName);

            String instanceJSON = JSON.toJSONString(instances);

            log.debug("获取服务{}下的实例信息：{}", serviceName, instanceJSON);

            return instanceJSON;
        } catch (NacosException e) {
            throw new RuntimeException("获取服务实例失败: " + e.getMessage(), e);
        }
    }


    @Tool(description = "获取Nacos指定服务的实例详情")
    public String getNacosServiceInstances(@ToolParam(description = "服务名称") String serviceName, @ToolParam(description = "服务分组，默认为DEFAULT_GROUP") String groupName, @ToolParam(description = "命名空间ID，为空则查询public命名空间") String namespace) {
        log.info("开始获取服务实例 - serviceName: [{}], groupName: [{}], namespace: [{}]", serviceName, groupName, namespace);

        if (!StringUtils.hasText(serviceName)) {
            return "调用失败: serviceName不能为空";
        }

        String finalGroupName = StringUtils.hasText(groupName) ? groupName : "DEFAULT_GROUP";
        String finalNamespace = StringUtils.hasText(namespace) ? namespace : "public";

        try {
            String response = webClient.get().uri(uriBuilder -> uriBuilder.path("/v1/ns/instance/list").queryParam("serviceName", serviceName).queryParam("groupName", finalGroupName).queryParam("namespaceId", finalNamespace).queryParam("healthyOnly", false).build()).headers(headers -> headers.setBasicAuth(username, password)).retrieve().onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new RuntimeException("服务不存在：" + serviceName))).onStatus(HttpStatus.UNAUTHORIZED::equals, resp -> Mono.error(new RuntimeException("Nacos认证失败，请检查用户名和密码"))).bodyToMono(String.class).doOnNext(res -> log.info("获取服务 [{}] 实例成功，响应: {}", serviceName, res)).doOnError(err -> log.error("获取服务 [{}] 实例失败", serviceName, err)).onErrorMap(WebClientResponseException.class, ex -> new RuntimeException("调用Nacos服务实例API失败: " + ex.getMessage(), ex)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).filter(throwable -> !(throwable instanceof RuntimeException))).block(); // 阻塞执行

            return response;
        } catch (Exception e) {
            log.error("调用 Nacos 服务实例API异常: {}", e.getMessage(), e);
            return "调用失败: " + e.getMessage();
        }
    }


    @Tool(description = "获取Nacos集群节点状态信息")
    public Mono<String> getNacosClusterNodes() {
        log.info("开始获取Nacos集群节点状态");
        return webClient.get().uri("/v1/ns/operator/cluster/nodes").headers(headers -> headers.setBasicAuth(username, password)).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, response -> Mono.error(new RuntimeException("Nacos认证失败，请检查用户名和密码"))).bodyToMono(String.class).doOnNext(response -> log.info("获取集群节点状态成功，响应: {}", response)).doOnError(error -> log.error("获取集群节点状态失败", error)).onErrorMap(WebClientResponseException.class, ex -> new RuntimeException("调用Nacos集群API失败: " + ex.getMessage(), ex)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).filter(throwable -> !(throwable instanceof RuntimeException)));
    }

    @Tool(description = "搜索Nacos配置，支持模糊匹配")
    public Mono<String> searchNacosConfig(@ToolParam(description = "搜索关键词，支持dataId和group的模糊匹配") String search, @ToolParam(description = "命名空间ID，为空则搜索public命名空间") String namespace) {
        log.info("开始搜索配置 - 关键词: [{}], namespace: [{}]", search, namespace);

        if (!StringUtils.hasText(search)) {
            return Mono.error(new IllegalArgumentException("搜索关键词不能为空"));
        }

        String finalNamespace = StringUtils.hasText(namespace) ? namespace : "";

        return webClient.get().uri(uriBuilder -> uriBuilder.path("/v1/cs/configs").queryParam("search", "blur").queryParam("dataId", search).queryParam("group", "").queryParam("appName", "").queryParam("namespaceId", finalNamespace).queryParam("pageNo", 1).queryParam("pageSize", 20).build()).headers(headers -> headers.setBasicAuth(username, password)).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, response -> Mono.error(new RuntimeException("Nacos认证失败，请检查用户名和密码"))).bodyToMono(String.class).doOnNext(response -> log.info("搜索配置 [{}] 成功，响应: {}", search, response)).doOnError(error -> log.error("搜索配置 [{}] 失败", search, error)).onErrorMap(WebClientResponseException.class, ex -> new RuntimeException("调用Nacos配置搜索API失败: " + ex.getMessage(), ex)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).filter(throwable -> !(throwable instanceof RuntimeException)));
    }


}
