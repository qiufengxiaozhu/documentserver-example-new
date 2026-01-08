import { Context, Next } from 'koa';
import { logger } from '../util/logger';

/**
 * 请求日志中间件
 */
export async function loggerMiddleware(ctx: Context, next: Next): Promise<void> {
  const start: number = Date.now();
  const { method, url } = ctx.request;

  try {
    await next();
    const ms = Date.now() - start;
    const { status } = ctx;
    
    logger.info(`${method} ${url} - ${status} - ${ms}ms`);
  } catch (error) {
    const ms = Date.now() - start;
    logger.error(`${method} ${url} - Error - ${ms}ms`, error);
    throw error;
  }
}

