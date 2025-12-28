# 🛒 电商数据管理系统

基于Docker容器化技术的电商数据管理系统，包含前端服务、后端API服务、数据库服务，以及完整的CI/CD流水线。

## 📋 项目概述

| 组件 | 技术栈 | 端口 |
|------|--------|------|
| 前端服务 | Nginx 1.25 Alpine | 80 |
| 后端服务 | Spring Boot 3.2 + JDK 21 | 8080 |
| 数据库 | MySQL 8.0 | 3306 |

## 🏗️ 项目架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network                            │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │  Nginx   │───▶│  Spring Boot │───▶│    MySQL     │       │
│  │ (前端)   │    │   (后端API)  │    │   (数据库)   │       │
│  │ :80      │    │   :8080      │    │   :3306      │       │
│  └──────────┘    └──────────────┘    └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

## 📁 项目结构

```
├── frontend/                    # 前端服务
│   ├── Dockerfile              # 多阶段构建 + Alpine优化
│   ├── nginx.conf              # Nginx配置（反向代理）
│   └── html/                   # 静态页面
├── backend/                     # 后端服务
│   ├── Dockerfile              # 4阶段构建优化
│   ├── pom.xml                 # Maven配置
│   └── src/                    # Spring Boot源码
├── database/                    # 数据库服务
│   ├── Dockerfile              # MySQL配置
│   ├── my.cnf                  # MySQL优化配置
│   └── init/                   # 初始化SQL脚本
├── k8s/                        # Kubernetes配置
├── monitoring/                  # 监控配置
├── docker-compose.yml          # Docker Compose编排
├── .gitlab-ci.yml              # GitLab CI/CD配置
└── README.md                   # 项目文档
```

## 🚀 快速开始

### 前置要求

- Docker 24.0+
- Docker Compose 2.0+
- Git

### 启动服务

```bash
# 克隆项目
git clone <repository-url>
cd ecommerce-docker-project

# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 访问服务

- 前端页面: http://localhost
- 后端API: http://localhost:8080/api/products
- 健康检查: http://localhost:8080/actuator/health

## 📡 API接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/products | 获取所有商品 |
| GET | /api/products/{id} | 获取单个商品 |
| POST | /api/products | 创建商品 |
| PUT | /api/products/{id} | 更新商品 |
| DELETE | /api/products/{id} | 删除商品 |
| GET | /api/products/category/{category} | 按分类查询 |
| GET | /api/products/search?name=xxx | 按名称搜索 |

## 🔧 环境变量

### 后端服务

| 变量 | 默认值 | 描述 |
|------|--------|------|
| DB_HOST | localhost | 数据库主机 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | ecommerce | 数据库名称 |
| DB_USER | root | 数据库用户 |
| DB_PASSWORD | root123 | 数据库密码 |

### 数据库服务

| 变量 | 默认值 | 描述 |
|------|--------|------|
| MYSQL_ROOT_PASSWORD | root123 | Root密码 |
| MYSQL_DATABASE | ecommerce | 数据库名 |
| MYSQL_USER | ecommerce_user | 应用用户 |
| MYSQL_PASSWORD | ecommerce123 | 应用密码 |

## 🧪 测试

```bash
# 运行单元测试
cd backend
./mvnw test

# 生成测试覆盖率报告
./mvnw jacoco:report
```

## 📦 CI/CD

项目使用GitLab CI实现自动化流水线：

1. **测试阶段**: 单元测试 + 代码质量检查
2. **构建阶段**: Docker镜像构建
3. **推送阶段**: 镜像推送到Registry
4. **部署阶段**: 自动部署到测试/生产环境

## 🛠️ 常用命令

```bash
# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v

# 重新构建单个服务
docker-compose build backend

# 查看服务日志
docker-compose logs -f backend

# 进入容器
docker exec -it ecommerce-backend sh
```

## 📝 开发团队

- 项目截止日期: 2025-12-29

## 📄 License

MIT License
