# Docker 期末综合项目实施指南

## 📋 项目概述

本项目要求设计并实现一个基于Docker容器化技术的**电商数据管理系统**，包含：
- 前端服务（Nginx + 静态页面）
- 后端API服务（Spring Boot RESTful API）
- 数据库服务（MySQL）
- 完整的CI/CD流水线

**截止日期：2025-12-29**

---

## 🏗️ 项目架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Network                          │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │  Nginx   │───▶│  Spring Boot │───▶│    MySQL     │       │
│  │ (前端)   │    │   (后端API)  │    │   (数据库)   │       │
│  │ :80      │    │   :8080      │    │   :3306      │       │
│  └──────────┘    └──────────────┘    └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    CI/CD Pipeline                            │
│  Git Push → Jenkins/GitLab CI → Build → Test → Deploy       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 推荐项目目录结构

```
ecommerce-docker-project/
├── frontend/                    # 前端服务
│   ├── Dockerfile
│   ├── nginx.conf
│   └── html/
│       ├── index.html          # 商品列表页
│       └── detail.html         # 商品详情页
├── backend/                     # 后端服务
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│               └── application.yml
├── database/                    # 数据库服务
│   ├── Dockerfile
│   └── init/
│       └── init.sql            # 初始化脚本
├── k8s/                        # Kubernetes配置（拓展）
│   ├── deployment.yaml
│   └── service.yaml
├── monitoring/                  # 监控配置（拓展）
│   └── prometheus.yml
├── docker-compose.yml          # Docker Compose编排文件
├── Jenkinsfile                 # CI/CD流水线配置
├── .gitlab-ci.yml              # GitLab CI配置（可选）
├── README.md                   # 项目文档
└── docs/
    ├── architecture.md         # 架构说明
    ├── deployment.md           # 部署指南
    └── troubleshooting.md      # 故障排查
```

---

## 🔧 第一部分：容器化服务架构（30分）

### 1.1 前端服务 - Nginx（10分）

#### Dockerfile（多阶段构建 + Alpine优化）

```dockerfile
# frontend/Dockerfile
# 阶段1：构建阶段（如果有前端构建需求）
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# 阶段2：生产阶段
FROM nginx:1.25-alpine

# 复制自定义Nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制静态文件
COPY --from=builder /app/dist /usr/share/nginx/html
# 或直接复制静态HTML（如果没有构建步骤）
# COPY html/ /usr/share/nginx/html/

# 健康检查配置（重要！2分）
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/health || exit 1

# 暴露端口
EXPOSE 80

# 以非root用户运行（安全最佳实践）
RUN chown -R nginx:nginx /usr/share/nginx/html
USER nginx

CMD ["nginx", "-g", "daemon off;"]
```

#### Nginx配置文件

