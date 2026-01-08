import { Context } from 'koa';
import * as userService from '../service/userService';
import { generateToken } from '../util/jwt';
import { success, error, unauthorized } from '../util/response';
import { appConfig as config } from '../config/AppConfig';
import { logger } from '../util/logger';

/**
 * 用户登录
 */
export async function login(ctx: Context): Promise<void> {
  const { username, password } = ctx.request.body as any;

  // 调试日志
  logger.debug(`登录请求 - username: ${username}, password: ${password ? '***' : 'empty'}`);

  if (!username || !password) {
    logger.warn(`登录失败: 用户名或密码为空 - username: ${username}`);
    error(ctx, '用户名和密码不能为空');
    return;
  }

  // 验证用户
  const user = await userService.validateUser(username, password);
  if (!user) {
    logger.warn(`登录失败: 用户名或密码错误 - username: ${username}`);
    unauthorized(ctx, '用户名或密码错误');
    return;
  }

  // 生成 token
  const token = generateToken({
    userId: user.id,
    username: user.username,
    email: user.email,
  });

  // 设置 cookie
  ctx.cookies.set(config.demo.tokenName, token, {
    maxAge: config.session.maxAge,
    httpOnly: true,
    overwrite: true,
  });

  logger.info(`用户登录成功: ${user.username}`);

  success(ctx, {
    token,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      nickname: user.nickname,
      avatar: user.avatar,
    },
  }, '登录成功');
}

/**
 * 用户登出
 */
export async function logout(ctx: Context): Promise<void> {
  // 清除 cookie
  ctx.cookies.set(config.demo.tokenName, '', {
    maxAge: 0,
  });

  logger.info('用户登出');
  success(ctx, null, '登出成功');
}

/**
 * 获取当前用户信息
 */
export async function profile(ctx: Context): Promise<void> {
  const userId = ctx.user?.userId;
  if (!userId) {
    unauthorized(ctx, '未登录');
    return;
  }

  const user = await userService.findById(userId);
  if (!user) {
    error(ctx, '用户不存在');
    return;
  }

  success(ctx, {
    id: user.id,
    username: user.username,
    email: user.email,
    nickname: user.nickname,
    avatar: user.avatar,
    createdAt: user.createdAt,
  });
}

/**
 * 获取用户列表（用于 Filez 集成）
 */
export async function profiles(ctx: Context): Promise<void> {
  const users = await userService.getAllUsers();
  
  const profileList = users.map(user => ({
    id: user.id,
    name: user.nickname || user.username,
    email: user.email,
    avatar: user.avatar || '',
  }));

  success(ctx, profileList);
}

