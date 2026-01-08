import { Context, Next } from 'koa';
import { logger } from '../util/logger';
import { serverError } from '../util/response';

/**
 * 全局错误处理中间件
 */
export async function errorMiddleware(ctx: Context, next: Next): Promise<void> {
  try {
    await next();
  } catch (error: any) {
    logger.error('服务器错误:', error);

    // 如果响应已经发送，则不再处理
    if (ctx.headerSent) {
      return;
    }

    // 处理不同类型的错误
    if (error.status) {
      ctx.status = error.status;
      ctx.body = {
        code: error.status,
        message: error.message || 'Error',
        timestamp: Date.now(),
      };
    } else {
      serverError(ctx, error.message || '服务器内部错误');
    }

    // 触发 Koa 的错误事件
    ctx.app.emit('error', error, ctx);
  }
}

