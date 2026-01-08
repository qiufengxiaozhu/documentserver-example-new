import { Context } from 'koa';
import * as userService from '../service/userService';
import { success, error, notFound, forbidden } from '../util/response';
import { logger } from '../util/logger';

/**
 * 获取用户信息
 */
export async function getUser(ctx: Context): Promise<void> {
  const { userId } = ctx.params;
  const currentUserId = ctx.user?.userId;

  if (!currentUserId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 只能查看自己的信息或管理员可以查看所有人
  if (currentUserId !== userId && currentUserId !== 'admin') {
    forbidden(ctx, '无权查看该用户信息');
    return;
  }

  const user = await userService.findById(userId);
  if (!user) {
    notFound(ctx, '用户不存在');
    return;
  }

  success(ctx, {
    id: user.id,
    username: user.username,
    email: user.email,
    nickname: user.nickname,
    avatar: user.avatar,
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  });
}

/**
 * 更新用户信息
 */
export async function updateUser(ctx: Context): Promise<void> {
  const { userId } = ctx.params;
  const { nickname, avatar, email } = ctx.request.body as any;
  const currentUserId = ctx.user?.userId;

  if (!currentUserId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 只能修改自己的信息或管理员可以修改所有人
  if (currentUserId !== userId && currentUserId !== 'admin') {
    forbidden(ctx, '无权修改该用户信息');
    return;
  }

  const user = await userService.findById(userId);
  if (!user) {
    notFound(ctx, '用户不存在');
    return;
  }

  const updatedUserData = await userService.updateUser(userId, {
    nickname,
    avatar,
    email,
  });

  logger.info(`用户信息更新成功: userId: ${userId}`);
  success(ctx, {
    id: updatedUserData.id,
    username: updatedUserData.username,
    email: updatedUserData.email,
    nickname: updatedUserData.nickname,
    avatar: updatedUserData.avatar,
  }, '更新成功');
}

/**
 * 修改密码
 */
export async function changePassword(ctx: Context): Promise<void> {
  const { userId } = ctx.params;
  const { oldPassword, newPassword } = ctx.request.body as any;
  const currentUserId = ctx.user?.userId;

  if (!currentUserId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 只能修改自己的密码
  if (currentUserId !== userId) {
    forbidden(ctx, '无权修改该用户密码');
    return;
  }

  if (!oldPassword || !newPassword) {
    error(ctx, '旧密码和新密码不能为空');
    return;
  }

  const user = await userService.findById(userId);
  if (!user) {
    notFound(ctx, '用户不存在');
    return;
  }

  // 验证旧密码
  const validUser = await userService.validateUser(user.username, oldPassword);
  if (!validUser) {
    error(ctx, '旧密码错误');
    return;
  }

  await userService.updatePassword(userId, newPassword);

  logger.info(`用户密码修改成功: userId: ${userId}`);
  success(ctx, null, '密码修改成功');
}

/**
 * 获取所有用户（管理员）
 */
export async function getAllUsers(ctx: Context): Promise<void> {
  const currentUserId = ctx.user?.userId;

  if (!currentUserId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 只有管理员可以查看所有用户
  if (currentUserId !== 'admin') {
    forbidden(ctx, '无权查看所有用户');
    return;
  }

  const users = await userService.getAllUsers();
  
  const userList = users.map(user => ({
    id: user.id,
    username: user.username,
    email: user.email,
    nickname: user.nickname,
    avatar: user.avatar,
    createdAt: user.createdAt,
  }));

  success(ctx, userList);
}

