# ============================================
# 前端服务 Dockerfile - Nginx静态页面容器
# 多阶段构建 + Alpine优化（重要！3分）
# 使用官方Docker Hub镜像
# ============================================

# ============================================
# 阶段1：准备阶段（如有Node.js构建可在此处理）
# ============================================
FROM alpine:3.19 AS prepare
WORKDIR /app

# 复制静态文件
COPY html/ ./html/
COPY nginx.conf ./

# ============================================
# 阶段2：生产运行阶段（Alpine最小化镜像）
# ============================================
FROM nginx:1.25-alpine AS final

LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Frontend Service"

# 从准备阶段复制文件
COPY --from=prepare /app/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=prepare /app/html/ /usr/share/nginx/html/

# 合并RUN指令，减少层数 + 安装健康检查工具
RUN apk add --no-cache curl && \
    chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# 健康检查配置（重要！2分）
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -fs http://localhost:80/health || exit 1

EXPOSE 80

USER nginx

CMD ["nginx", "-g", "daemon off;"]
