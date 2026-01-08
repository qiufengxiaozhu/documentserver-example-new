# Filez Demo Backend

基于 Node.js + Koa + TypeScript 的文档管理后端服务，用于演示 ZOffice 集成。

## 快速开始

```bash
# 安装依赖
npm install

# 初始化数据库
npm run seed

# 开发模式
npm run dev

# 生产构建
npm run build
npm start
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| PORT | 服务端口 | 8000 |
| NODE_ENV | 环境 | development |

## 默认账号

- 用户名: `admin`
- 密码: `zOffice`

## API 文档

启动后访问: http://localhost:8000/health

