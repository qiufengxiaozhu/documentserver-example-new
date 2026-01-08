import 'reflect-metadata';
import * as crypto from 'crypto';
import { AppDataSource } from './DataSource';
import { SysUser } from '../entity';
import config from 'config';

// 简单的密码加密函数
function hashPassword(password: string): string {
  return crypto.createHash('sha256').update(password).digest('hex');
}

async function seed() {
  try {
    // 初始化数据库连接
    await AppDataSource.initialize();
    console.log('开始初始化数据库...\n');

    const userRepository = AppDataSource.getRepository(SysUser);

    // 检查是否已存在管理员用户
    const existingAdmin = await userRepository.findOne({
      where: { username: config.get<string>('admin.username') },
    });

    if (existingAdmin) {
      console.log('⚠️  管理员用户已存在，跳过初始化');
      await AppDataSource.destroy();
      return;
    }

    // 创建管理员用户
    const adminUser = userRepository.create({
      username: config.get<string>('admin.username'),
      password: hashPassword(config.get<string>('admin.password')),
      email: config.get<string>('admin.email'),
      nickname: '管理员',
    });
    await userRepository.save(adminUser);
    console.log('✅ 管理员用户创建成功:', {
      id: adminUser.id,
      username: adminUser.username,
      email: adminUser.email,
      nickname: adminUser.nickname,
    });

    // 创建测试用户
    const testUser = userRepository.create({
      username: 'test',
      password: hashPassword('test123'),
      email: 'test@example.com',
      nickname: '测试用户',
    });
    await userRepository.save(testUser);
    console.log('✅ 测试用户创建成功:', {
      id: testUser.id,
      username: testUser.username,
      email: testUser.email,
      nickname: testUser.nickname,
    });

    // 创建共享用户
    const shareUser = userRepository.create({
      username: 'share',
      password: hashPassword('share123'),
      email: 'share@example.com',
      nickname: '共享用户',
    });
    await userRepository.save(shareUser);
    console.log('✅ 共享用户创建成功:', {
      id: shareUser.id,
      username: shareUser.username,
      email: shareUser.email,
      nickname: shareUser.nickname,
    });

    console.log('\n🎉 数据库初始化完成！');
    console.log('\n默认账号信息：');
    console.log('  管理员: admin / zOffice');
    console.log('  测试用户: test / test123');
    console.log('  共享用户: share / share123');

    await AppDataSource.destroy();
  } catch (error) {
    console.error('❌ 数据库初始化失败:', error);
    process.exit(1);
  }
}

// 执行初始化
seed();
