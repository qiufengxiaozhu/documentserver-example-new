import * as userDao from '../dao/userDao';
import { SysUser } from '../entity';
import { hashPassword, verifyPassword } from '../util/crypto';

/**
 * 根据用户名查找用户
 */
export async function findByUsername(username: string): Promise<SysUser | null> {
  return userDao.findByUsername(username);
}

/**
 * 根据用户ID查找用户
 */
export async function findById(id: string): Promise<SysUser | null> {
  return userDao.findById(id);
}

/**
 * 根据邮箱查找用户
 */
export async function findByEmail(email: string): Promise<SysUser | null> {
  return userDao.findByEmail(email);
}

/**
 * 验证用户登录
 */
export async function validateUser(username: string, password: string): Promise<SysUser | null> {
  const user = await findByUsername(username);
  if (!user) {
    return null;
  }

  const isValid = verifyPassword(password, user.password);
  if (!isValid) {
    return null;
  }

  return user;
}

/**
 * 创建用户
 */
export async function createUser(data: {
  username: string;
  password: string;
  email: string;
  nickname?: string;
  avatar?: string;
}): Promise<SysUser> {
  return userDao.create({
    ...data,
    password: hashPassword(data.password),
  });
}

/**
 * 更新用户信息
 */
export async function updateUser(
  id: string,
  data: {
    nickname?: string;
    avatar?: string;
    email?: string;
  }
): Promise<SysUser> {
  await userDao.update(id, data);
  const user = await findById(id);
  if (!user) {
    throw new Error('User not found');
  }
  return user;
}

/**
 * 更新用户密码
 */
export async function updatePassword(id: string, newPassword: string): Promise<SysUser> {
  await userDao.update(id, { password: hashPassword(newPassword) });
  const user = await findById(id);
  if (!user) {
    throw new Error('User not found');
  }
  return user;
}

/**
 * 删除用户
 */
export async function deleteUser(id: string): Promise<SysUser> {
  const user = await findById(id);
  if (!user) {
    throw new Error('User not found');
  }
  return userDao.remove(user);
}

/**
 * 获取所有用户
 */
export async function getAllUsers(): Promise<SysUser[]> {
  return userDao.findAll();
}