```nginx
# frontend/nginx.conf
server {
    listen 80;
    server_name localhost;
    
    # 健康检查端点
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
    
    # 静态文件服务
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到后端服务
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_read_timeout 30s;
    }
    
    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

#### 前端页面示例

```html
<!-- frontend/html/index.html -->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>电商数据管理系统 - 商品列表</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        .header { background: #1890ff; color: white; padding: 20px; margin-bottom: 20px; }
        .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
        .product-card { background: white; border-radius: 8px; padding: 15px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .product-card img { width: 100%; height: 200px; object-fit: cover; border-radius: 4px; }
        .product-name { font-size: 16px; margin: 10px 0; }
        .product-price { color: #f5222d; font-size: 20px; font-weight: bold; }
        .btn { background: #1890ff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
        .btn:hover { background: #40a9ff; }
    </style>
</head>
<body>
    <div class="header">
        <div class="container">
            <h1>🛒 电商数据管理系统</h1>
        </div>
    </div>
    <div class="container">
        <div id="product-list" class="product-grid"></div>
    </div>
    <script>
        // 从后端API获取商品数据
        async function loadProducts() {
            try {
                const response = await fetch('/api/products');
                const products = await response.json();
                renderProducts(products);
            } catch (error) {
                console.error('加载商品失败:', error);
                document.getElementById('product-list').innerHTML = '<p>加载失败，请稍后重试</p>';
            }
        }
        
        function renderProducts(products) {
            const html = products.map(p => `
                <div class="product-card">
                    <img src="${p.imageUrl || 'https://via.placeholder.com/280x200'}" alt="${p.name}">
                    <h3 class="product-name">${p.name}</h3>
                    <p class="product-price">¥${p.price.toFixed(2)}</p>
                    <p>${p.description || ''}</p>
                    <button class="btn" onclick="viewDetail(${p.id})">查看详情</button>
                </div>
            `).join('');
            document.getElementById('product-list').innerHTML = html;
        }
        
        function viewDetail(id) {
            window.location.href = `/detail.html?id=${id}`;
        }
        
        loadProducts();
    </script>
</body>
</html>
```

### 1.2 后端API服务 - Spring Boot（10分）

#### Dockerfile（多阶段构建 - 重要！3分）

```dockerfile
# backend/Dockerfile
# ============================================
# 阶段1：依赖下载阶段（利用Docker缓存）
# ============================================
FROM eclipse-temurin:21-jdk-jammy AS deps
WORKDIR /build

# 复制Maven Wrapper
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

# 下载依赖（利用缓存加速构建）
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

# ============================================
# 阶段2：构建阶段
# ============================================
FROM deps AS package
WORKDIR /build

COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/*.jar target/app.jar

# ============================================
# 阶段3：提取Spring Boot分层（优化镜像大小）
# ============================================
FROM package AS extract
WORKDIR /build
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# ============================================
# 阶段4：生产运行阶段（最小化镜像）
# ============================================
FROM eclipse-temurin:21-jre-jammy AS final

# 创建非root用户（安全最佳实践）
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

USER appuser
WORKDIR /app

# 按变化频率复制分层（优化缓存）
COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM优化参数
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

#### pom.xml 关键配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
    </parent>
    
    <groupId>com.ecommerce</groupId>
    <artifactId>ecommerce-api</artifactId>
    <version>1.0.0</version>
    <name>E-Commerce API</name>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Actuator（健康检查） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- H2数据库（测试用） -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                    <!-- 启用分层JAR -->
                    <layers>
                        <enabled>true</enabled>
                    </layers>
                </configuration>
            </plugin>
            <!-- JaCoCo测试覆盖率 -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml（环境变量管理 - 重要！2分）

```yaml
# backend/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: ecommerce-api
  
  # 数据源配置（使用环境变量，避免硬编码）
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ecommerce}?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:root123}
    driver-class-name: com.mysql.cj.jdbc.Driver
    
    # HikariCP连接池优化
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      pool-name: EcommerceHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: ${SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

# Actuator配置（健康检查）
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# 日志配置
logging:
  level:
    com.ecommerce: ${LOG_LEVEL:INFO}
    org.springframework: WARN
```

#### Spring Boot 核心代码

```java
// backend/src/main/java/com/ecommerce/EcommerceApplication.java
package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```

```java
// backend/src/main/java/com/ecommerce/entity/Product.java
package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 500)
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Min(value = 0, message = "库存不能为负数")
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Column(length = 255)
    private String imageUrl;
    
    @Column(length = 50)
    private String category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

```java
// backend/src/main/java/com/ecommerce/repository/ProductRepository.java
package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContaining(String name);
}
```

```java
// backend/src/main/java/com/ecommerce/service/ProductService.java
package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + id));
    }
    
    public Product create(Product product) {
        return productRepository.save(product);
    }
    
    public Product update(Long id, Product product) {
        Product existing = findById(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        existing.setCategory(product.getCategory());
        return productRepository.save(existing);
    }
    
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
    
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}
```

```java
// backend/src/main/java/com/ecommerce/controller/ProductController.java
package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final ProductService productService;
    
    // 获取所有商品
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }
    
    // 根据ID获取商品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }
    
    // 创建商品
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // 更新商品
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.update(id, product));
    }
    
    // 删除商品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // 按分类查询
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }
}
```

### 1.3 数据库服务 - MySQL（10分）

#### Dockerfile

```dockerfile
# database/Dockerfile
FROM mysql:8.0

# 设置字符集和时区（重要！1分）
ENV MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root123}
ENV MYSQL_DATABASE=${MYSQL_DATABASE:-ecommerce}
ENV MYSQL_USER=${MYSQL_USER:-ecommerce_user}
ENV MYSQL_PASSWORD=${MYSQL_PASSWORD:-ecommerce123}
ENV TZ=Asia/Shanghai

# 复制初始化脚本（自动执行 - 重要！3分）
COPY init/ /docker-entrypoint-initdb.d/

# 复制自定义配置
COPY my.cnf /etc/mysql/conf.d/custom.cnf

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1

EXPOSE 3306
```

#### MySQL配置文件

```ini
# database/my.cnf
[mysqld]
# 字符集配置
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

# 时区配置
default-time-zone='+08:00'

# 性能优化
innodb_buffer_pool_size=256M
innodb_log_file_size=64M
max_connections=200
query_cache_size=0
query_cache_type=0

# 日志配置
slow_query_log=1
slow_query_log_file=/var/log/mysql/slow.log
long_query_time=2

# 安全配置
skip-name-resolve
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4
```

#### 初始化SQL脚本

```sql
-- database/init/01-schema.sql
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ecommerce 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE ecommerce;

-- 创建商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    image_url VARCHAR(255) COMMENT '图片URL',
    category VARCHAR(50) COMMENT '分类',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 创建订单明细表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
```

```sql
-- database/init/02-data.sql
-- 插入测试数据
USE ecommerce;

INSERT INTO products (name, description, price, stock, image_url, category) VALUES
('iPhone 15 Pro', 'Apple最新旗舰手机，A17 Pro芯片', 8999.00, 100, 'https://via.placeholder.com/300x300?text=iPhone15', '手机'),
('MacBook Pro 14', 'M3 Pro芯片，专业级笔记本', 16999.00, 50, 'https://via.placeholder.com/300x300?text=MacBook', '电脑'),
('AirPods Pro 2', '主动降噪无线耳机', 1899.00, 200, 'https://via.placeholder.com/300x300?text=AirPods', '配件'),
('iPad Air', 'M1芯片，轻薄便携', 4799.00, 80, 'https://via.placeholder.com/300x300?text=iPad', '平板'),
('Apple Watch S9', '健康监测智能手表', 3299.00, 150, 'https://via.placeholder.com/300x300?text=Watch', '穿戴'),
('小米14 Ultra', '徕卡影像旗舰', 6499.00, 120, 'https://via.placeholder.com/300x300?text=Mi14', '手机'),
('华为MatePad Pro', '鸿蒙系统平板', 5499.00, 60, 'https://via.placeholder.com/300x300?text=MatePad', '平板'),
('Sony WH-1000XM5', '顶级降噪头戴耳机', 2699.00, 90, 'https://via.placeholder.com/300x300?text=Sony', '配件'),
('ThinkPad X1 Carbon', '商务轻薄本', 12999.00, 40, 'https://via.placeholder.com/300x300?text=ThinkPad', '电脑'),
('Samsung Galaxy S24', '三星旗舰手机', 6999.00, 110, 'https://via.placeholder.com/300x300?text=Samsung', '手机');
```

---

## 🔗 第二部分：容器编排与网络（20分）

### 2.1 Docker Compose编排（15分）

```yaml
# docker-compose.yml
version: '3.8'

services:
  # ==================== 前端服务 ====================
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: ecommerce-frontend
    ports:
      - "80:80"
    depends_on:
      backend:
        condition: service_healthy
    networks:
      - ecommerce-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:80/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 10s
    # 资源限制
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 256M
        reservations:
          cpus: '0.1'
          memory: 64M

  # ==================== 后端服务 ====================
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ecommerce-backend
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=database
      - DB_PORT=3306
      - DB_NAME=ecommerce
      - DB_USER=ecommerce_user
      - DB_PASSWORD=ecommerce123
      - SHOW_SQL=false
      - LOG_LEVEL=INFO
    depends_on:
      database:
        condition: service_healthy
    networks:
      - ecommerce-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.25'
          memory: 512M

  # ==================== 数据库服务 ====================
  database:
    build:
      context: ./database
      dockerfile: Dockerfile
    container_name: ecommerce-database
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=ecommerce
      - MYSQL_USER=ecommerce_user
      - MYSQL_PASSWORD=ecommerce123
      - TZ=Asia/Shanghai
    volumes:
      # 数据持久化（重要！3分）
      - mysql-data:/var/lib/mysql
      # 日志持久化
      - mysql-logs:/var/log/mysql
    networks:
      - ecommerce-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot123"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.25'
          memory: 256M

# ==================== 网络配置（5分） ====================
networks:
  ecommerce-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.28.0.0/16

# ==================== 数据卷配置 ====================
volumes:
  mysql-data:
    driver: local
  mysql-logs:
    driver: local
```

---

## 🚀 第三部分：CI/CD持续集成/持续部署（20分）

### 3.1 Jenkins Pipeline（10分）

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    environment {
        // Docker镜像仓库配置
        DOCKER_REGISTRY = 'your-registry.com'
        IMAGE_NAME = 'ecommerce'
        IMAGE_TAG = "${BUILD_NUMBER}"
        
        // 数据库测试配置
        DB_HOST = 'localhost'
        DB_PORT = '3306'
        DB_NAME = 'ecommerce_test'
    }
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }
    
    stages {
        // ==================== 代码检出 ====================
        stage('Checkout') {
            steps {
                echo '📥 拉取代码...'
                checkout scm
                sh 'git log --oneline -5'
            }
        }
        
        // ==================== 代码质量检查 ====================
        stage('Code Quality') {
            steps {
                echo '🔍 代码质量检查...'
                dir('backend') {
                    sh 'mvn checkstyle:check'
                }
            }
        }
        
        // ==================== 单元测试 ====================
        stage('Unit Test') {
            steps {
                echo '🧪 执行单元测试...'
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    // 发布测试报告
                    junit '**/target/surefire-reports/*.xml'
                    // 发布覆盖率报告
                    jacoco(
                        execPattern: '**/target/*.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/test/**'
                    )
                }
            }
        }
        
        // ==================== 构建应用 ====================
        stage('Build') {
            steps {
                echo '🔨 构建应用...'
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        // ==================== 构建Docker镜像 ====================
        stage('Build Docker Images') {
            steps {
                echo '🐳 构建Docker镜像...'
                
                // 构建前端镜像
                sh """
                    docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}-frontend:${IMAGE_TAG} ./frontend
                    docker tag ${DOCKER_REGISTRY}/${IMAGE_NAME}-frontend:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}-frontend:latest
                """
                
                // 构建后端镜像
                sh """
                    docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}-backend:${IMAGE_TAG} ./backend
                    docker tag ${DOCKER_REGISTRY}/${IMAGE_NAME}-backend:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}-backend:latest
                """
            }
        }
        
        // ==================== 集成测试 ====================
        stage('Integration Test') {
            steps {
                echo '🔗 执行集成测试...'
                sh '''
                    # 启动测试环境
                    docker-compose -f docker-compose.test.yml up -d
                    
                    # 等待服务就绪
                    sleep 30
                    
                    # 执行集成测试
                    curl -f http://localhost:8080/actuator/health || exit 1
                    curl -f http://localhost:80/health || exit 1
                    
                    # API测试
                    curl -f http://localhost:8080/api/products || exit 1
                '''
            }
            post {
                always {
                    sh 'docker-compose -f docker-compose.test.yml down -v'
                }
            }
        }
        
        // ==================== 推送镜像 ====================
        stage('Push Images') {
            steps {
                echo '📤 推送镜像到仓库...'
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo $DOCKER_PASS | docker login ${DOCKER_REGISTRY} -u $DOCKER_USER --password-stdin
                        
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}-frontend:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}-frontend:latest
                        
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}-backend:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}-backend:latest
                    '''
                }
            }
        }
        
        // ==================== 部署 ====================
        stage('Deploy') {
            steps {
                echo '🚀 部署应用...'
                sh '''
                    # 停止旧容器
                    docker-compose down || true
                    
                    # 拉取最新镜像
                    docker-compose pull
                    
                    # 启动新容器
                    docker-compose up -d
                    
                    # 验证部署
                    sleep 30
                    curl -f http://localhost:80/health || exit 1
                '''
            }
        }
    }
    
    post {
        success {
            echo '✅ 流水线执行成功！'
            // 可以添加通知，如邮件、钉钉等
        }
        failure {
            echo '❌ 流水线执行失败！'
        }
        always {
            // 清理工作空间
            cleanWs()
            // 清理Docker资源
            sh 'docker system prune -f'
        }
    }
}
```

### 3.2 GitLab CI配置（可选替代方案）

```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - push
  - deploy

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: "/certs"
  IMAGE_NAME: ecommerce
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"

cache:
  paths:
    - .m2/repository/
    - node_modules/

# ==================== 测试阶段 ====================
unit-test:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - cd backend
    - mvn test
  artifacts:
    when: always
    reports:
      junit: backend/target/surefire-reports/*.xml
    paths:
      - backend/target/site/jacoco/
  coverage: '/Total.*?([0-9]{1,3})%/'

# ==================== 构建阶段 ====================
build-backend:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker build -t $CI_REGISTRY_IMAGE/backend:$CI_COMMIT_SHA ./backend
    - docker tag $CI_REGISTRY_IMAGE/backend:$CI_COMMIT_SHA $CI_REGISTRY_IMAGE/backend:latest
  only:
    - main
    - develop

build-frontend:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker build -t $CI_REGISTRY_IMAGE/frontend:$CI_COMMIT_SHA ./frontend
    - docker tag $CI_REGISTRY_IMAGE/frontend:$CI_COMMIT_SHA $CI_REGISTRY_IMAGE/frontend:latest
  only:
    - main
    - develop

# ==================== 推送阶段 ====================
push-images:
  stage: push
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker push $CI_REGISTRY_IMAGE/backend:$CI_COMMIT_SHA
    - docker push $CI_REGISTRY_IMAGE/backend:latest
    - docker push $CI_REGISTRY_IMAGE/frontend:$CI_COMMIT_SHA
    - docker push $CI_REGISTRY_IMAGE/frontend:latest
  only:
    - main

# ==================== 部署阶段 ====================
deploy-production:
  stage: deploy
  image: alpine:latest
  script:
    - apk add --no-cache curl
    - curl -X POST $DEPLOY_WEBHOOK_URL
  only:
    - main
  when: manual
```

### 3.3 自动化测试（10分）

#### 单元测试示例

```java
// backend/src/test/java/com/ecommerce/service/ProductServiceTest.java
package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务单元测试")
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1L)
            .name("测试商品")
            .description("测试描述")
            .price(new BigDecimal("99.99"))
            .stock(100)
            .category("测试分类")
            .build();
    }
    
    @Test
    @DisplayName("获取所有商品 - 成功")
    void findAll_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);
        
        // When
        List<Product> result = productService.findAll();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("测试商品");
        verify(productRepository, times(1)).findAll();
    }
    
    @Test
    @DisplayName("根据ID获取商品 - 成功")
    void findById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When
        Product result = productService.findById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("根据ID获取商品 - 商品不存在")
    void findById_WhenProductNotExists_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.findById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("商品不存在");
    }
    
    @Test
    @DisplayName("创建商品 - 成功")
    void create_ShouldSaveAndReturnProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        Product result = productService.create(testProduct);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(testProduct);
    }
    
    @Test
    @DisplayName("删除商品 - 成功")
    void delete_ShouldCallRepositoryDelete() {
        // Given
        Long productId = 1L;
        doNothing().when(productRepository).deleteById(productId);
        
        // When
        productService.delete(productId);
        
        // Then
        verify(productRepository, times(1)).deleteById(productId);
    }
}
```

#### 集成测试示例

```java
// backend/src/test/java/com/ecommerce/controller/ProductControllerIntegrationTest.java
package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("商品API集成测试")
class ProductControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }
    
    @Test
    @DisplayName("GET /api/products - 获取商品列表")
    void getAllProducts_ShouldReturnProductList() throws Exception {
        // Given
        Product product = createTestProduct();
        productRepository.save(product);
        
        // When & Then
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].name").value("测试商品"));
    }
    
    @Test
    @DisplayName("POST /api/products - 创建商品")
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        // Given
        Product product = createTestProduct();
        
        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("测试商品"));
    }
    
    private Product createTestProduct() {
        return Product.builder()
            .name("测试商品")
            .description("测试描述")
            .price(new BigDecimal("99.99"))
            .stock(100)
            .category("测试分类")
            .build();
    }
}
```

---

## 🌟 第四部分：拓展高级功能（10分）

### 4.1 Kubernetes编排（4分）

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ecommerce
---
# k8s/mysql-deployment.yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-secret
  namespace: ecommerce
type: Opaque
data:
  root-password: cm9vdDEyMw==  # base64编码的 root123
  user-password: ZWNvbW1lcmNlMTIz  # base64编码的 ecommerce123
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: ecommerce
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: ecommerce
  labels:
    app: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          ports:
            - containerPort: 3306
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: root-password
            - name: MYSQL_DATABASE
              value: ecommerce
            - name: MYSQL_USER
              value: ecommerce_user
            - name: MYSQL_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: user-password
          volumeMounts:
            - name: mysql-storage
              mountPath: /var/lib/mysql
          resources:
            limits:
              cpu: "1"
              memory: "1Gi"
            requests:
              cpu: "250m"
              memory: "256Mi"
          livenessProbe:
            exec:
              command: ["mysqladmin", "ping", "-h", "localhost"]
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            exec:
              command: ["mysqladmin", "ping", "-h", "localhost"]
            initialDelaySeconds: 5
            periodSeconds: 5
      volumes:
        - name: mysql-storage
          persistentVolumeClaim:
            claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: ecommerce
spec:
  selector:
    app: mysql
  ports:
    - port: 3306
      targetPort: 3306
  type: ClusterIP
```

```yaml
# k8s/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: ecommerce
  labels:
    app: backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: your-registry/ecommerce-backend:latest
          ports:
            - containerPort: 8080
          env:
            - name: DB_HOST
              value: mysql
            - name: DB_PORT
              value: "3306"
            - name: DB_NAME
              value: ecommerce
            - name: DB_USER
              value: ecommerce_user
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: user-password
          resources:
            limits:
              cpu: "1"
              memory: "1Gi"
            requests:
              cpu: "250m"
              memory: "512Mi"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: backend
  namespace: ecommerce
spec:
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
  type: ClusterIP
```

```yaml
# k8s/frontend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: ecommerce
  labels:
    app: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: frontend
          image: your-registry/ecommerce-frontend:latest
          ports:
            - containerPort: 80
          resources:
            limits:
              cpu: "500m"
              memory: "256Mi"
            requests:
              cpu: "100m"
              memory: "64Mi"
          livenessProbe:
            httpGet:
              path: /health
              port: 80
            initialDelaySeconds: 10
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /health
              port: 80
            initialDelaySeconds: 5
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: frontend
  namespace: ecommerce
spec:
  selector:
    app: frontend
  ports:
    - port: 80
      targetPort: 80
  type: LoadBalancer
```

### 4.2 蓝绿部署/金丝雀发布（3分）

```yaml
# k8s/canary-deployment.yaml
# 金丝雀发布示例 - 新版本部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-canary
  namespace: ecommerce
  labels:
    app: backend
    version: canary
spec:
  replicas: 1  # 金丝雀只部署少量副本
  selector:
    matchLabels:
      app: backend
      version: canary
  template:
    metadata:
      labels:
        app: backend
        version: canary
    spec:
      containers:
        - name: backend
          image: your-registry/ecommerce-backend:v2.0.0  # 新版本
          ports:
            - containerPort: 8080
          env:
            - name: DB_HOST
              value: mysql
            - name: DB_PORT
              value: "3306"
            - name: DB_NAME
              value: ecommerce
            - name: DB_USER
              value: ecommerce_user
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: user-password
---
# 使用Service选择器同时路由到稳定版和金丝雀版
apiVersion: v1
kind: Service
metadata:
  name: backend
  namespace: ecommerce
spec:
  selector:
    app: backend  # 同时匹配stable和canary
  ports:
    - port: 8080
      targetPort: 8080
```

### 4.3 APM监控配置 - Prometheus + Grafana（3分）

```yaml
# monitoring/docker-compose.monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - ecommerce-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      - grafana-data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - ecommerce-network
    restart: unless-stopped

volumes:
  prometheus-data:
  grafana-data:

networks:
  ecommerce-network:
    external: true
```

```yaml
# monitoring/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'spring-boot-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
    scrape_interval: 5s

  - job_name: 'nginx-frontend'
    static_configs:
      - targets: ['frontend:80']
```

#### 后端添加Prometheus支持

```xml
<!-- 在pom.xml中添加 -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
# application.yml 添加
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 📚 第五部分：文档与代码质量（20分）

### 5.1 README.md 模板

```markdown
# 🛒 电商数据管理系统

基于Docker容器化技术的电商数据管理系统，包含前端、后端API和数据库服务。

## 📋 项目概述

| 组件 | 技术栈 | 端口 |
|------|--------|------|
| 前端 | Nginx + HTML/CSS/JS | 80 |
| 后端 | Spring Boot 3.2 + JDK 21 | 8080 |
| 数据库 | MySQL 8.0 | 3306 |

## 🚀 快速开始

### 前置条件
- Docker 24.0+
- Docker Compose 2.20+
- Git

### 一键启动
\`\`\`bash
# 克隆项目
git clone https://github.com/your-repo/ecommerce-docker.git
cd ecommerce-docker

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
\`\`\`

### 访问地址
- 前端页面: http://localhost
- 后端API: http://localhost:8080/api/products
- 健康检查: http://localhost:8080/actuator/health

## 📁 项目结构
\`\`\`
├── frontend/          # 前端服务
├── backend/           # 后端服务
├── database/          # 数据库服务
├── k8s/              # Kubernetes配置
├── monitoring/        # 监控配置
├── docker-compose.yml
├── Jenkinsfile
└── README.md
\`\`\`

## 🔧 API文档

### 商品接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/products | 获取所有商品 |
| GET | /api/products/{id} | 获取单个商品 |
| POST | /api/products | 创建商品 |
| PUT | /api/products/{id} | 更新商品 |
| DELETE | /api/products/{id} | 删除商品 |

## 👥 团队分工

| 成员 | 负责模块 | 贡献度 |
|------|----------|--------|
| 成员A | 后端API开发、数据库设计 | 40% |
| 成员B | 前端开发、Nginx配置 | 30% |
| 成员C | CI/CD、K8s部署、文档 | 30% |

## 📄 License
MIT License
```

### 5.2 Git协作规范（5分）

#### 分支策略

```
main (生产分支)
  │
  ├── develop (开发分支)
  │     │
  │     ├── feature/frontend-product-list
  │     ├── feature/backend-crud-api
  │     ├── feature/database-init
  │     └── feature/cicd-pipeline
  │
  └── hotfix/xxx (紧急修复)
```

#### Commit规范

```bash
# 格式: <type>(<scope>): <subject>

# 类型(type):
# feat:     新功能
# fix:      修复bug
# docs:     文档更新
# style:    代码格式
# refactor: 重构
# test:     测试
# chore:    构建/工具

# 示例:
git commit -m "feat(backend): 添加商品CRUD接口"
git commit -m "fix(frontend): 修复商品列表加载失败问题"
git commit -m "docs: 更新README部署说明"
git commit -m "chore(docker): 优化Dockerfile多阶段构建"
```

---

## ✅ 评分检查清单

### 容器化服务架构（30分）
- [ ] 前端Nginx使用Alpine镜像（2分）
- [ ] 前端包含商品列表和详情页（3分）
- [ ] 前端Dockerfile多阶段构建（3分）
- [ ] 前端健康检查配置（2分）
- [ ] 后端Spring Boot配置完整（2分）
- [ ] 后端RESTful API设计规范（3分）
- [ ] 后端多阶段构建优化镜像（3分）
- [ ] 后端环境变量管理（2分）
- [ ] MySQL版本选择合理（2分）
- [ ] 数据卷持久化配置（3分）
- [ ] 初始化脚本自动执行（3分）
- [ ] 数据库连接池优化（2分）

### 容器编排与网络（20分）
- [ ] docker-compose.yml完整规范（3分）
- [ ] 服务依赖关系正确（4分）
- [ ] 自定义网络配置（3分）
- [ ] 服务通信链路完整（3分）
- [ ] 版本兼容性（2分）
- [ ] 容器间服务名通信（3分）
- [ ] 端口映射合理（2分）

### CI/CD（20分）
- [ ] Jenkins/GitLab CI配置完整（3分）
- [ ] 构建→测试→部署流程（3分）
- [ ] 代码提交触发机制（2分）
- [ ] 镜像推送仓库（2分）
- [ ] 单元测试覆盖率≥80%（4分）
- [ ] 集成测试验证（3分）
- [ ] 测试报告生成（3分）

### 拓展功能（10分）
- [ ] K8s部署文件完整（2分）
- [ ] K8s实际部署验证（2分）
- [ ] 蓝绿/金丝雀部署策略（3分）
- [ ] APM监控配置（3分）

### 文档+代码质量（20分）
- [ ] 架构图清晰（3分）
- [ ] 部署指南完整（3分）
- [ ] Dockerfile说明（2分）
- [ ] 故障排查文档（2分）
- [ ] 代码规范（2分）
- [ ] Dockerfile最佳实践（2分）
- [ ] 配置文件管理（1分）
- [ ] 分支策略合理（2分）
- [ ] commit信息规范（2分）
- [ ] code review流程（1分）

---

## 🛠️ 常用命令速查

```bash
# Docker Compose
docker-compose up -d              # 后台启动
docker-compose down               # 停止并删除
docker-compose logs -f backend    # 查看后端日志
docker-compose ps                 # 查看状态
docker-compose exec backend sh    # 进入容器

# Docker
docker build -t myapp:v1 .        # 构建镜像
docker images                     # 查看镜像
docker ps -a                      # 查看容器
docker system prune -f            # 清理资源

# Kubernetes
kubectl apply -f k8s/             # 部署
kubectl get pods -n ecommerce     # 查看Pod
kubectl logs -f <pod-name>        # 查看日志
kubectl delete -f k8s/            # 删除

# 测试
mvn test                          # 运行测试
mvn jacoco:report                 # 生成覆盖率报告
```

---

## 📞 故障排查

### 常见问题

1. **数据库连接失败**
   ```bash
   # 检查数据库容器状态
   docker-compose logs database
   # 检查网络连通性
   docker-compose exec backend ping database
   ```

2. **后端启动失败**
   ```bash
   # 查看详细日志
   docker-compose logs -f backend
   # 检查健康状态
   curl http://localhost:8080/actuator/health
   ```

3. **前端无法访问后端API**
   ```bash
   # 检查Nginx配置
   docker-compose exec frontend cat /etc/nginx/conf.d/default.conf
   # 测试后端API
   curl http://localhost:8080/api/products
   ```

---

> 📌 **提示**: 本文档基于最新的Docker、Spring Boot和Kubernetes最佳实践编写，参考了Docker官方文档、Spring Boot官方指南以及GitHub上的优秀开源项目。
