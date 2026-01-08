import 'reflect-metadata';
import * as path from 'path';
import * as fs from 'fs';
import Koa from 'koa';
import cors from '@koa/cors';
import bodyParser from 'koa-bodyparser';
import session from 'koa-session';
import serve from 'koa-static';
import { appConfig as config } from './config/AppConfig';
import { closeDatabase, initializeDatabase } from './database/DataSource';
import { logger } from './util/logger';
import { errorMiddleware } from './middleware/error';
import { loggerMiddleware } from './middleware/logger';
import router from './route';

const app = new Koa();

// 配置 session
app.keys = [config.session.secret];
const sessionConfig = {
  key: config.session.key,
  maxAge: config.session.maxAge,
  overwrite: true,
  httpOnly: true,
  signed: true,
  rolling: false,
  renew: false,
};

// 全局中间件
app.use(errorMiddleware);
app.use(loggerMiddleware);
app.use(cors({
  origin: (ctx) => {
    // 允许所有来源（开发环境）
    // 生产环境应该配置具体的域名
    return ctx.request.header.origin || '*';
  },
  credentials: true,
  allowMethods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowHeaders: ['Content-Type', 'Authorization', 'Accept'],
}));
app.use(bodyParser({
  enableTypes: ['json', 'form', 'text'],
  jsonLimit: '10mb',
  formLimit: '10mb',
  textLimit: '10mb',
}));
app.use(session(sessionConfig, app));

// 注册 API 路由
app.use(router.routes());
app.use(router.allowedMethods());

// 静态文件服务（生产环境托管前端）
// Docker 中 public 目录在 /app/public，开发环境在项目根目录
const publicDir = process.env.NODE_ENV === 'production'
  ? path.join(process.cwd(), 'public')
  : path.join(__dirname, '../../public');

if (fs.existsSync(publicDir)) {
  app.use(serve(publicDir));
  logger.info(`📦 静态文件目录: ${publicDir}`);
  
  // SPA 回退：非 API 路由返回 index.html
  app.use(async (ctx, next) => {
    if (!ctx.path.startsWith('/api') && !ctx.path.startsWith('/v2') && !ctx.path.startsWith('/health')) {
      const indexPath = path.join(publicDir, 'index.html');
      if (fs.existsSync(indexPath)) {
        ctx.type = 'html';
        ctx.body = fs.createReadStream(indexPath);
        return;
      }
    }
    await next();
  });
}

// 错误事件监听
app.on('error', (err) => {
  logger.error('服务器错误:', err);
});

// 启动服务器
const PORT = config.server.port;

// 初始化数据库并启动服务器
initializeDatabase()
  .then(() => {
    app.listen(PORT, () => {
      logger.info(`🚀 服务器启动成功！`);
      logger.info(`📍 运行地址: http://localhost:${PORT}`);
      logger.info(`🌍 环境: ${config.server.env}`);
      logger.info(`📁 上传目录: ${config.upload.dir}`);
      logger.info(`💾 数据库: ${config.database.url}`);
    });
  })
  .catch((error) => {
    logger.error('❌ 服务器启动失败:', error);
    process.exit(1);
  });

// 优雅关闭
process.on('SIGINT', async () => {
  logger.info('正在关闭服务器...');
  await closeDatabase();
  process.exit(0);
});

process.on('SIGTERM', async () => {
  logger.info('正在关闭服务器...');
  await closeDatabase();
  process.exit(0);
});

export default app;
