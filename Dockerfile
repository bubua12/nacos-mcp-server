FROM eclipse-temurin:21-jre AS runtime

MAINTAINER bubua12

WORKDIR /app

COPY nacos-mcp-server-*.jar app.jar

# 默认环境变量、可被 docker-compose 或 k8s 覆盖
ENV NACOS_SERVER=http://127.0.0.1:8848 \
    NACOS_USERNAME=nacos \
    NACOS_PASSWORD=nacos \
    APP_LOG_LEVEL=INFO \
    HTTP_LOG_LEVEL=INFO \
    JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
