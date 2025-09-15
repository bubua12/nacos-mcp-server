# 🚀 Nacos MCP 服务器

<img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java Version">
<img src="https://img.shields.io/badge/Spring%20Boot-3.4.4-green.svg" alt="Spring Boot">
<img src="https://img.shields.io/badge/Spring%20AI-1.0.2-brightgreen.svg" alt="Spring AI">
<img src="https://img.shields.io/badge/Nacos-2.4.3-blue.svg" alt="Nacos">
<img src="https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg" alt="Platform">
<img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">


**专为与 Nacos 集群无缝交互和自动化而设计的强大 MCP（模型上下文协议）服务器**

*赋能大模型搜索、读取和管理 Nacos 资源，包括命名空间、服务、配置和集群节点。*


---

## ✨ 功能特性

### 📝 配置管理
- 🗂️ **命名空间列表** - 列出所有可用的命名空间
- 📋 **配置详情** - 查看指定命名空间下的详细配置信息
- 🔍 **模糊查询** - 根据名称模式搜索配置
- 🔎 **高级搜索** - 在集群中查找相关配置

### 🎯 服务管理
- 📡 **服务发现** - 列出指定命名空间下的在线服务
- 🔍 **服务搜索** - 高级服务搜索功能
- 📊 **服务详情** - 查看注册 IP、元数据和服务信息
- 🌐 **命名空间管理** - 全面的命名空间操作
- ⚡ **服务状态** - 监控服务上下线状态，支持条件过滤

### 🖥️ 节点管理
- 🏗️ **集群状态** - 实时集群节点状态监控
- 📈 **健康检查** - 监控节点健康状况和可用性
- 🔧 **节点信息** - 详细的节点元数据和配置

---

## 🏗️ 架构设计

本项目基于 **Spring AI MCP Server** 和 **WebFlux** 响应式编程，提供：

- **🔄 响应式架构** - 非阻塞 I/O 操作，性能更优
- **🤖 AI 集成** - 内置大型语言模型交互支持
- **🌐 RESTful APIs** - 轻松集成 Nacos REST 端点
- **🔧 工具化设计** - 模块化工具系统，功能可扩展

---

## 🚀 快速开始

### 环境要求

- ☕ **Java 21** 或更高版本
- 🐳 **Nacos Server** (推荐 2.4.3+ 版本)
- 🛠️ **Maven 3.6+**

### 安装部署

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/nacos-mcp-server.git
   cd nacos-mcp-server
   ```

2. **配置 Nacos 连接**
   
   编辑 `src/main/resources/application.yaml`:
   ```yaml
   nacos:
     server: http://your-nacos-server:8848
   ```

3. **构建和运行**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **访问 MCP 服务器**
   
   服务器将在默认端口启动，SSE 端点地址为 `/sse`

---

## ⚙️ 配置说明

### 应用配置

```yaml
spring:
  application:
    name: nacos-mcp-server
  ai:
    mcp:
      server:
        name: nacos-mcp-server
        version: 0.0.1
        sse-endpoint: /sse
        instructions: "Nacos MCP AI Tools"

nacos:
  server: http://192.168.1.242:8848
```

### 环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `NACOS_SERVER` | Nacos 服务器地址 | `http://localhost:8848` |
| `NACOS_USERNAME` | Nacos 用户名 | `nacos` |
| `NACOS_PASSWORD` | Nacos 密码 | `nacos` |

---

## 🛠️ 可用工具

### 📋 命名空间操作
- `getNacosNamespaces()` - 获取所有可用的命名空间

### 📄 配置操作  
- `listNacosConfigByNamespace(namespace)` - 列出指定命名空间中的配置

*更多工具正在持续添加中，敬请期待！*

---

## 📊 API 示例

### 获取所有命名空间
```http
GET /nacos/v1/console/namespaces
Authorization: Basic bmFjb3M6bmFjb3M=
```

### 根据命名空间列出配置
```http
GET /nacos/v1/cs/configs?namespaceId={namespace}&pageNo=1&pageSize=10
Authorization: Bearer {accessToken}
```

---

## 🏢 项目结构

```
nacos-mcp-server/
├── src/main/java/com/bubua12/mcp/nacos/
│   ├── config/
│   │   └── ToosConfig.java              # 工具配置
│   ├── service/
│   │   └── NacosMetaService.java        # 核心 Nacos 操作
│   └── NacosWebfluxMCPServerApplication.java  # 主应用程序
├── src/main/resources/
│   └── application.yaml                 # 应用配置
└── pom.xml                             # Maven 依赖
```

---

## 🔧 开发指南

### 添加新工具

1. 在 `NacosMetaService` 中创建新方法
2. 使用 `@Tool` 注解并提供描述
3. 使用 `@ToolParam` 进行参数文档化
4. 在 `ToosConfig` 中注册工具

示例：
```java
@Tool(description = "根据名称获取服务详情")
public Mono<String> getServiceDetails(
    @ToolParam(description = "服务名称") String serviceName,
    @ToolParam(description = "命名空间 ID") String namespace) {
    // 实现代码
}
```

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建您的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

---

## 📄 许可证

本项目基于 MIT 许可证 - 详细信息请查看 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

- **Nacos 社区** - 提供优秀的服务发现和配置管理平台
- **Spring AI 团队** - 提供创新的 MCP Server 框架
- **阿里云** - 提供强大的 Nacos 生态系统

---

## 📞 技术支持

- 📧 **邮箱**: bubua12@example.com
- 🐛 **问题反馈**: [GitHub Issues](https://github.com/your-username/nacos-mcp-server/issues)
- 💬 **讨论交流**: [GitHub Discussions](https://github.com/your-username/nacos-mcp-server/discussions)

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给个 Star！**

由 [bubua12](https://github.com/bubua12) 制作

</div>