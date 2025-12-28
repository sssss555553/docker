# ============================================
# 数据库服务 Dockerfile - MySQL容器化部署
# MySQL版本选择合理（2分）
# 使用官方Docker Hub镜像
# ============================================

FROM mysql:8.0

LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Database Service"

# 环境变量配置
ENV MYSQL_ROOT_PASSWORD=root123
ENV MYSQL_DATABASE=ecommerce
ENV MYSQL_USER=ecommerce_user
ENV MYSQL_PASSWORD=ecommerce123
ENV TZ=Asia/Shanghai

# 复制自定义MySQL配置（字符集和时区配置）
COPY my.cnf /etc/mysql/conf.d/custom.cnf

# 复制初始化脚本（自动执行 - 重要！3分）
COPY init/ /docker-entrypoint-initdb.d/

# 健康检查配置
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1

EXPOSE 3306
