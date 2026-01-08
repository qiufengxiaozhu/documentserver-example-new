import { Context, Next } from 'koa';
import { JwtPayload, verifyToken } from '../util/jwt';
import { unauthorized } from '../util/response';
import { appConfig as config } from '../config/AppConfig';

// 扩展 Koa Context 类型
declare module 'koa' {
  interface Context {
    user?: JwtPayload;
  }
}

/**
 * JWT 认证中间件
 */
export async function authMiddleware(ctx: Context, next: Next): Promise<void> {
  try {
    // 从 header 中获取 token
    const authHeader: string | undefined = ctx.headers.authorization;
    let token: string | undefined;

    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.substring(7);
    }

    // 从 cookie 中获取 token
    if (!token) {
      token = ctx.cookies.get(config.demo.tokenName);
    }

    // 从 query 中获取 token（支持多种参数名）
    if (!token) {
      // 优先使用配置的 tokenName，也支持通用的 token 参数
      token = (ctx.query[config.demo.tokenName] || ctx.query.token) as string | undefined;
    }

    if (!token) {
      unauthorized(ctx, '未提供认证令牌');
      return;
    }

    // 验证 token
    ctx.user = verifyToken(token);

    await next();
  } catch (error) {
    unauthorized(ctx, '无效的认证令牌');
  }
}

/**
 * 可选认证中间件（不强制要求登录）
 */
export async function optionalAuthMiddleware(ctx: Context, next: Next): Promise<void> {
  try {
    const authHeader = ctx.headers.authorization;
    let token: string | undefined;

    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.substring(7);
    }

    if (!token) {
      token = ctx.cookies.get(config.demo.tokenName);
    }

    if (token) {
      try {
        ctx.user = verifyToken(token);
      } catch (error) {
        // 忽略错误，继续执行
      }
    }

    await next();
  } catch (error) {
    await next();
  }
}

