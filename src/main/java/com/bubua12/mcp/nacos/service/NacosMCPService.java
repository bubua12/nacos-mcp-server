package com.bubua12.mcp.nacos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Nacos MCP Service
 *
 * @author bubua12
 * @since 2025/9/17 10:55
 */
@Slf4j
@Service
@SuppressWarnings("unused")
public class NacosMCPService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String username;
    private final String password;

    private String accessToken;
    private Instant tokenExpireAt;

    private final Map<String, String> namespaceCache = new HashMap<>();

    public NacosMCPService(
            @Value("${nacos.server}") String nacosServer,
            @Value("${nacos.username:nacos}") String username,
            @Value("${nacos.password:nacos}") String password) {
        this.username = username;
        this.password = password;
        this.webClient = WebClient.builder()
                .baseUrl(nacosServer)
                .build();
    }

    /**
     * 获取指定配置
     *
     * @param dataId 配置名称
     * @param group 所属分类
     * @param namespace 名称空间
     * @return 详情
     */
    @Tool(description = "获取Nacos指定配置，输入精确的配置名称(dataId)、分组名称(group)、名称空间(namespace)")
    public String getConfig(
            @ToolParam(description = "配置名称，如：service-order.yaml") String dataId,
            @ToolParam(description = "分组名称，如：DEFAULT_GROUP、SEATA_GROUP") String group,
            @ToolParam(description = "名称空间，如：public、dev、prod等") String namespace) {
        log.debug("[获取指定配置] 调用 getConfig 方法，接收参数：dataId: {}，group: {}，namespace: {}", dataId, group, namespace);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/cs/config")
                        .queryParam("dataId", dataId)
                        .queryParam("group", group)
                        .queryParam("namespaceId", getNamespaceId(namespace))
                        .queryParam("accessToken", getToken()) // 你自己写的获取 token 方法
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 获取配置历史
     *
     * @param dataId dataId
     * @param group 所属分类
     * @param namespace 名称空间
     * @return 配置历史
     */
    @Tool(description = "获取Nacos里某个配置的历史，输入配置名称(dataId)、它的所属分组(group)、名称空间(namespace)")
    public String getConfigHistory(
            @ToolParam(description = "配置名称，如：service-order.yaml") String dataId,
            @ToolParam(description = "分组名称，如：DEFAULT_GROUP、SEATA_GROUP") String group,
            @ToolParam(description = "名称空间，如：public、dev、prod等") String namespace
    ) {
        log.debug("[获取配置历史] 调用 getConfigHistory 方法，接收参数：dataId: {}，group: {}，namespace: {}", dataId, group, namespace);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/cs/history/list")
                        .queryParam("dataId", dataId)
                        .queryParam("group", group)
                        .queryParam("namespaceId", getNamespaceId(namespace))
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询指定命名空间下的配置列表
     *
     * @param namespace 名称空间
     * @return 配置列表
     */
    @Tool(description = "查询指定命名空间下的Nacos配置列表，输入名称空间(namespace)，如 public、dev、prod等")
    public String listConfigsByNamespace(@ToolParam(description = "名称空间，如：public、dev、prod等") String namespace) {
        log.debug("[查询指定命名空间下的配置列表] 调用 listConfigsByNamespace 方法，接收参数：namespace: {}", namespace);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/cs/history/configs")
                        .queryParam("namespaceId", getNamespaceId(namespace))
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询服务列表
     *
     * @return 服务列表
     */
    @Tool(description = "查询服务列表，查询当前Nacos里的服务概览")
    public String listServices() {
        log.debug("[查询服务列表] 调用 listServices 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/service/list")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询指定服务的实例列表
     *
     * @param serviceName 服务名，如service-product
     * @return 服务下的实例列表
     */
    @Tool(description = "查询Nacos里指定服务的实例列表，输入服务名(serviceName)查询")
    public String listServiceInstances(@ToolParam(description = "服务名，如ms-gateway、service-product等") String serviceName) {
        log.debug("[查询指定服务的实例列表] 调用 listServiceInstances 方法，接收参数：serviceName: {}", serviceName);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/instance/list")
                        .queryParam("serviceName", serviceName)
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 对服务实例进行上线或者下线
     *
     * @param serviceName 服务名
     * @param group 分组名称
     * @param namespace 名称空间
     * @param ip 实例IP
     * @param port 实例端口
     * @param enabled true=上线, false=下线
     */
    @Tool(description = "对服务的实例进行上线或者下线操作，根据传入的enabled来判断")
    public String updateInstanceStatus(
            @ToolParam(description = "服务名，如ms-gateway、service-product等") String serviceName,
            @ToolParam(description = "分组名称，如：DEFAULT_GROUP、SEATA_GROUP，没有特殊说明则是DEFAULT_GROUP") String group,
            @ToolParam(description = "名称空间，如：public、dev、prod等，没有特殊说明则是public") String namespace,
            @ToolParam(description = "服务实例IP，必填") String ip,
            @ToolParam(description = "服务实例的端口号，必填，结合服务实例IP进行操作") int port,
            @ToolParam(description = "对服务进行上线还是下线，上线则为true，下线则为false") boolean enabled
    ) {
        log.debug("[服务实例上下线] 调用 updateInstanceStatus 方法，接收参数：serviceName: {}，group: {}，namespace: {}，ip: {}，port: {}，enabled: {}",
                serviceName, group, namespace, ip, port, enabled);
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/instance")
                        .queryParam("serviceName", serviceName)
                        .queryParam("group", group)
                        .queryParam("namespace", getNamespaceId(namespace))
                        .queryParam("ip", ip)
                        .queryParam("port", port)
                        .queryParam("enabled", enabled)
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询服务详情
     *
     * @param serviceName 服务维度的详情信息
     * @return 服务详情
     */
    @Tool(description = "根据提供的服务名称查询Nacos里的服务详情")
    public String getServiceDetail(@ToolParam(description = "服务名，如ms-gateway、service-product等") String serviceName) {
        log.debug("[查询服务详情] 调用 getServiceDetail 方法，接收参数：serviceName: {}", serviceName);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/service")
                        .queryParam("serviceName", serviceName)
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询系统当前数据指标
     *
     * @return 系统当前数据指标
     */
    @Tool(description = "查询Nacos目前的数据指标情况")
    public String getSystemMetrics() {
        log.debug("[查询系统当前数据指标] 调用 getSystemMetrics 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/operator/metrics")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询客户端列表
     *
     * @return 客户端列表
     */
    @Tool(description = "查询当前Nacos的客户端列表，显示连接的客户端信息")
    public String listClients() {
        log.debug("[查询客户端列表] 调用 listClients 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/client/list")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询客户端信息
     *
     * @param clientId clientId
     * @return 查询客户端信息
     */
    @Tool(description = "根据提供的客户端ID，进一步查询连接的客户端信息")
    public String getClientInfoByClientID(
            @ToolParam(description = "客户端ID，clientId 格式为time_ip_port，如：1664527081276_127.0.0.1_4400") String clientId) {
        log.debug("[查询客户端信息] 调用 getClientInfoByClientID 方法，接收参数：clientId: {}", clientId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/ns/client")
                        .queryParam("clientId", clientId)
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询命名空间列表
     *
     * @return 命名空间列表
     */
    @Tool(description = "查询Nacos里的命名空间列表")
    public String listNamespaces() {
        log.debug("[查询命名空间列表] 调用 listNamespaces 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/console/namespace/list")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询具体命名空间
     *
     * @param namespace 命名空间名称
     * @return 具体ns详情
     */
    @Tool(description = "根据名称空间，查询Nacos里的具体的命名空间")
    public String getNamespaceDetail(@ToolParam(description = "名称空间，如：public、dev、prod等") String namespace) {
        log.debug("[查询具体命名空间] 调用 getNamespaceDetail 方法，接收参数：namespace: {}", namespace);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/console/namespace")
                        .queryParam("namespaceId", getNamespaceId(namespace))
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询当前节点信息
     *
     * @return 当前节点信息
     */
    @Tool(description = "根据名称空间，查询Nacos里的具体的命名空间")
    public String getCurrentNodeInfo() {
        log.debug("[查询当前节点信息] 调用 getCurrentNodeInfo 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/core/cluster/node/self")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询集群节点列表
     *
     * @return 集群节点列表
     */
    @Tool(description = "查询Nacos集群节点列表")
    public String listClusterNodes() {
        log.debug("[查询Nacos集群节点列表] 调用 listClusterNodes 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/core/cluster/node/list")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    /**
     * 查询当前节点健康状态
     *
     * @return 节点健康状态
     */
    @Tool(description = "查询当前节点健康状态")
    public String getCurrentNodeHealth() {
        log.debug("[查询当前节点健康状态] 调用 getCurrentNodeHealth 方法");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nacos/v2/core/cluster/node/self/health")
                        .queryParam("accessToken", getToken())
                        .build())
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> "HTTP " + response.statusCode() + "\n" + body))
                .block();
    }


    @PostConstruct
    public void init() {
        login();
        refreshNamespaces();
    }

    /**
     * 登录获取 token
     */
    private synchronized void login() {
        try {
            String resp = webClient.post()
                    .uri("/nacos/v1/auth/login")
                    .body(BodyInserters.fromFormData("username", username)
                            .with("password", password))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(resp);
            this.accessToken = node.get("accessToken").asText();
            long ttl = node.get("tokenTtl").asLong();
            this.tokenExpireAt = Instant.now().plusSeconds(ttl - 60);
            log.info("登录 Nacos 成功，token 有效期 {} 秒", ttl);
        } catch (Exception e) {
            log.error("登录 Nacos 失败", e);
            throw new RuntimeException("Login Nacos failed", e);
        }
    }

    /**
     * 获取 token，自动刷新
     */
    private String getToken() {
        if (this.accessToken == null || Instant.now().isAfter(tokenExpireAt)) {
            login();
        }
        return this.accessToken;
    }

    /**
     * 刷新 namespace 缓存
     */
    private void refreshNamespaces() {
        try {
            String resp = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/nacos/v1/console/namespaces")
                            .queryParam("accessToken", getToken())
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(resp);
            JsonNode data = root.get("data");
            if (data != null && data.isArray()) {
                namespaceCache.clear();
                Iterator<JsonNode> it = data.elements();
                while (it.hasNext()) {
                    JsonNode ns = it.next();
                    String nsId = ns.get("namespace").asText();
                    String nsName = ns.get("namespaceShowName").asText();
                    namespaceCache.put(nsName, nsId);
                }
                namespaceCache.put("public", ""); // public 特殊处理
                log.info("✅ 已刷新 namespace 缓存: {}", namespaceCache);
            }
        } catch (Exception e) {
            log.error("刷新 namespace 缓存失败", e);
        }
    }


    /**
     * 根据 namespace 名称获取 namespaceId
     *
     * @param namespaceName namespace名称
     * @return namespaceId
     */
    private String getNamespaceId(String namespaceName) {
        if (!StringUtils.hasText(namespaceName) || "public".equalsIgnoreCase(namespaceName)) {
            return null;
        }
        String nsId = namespaceCache.get(namespaceName);
        if (nsId == null) {
            refreshNamespaces();
            nsId = namespaceCache.get(namespaceName);
        }
        if (nsId == null) {
            throw new IllegalArgumentException("未知 namespace 名称: " + namespaceName);
        }
        return nsId;
    }
}