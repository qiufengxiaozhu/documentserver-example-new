import { AppDataSource } from '../database/DataSource';
import { SysUser } from '../entity';
import { Repository } from "typeorm";

const userRepository: Repository<SysUser> = AppDataSource.getRepository(SysUser);

/**
 * 根据用户名查找用户
 */
export async function findByUsername(username: string): Promise<SysUser | null> {
  return userRepository.findOne({ where: { username } });
}

/**
 * 根据用户ID查找用户
 */
export async function findById(id: string): Promise<SysUser | null> {
  return userRepository.findOne({ where: { id } });
}

/**
 * 根据邮箱查找用户
 */
export async function findByEmail(email: string): Promise<SysUser | null> {
  return userRepository.findOne({ where: { email } });
}

/**
 * 创建用户
 */
export async function create(data: Partial<SysUser>): Promise<SysUser> {
  const user = userRepository.create(data);
  return userRepository.save(user);
}

/**
 * 更新用户信息
 */
export async function update(id: string, data: Partial<SysUser>): Promise<void> {
  await userRepository.update(id, data);
}

/**
 * 删除用户
 */
export async function remove(user: SysUser): Promise<SysUser> {
  return userRepository.remove(user);
}

/**
 * 获取所有用户
 */
export async function findAll(): Promise<SysUser[]> {
  return userRepository.find({ order: { createdAt: 'DESC' } });
}

