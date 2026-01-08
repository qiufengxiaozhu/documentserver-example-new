# 阶段1: 构建前端
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 阶段2: 构建后端
FROM node:20-alpine AS backend-builder

WORKDIR /app/backend
COPY backend/package*.json ./
RUN npm ci
COPY backend/ ./
RUN npm run build

# 阶段3: 生产镜像
FROM node:20-alpine AS production

WORKDIR /app

# 安装生产依赖
COPY backend/package*.json ./
RUN npm ci --only=production

# 复制后端构建产物
COPY --from=backend-builder /app/backend/dist ./dist
COPY --from=backend-builder /app/backend/config ./config
COPY --from=backend-builder /app/backend/templates ./templates

# 复制前端构建产物到 public 目录
COPY --from=frontend-builder /app/frontend/dist ./public

# 复制启动脚本
COPY backend/docker-entrypoint.sh ./
RUN chmod +x docker-entrypoint.sh

# 创建数据目录和日志目录
RUN mkdir -p data uploads logs

# 环境变量
ENV NODE_ENV=production
ENV PORT=8000
ENV LOG_DIR=/app/logs

EXPOSE 8000

# 启动命令
ENTRYPOINT ["./docker-entrypoint.sh"]

