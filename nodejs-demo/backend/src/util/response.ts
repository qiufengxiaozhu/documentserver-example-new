import { Context } from 'koa';

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data?: T;
  timestamp: number;
}

/**
 * 成功响应
 */
export function success<T = any>(ctx: Context, data?: T, message: string = 'Success'): void {
  ctx.status = 200;
  ctx.body = {
    code: 0,
    message,
    data,
    timestamp: Date.now(),
  } as ApiResponse<T>;
}

/**
 * 失败响应
 */
export function error(ctx: Context, message: string = 'Error', code: number = 1, status: number = 400): void {
  ctx.status = status;
  ctx.body = {
    code,
    message,
    timestamp: Date.now(),
  } as ApiResponse;
}

/**
 * 未授权响应
 */
export function unauthorized(ctx: Context, message: string = 'Unauthorized'): void {
  error(ctx, message, 401, 401);
}

/**
 * 禁止访问响应
 */
export function forbidden(ctx: Context, message: string = 'Forbidden'): void {
  error(ctx, message, 403, 403);
}

/**
 * 未找到响应
 */
export function notFound(ctx: Context, message: string = 'Not Found'): void {
  error(ctx, message, 404, 404);
}

/**
 * 服务器错误响应
 */
export function serverError(ctx: Context, message: string = 'Internal Server Error'): void {
  error(ctx, message, 500, 500);
}

