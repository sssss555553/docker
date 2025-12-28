# CI/CD 流水线说明

## 📋 概述

本项目使用 GitLab CI/CD 实现自动化构建、测试和部署流程。

## 🔄 流水线阶段

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  test   │───▶│  build  │───▶│  push   │───▶│ deploy  │
└─────────┘    └─────────┘    └─────────┘    └─────────┘
```

| 阶段 | 任务 | 说明 |
|------|------|------|
| test | unit-test | 单元测试 + JaCoCo覆盖率 |
| test | code-quality | 代码质量检查 |
| test | integration-test | 集成测试（手动触发） |
| build | build-backend | 构建后端Docker镜像 |
| build | build-frontend | 构建前端Docker镜像 |
| build | build-database | 构建数据库Docker镜像 |
| push | push-images | 推送镜像到GitLab Registry |
| deploy | deploy-staging | 部署到测试环境（手动） |
| deploy | deploy-production | 部署到生产环境（手动） |

## 🔧 触发条件

- `main` 分支：完整流水线（测试→构建→推送→部署）
- `develop` 分支：测试和构建
- Merge Request：仅测试阶段

## 📊 测试报告

### JUnit 测试报告
- 位置：`backend/target/surefire-reports/`
- 在 MR 和 Pipeline 页面可查看测试结果

### JaCoCo 覆盖率报告
- 位置：`backend/target/site/jacoco/`
- 目标覆盖率：≥80%

## 🐳 镜像仓库

镜像推送到 GitLab Container Registry：
- 后端：`$CI_REGISTRY_IMAGE/backend`
- 前端：`$CI_REGISTRY_IMAGE/frontend`
- 数据库：`$CI_REGISTRY_IMAGE/database`

## 🚀 部署

### 测试环境
```bash
# 手动触发 deploy-staging 任务
# 或使用命令行
docker-compose -f docker-compose.yml up -d
```

### 生产环境
```bash
# 手动触发 deploy-production 任务
# 配置 DEPLOY_WEBHOOK_URL 变量实现自动部署
```

## ⚙️ 配置变量

在 GitLab 项目设置中配置以下变量：

| 变量 | 说明 | 必需 |
|------|------|------|
| CI_REGISTRY_USER | Registry用户名 | 自动 |
| CI_REGISTRY_PASSWORD | Registry密码 | 自动 |
| DEPLOY_WEBHOOK_URL | 部署Webhook地址 | 可选 |

## 📝 本地测试

使用 gitlab-ci-local 在本地测试流水线：
```bash
# 安装
npm install -g gitlab-ci-local

# 运行测试阶段
gitlab-ci-local --job unit-test
```
