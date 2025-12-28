# 故障排查指南

## 🔍 常见问题

### 1. 数据库连接失败

**症状**: 后端服务启动失败，日志显示 `Connection refused` 或 `Communications link failure`

**排查步骤**:
```bash
# 1. 检查数据库容器状态
docker-compose ps database

# 2. 查看数据库日志
docker-compose logs database

# 3. 检查网络连通性
docker-compose exec backend ping database

# 4. 手动测试数据库连接
docker-compose exec database mysql -u root -proot123 -e "SELECT 1"
```

**解决方案**:
- 确保数据库容器健康状态为 `healthy`
- 检查环境变量 `DB_HOST` 是否正确设置为 `database`
- 等待数据库初始化完成（约60秒）

---

### 2. 后端服务启动超时

**症状**: 后端容器反复重启，健康检查失败

**排查步骤**:
```bash
# 查看后端日志
docker-compose logs -f backend

# 检查健康状态
curl http://localhost:8080/actuator/health
```

**解决方案**:
- 增加 `start_period` 时间
- 检查JVM内存配置是否足够
- 确认数据库连接配置正确

---

### 3. 前端无法访问后端API

**症状**: 页面显示 "加载失败" 或 API 返回 502/504

**排查步骤**:
```bash
# 1. 检查Nginx配置
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# 2. 测试后端API直接访问
curl http://localhost:8080/api/products

# 3. 测试通过Nginx代理访问
curl http://localhost/api/products

# 4. 查看Nginx错误日志
docker-compose exec frontend cat /var/log/nginx/error.log
```

**解决方案**:
- 确认 `proxy_pass` 配置正确指向 `http://backend:8080`
- 检查后端服务是否健康
- 验证网络配置

---

### 4. 数据丢失

**症状**: 重启容器后数据消失

**排查步骤**:
```bash
# 检查数据卷
docker volume ls | grep ecommerce

# 查看卷详情
docker volume inspect ecommerce-mysql-data
```

**解决方案**:
- 确保使用命名卷而非匿名卷
- 不要使用 `docker-compose down -v`（会删除卷）
- 定期备份数据卷

---

### 5. 端口冲突

**症状**: `Bind for 0.0.0.0:80 failed: port is already allocated`

**排查步骤**:
```bash
# Windows
netstat -ano | findstr :80

# Linux/Mac
lsof -i :80
```

**解决方案**:
- 停止占用端口的进程
- 或修改 docker-compose.yml 中的端口映射

---

## 🛠️ 调试命令

```bash
# 查看所有容器状态
docker-compose ps -a

# 查看容器资源使用
docker stats

# 进入容器调试
docker-compose exec backend sh
docker-compose exec database mysql -u root -proot123

# 查看网络配置
docker network inspect ecommerce-network

# 重建单个服务
docker-compose up -d --build --force-recreate backend

# 清理未使用资源
docker system prune -f
```

## 📊 健康检查端点

| 服务 | 端点 | 预期响应 |
|------|------|----------|
| frontend | http://localhost/health | `healthy` |
| backend | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| database | mysqladmin ping | `mysqld is alive` |
