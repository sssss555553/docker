# 架构说明

## 🏗️ 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Network (ecommerce-network)            │
│                         172.28.0.0/16                            │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │   Nginx      │───▶│  Spring Boot │───▶│    MySQL     │       │
│  │  (frontend)  │    │   (backend)  │    │  (database)  │       │
│  │   :80        │    │    :8080     │    │    :3306     │       │
│  │              │    │              │    │              │       │
│  │ 静态页面服务  │    │  RESTful API │    │   数据存储   │       │
│  └──────────────┘    └──────────────┘    └──────────────┘       │
│         │                   │                   │                │
│         │                   │                   │                │
│    健康检查:            健康检查:           健康检查:            │
│    /health          /actuator/health    mysqladmin ping         │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────┐
    │  用户   │
    │ :80     │
    └─────────┘
```

## 📦 容器服务说明

### Frontend (Nginx)
- 基础镜像: `nginx:1.25-alpine`
- 功能: 静态页面服务、API反向代理
- 端口: 80
- 健康检查: `curl -fs http://localhost:80/health`

### Backend (Spring Boot)
- 基础镜像: `eclipse-temurin:21-jre-jammy`
- 功能: RESTful API服务
- 端口: 8080
- 健康检查: `curl -f http://localhost:8080/actuator/health`

### Database (MySQL)
- 基础镜像: `mysql:8.0`
- 功能: 数据持久化存储
- 端口: 3306
- 健康检查: `mysqladmin ping`

## 🔗 网络配置

| 配置项 | 值 |
|--------|-----|
| 网络名称 | ecommerce-network |
| 驱动类型 | bridge |
| 子网 | 172.28.0.0/16 |
| 网关 | 172.28.0.1 |

## 💾 数据卷配置

| 卷名称 | 挂载路径 | 用途 |
|--------|----------|------|
| ecommerce-mysql-data | /var/lib/mysql | MySQL数据文件 |
| ecommerce-mysql-logs | /var/log/mysql | MySQL日志文件 |

## 🔒 安全配置

1. 所有容器使用非root用户运行
2. 敏感信息通过环境变量管理
3. 网络隔离，仅暴露必要端口
4. 健康检查确保服务可用性
