# 部署指南

## 📋 前置条件

- Docker 24.0+
- Docker Compose 2.20+
- Git

## 🚀 快速启动

### 1. 克隆项目

```bash
git clone <repository-url>
cd ecommerce-docker-project
```

### 2. 配置环境变量（可选）

```bash
# 复制环境变量示例文件
cp .env.example .env

# 根据需要修改配置
vim .env
```

### 3. 启动所有服务

```bash
# 构建并启动（后台运行）
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 验证部署

```bash
# 检查前端健康状态
curl http://localhost/health

# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 测试API
curl http://localhost:8080/api/products
```

## 📊 服务架构

| 服务 | 端口 | 健康检查端点 | 说明 |
|------|------|--------------|------|
| frontend | 80 | /health | Nginx静态页面服务 |
| backend | 8080 | /actuator/health | Spring Boot API |
| database | 3306 | mysqladmin ping | MySQL数据库 |

## 🔧 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 停止并删除数据卷（慎用！会删除数据）
docker-compose down -v

# 重启单个服务
docker-compose restart backend

# 查看服务日志
docker-compose logs -f backend

# 进入容器
docker-compose exec backend sh
docker-compose exec database mysql -u root -proot123

# 查看资源使用
docker stats
```

## 🔄 服务依赖关系

```
database (MySQL)
    ↓ service_healthy
backend (Spring Boot)
    ↓ service_healthy
frontend (Nginx)
```

启动顺序：database → backend → frontend
停止顺序：frontend → backend → database

## 📁 数据持久化

数据卷位置：
- `ecommerce-mysql-data`: MySQL数据文件
- `ecommerce-mysql-logs`: MySQL日志文件

备份数据：
```bash
# 备份MySQL数据
docker run --rm -v ecommerce-mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data
```

## ⚠️ 注意事项

1. 首次启动时，MySQL初始化需要约60秒
2. 后端服务会等待数据库健康后才启动
3. 生产环境请修改默认密码
4. 建议定期备份数据卷
