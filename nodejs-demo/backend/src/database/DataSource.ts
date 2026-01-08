import 'reflect-metadata';
import { DataSource } from 'typeorm';
import { appConfig } from '../config/AppConfig';
import { SysUser, DocMeta, DocControl } from '../entity';

// 解析 SQLite 数据库 URL
const getDatabasePath = (url: string): string => {
  // 处理 file:./data/filez_demo.db 格式
  if (url.startsWith('file:')) {
    return url.replace('file:', '');
  }
  return url;
};

const isDev = true;
// const isDev = appConfig.server.env === 'development';

export const AppDataSource = new DataSource({
  type: 'sqlite',
  database: getDatabasePath(appConfig.database.url),
  synchronize: isDev, // 仅开发环境自动同步，生产环境需使用 migration
  logging: isDev,
  entities: [SysUser, DocMeta, DocControl],
  migrations: ['src/database/migration/*.ts'],
  subscribers: [],
});

// 初始化数据源
export const initializeDatabase = async () => {
  try {
    if (!AppDataSource.isInitialized) {
      await AppDataSource.initialize();
      
      // SQLite 优化配置 - 解决 database is locked 问题
      await AppDataSource.query('PRAGMA journal_mode = WAL;');      // 启用 WAL 模式，提高并发性能
      await AppDataSource.query('PRAGMA busy_timeout = 30000;');    // 设置忙等待超时为 30 秒
      await AppDataSource.query('PRAGMA synchronous = NORMAL;');    // 平衡性能和安全性
      await AppDataSource.query('PRAGMA cache_size = -64000;');     // 设置缓存为 64MB
      await AppDataSource.query('PRAGMA temp_store = MEMORY;');     // 临时表存储在内存中
      
      console.log('✅ 数据库连接成功！');
      console.log(`📁 数据库路径: ${getDatabasePath(appConfig.database.url)}`);
    }
  } catch (error) {
    console.error('❌ 数据库连接失败:', error);
    throw error;
  }
};

// 关闭数据源
export const closeDatabase = async () => {
  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
    console.log('✅ 数据库连接已关闭');
  }
};
