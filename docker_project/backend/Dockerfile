# ============================================
# 后端服务 Dockerfile - Spring Boot RESTful API
# 多阶段构建 + 镜像优化（重要！3分）
# 使用官方Docker Hub镜像
# ============================================

# ============================================
# 阶段1：构建阶段（使用Maven镜像）
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 复制pom.xml并下载依赖（利用缓存加速构建）
COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests

# 复制源代码并构建
COPY src/ src/
RUN mvn package -DskipTests && \
    mv target/*.jar target/app.jar

# ============================================
# 阶段2：提取Spring Boot分层（优化镜像大小）
# ============================================
FROM build AS extract
WORKDIR /build
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# ============================================
# 阶段3：生产运行阶段（Alpine最小化镜像）
# ============================================
FROM eclipse-temurin:21-jre-alpine AS final

LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Backend API Service"

# 安装curl用于健康检查，创建非root用户
RUN apk add --no-cache curl && \
    addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D -s /sbin/nologin appuser

USER appuser
WORKDIR /app

# 按变化频率复制分层（优化缓存）
COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

# 健康检查配置
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM优化参数
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+UseStringDeduplication"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
