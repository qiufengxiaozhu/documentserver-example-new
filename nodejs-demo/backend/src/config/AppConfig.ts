import config from 'config';

// 辅助函数：将字符串转换为布尔值
function toBoolean(value: any): boolean {
  if (typeof value === 'boolean') return value;
  if (typeof value === 'string') {
    return value.toLowerCase() === 'true' || value === '1';
  }
  return Boolean(value);
}

// 导出类型化的配置
export interface ServerConfig {
  port: number;
  env: string;
}

export interface DatabaseConfig {
  url: string;
}

export interface JwtConfig {
  secret: string;
  expiresIn: string;
}

export interface SessionConfig {
  secret: string;
  maxAge: number;
  key: string;
}

export interface UploadConfig {
  dir: string;
  maxFileSize: number;
}

export interface ZOfficeConfig {
  schema: string;
  host: string;
  port: number;
  context: string;
  cors: boolean;
  app: {
    secret: string;
    feIntegration: {
      enable: boolean;
    };
  };
}

export interface DemoConfig {
  host: string;
  context: string;
  repoId: string;
  tokenName: string;
}

export interface AdminConfig {
  username: string;
  password: string;
  email: string;
}

export interface AppConfig {
  server: ServerConfig;
  database: DatabaseConfig;
  jwt: JwtConfig;
  session: SessionConfig;
  upload: UploadConfig;
  zoffice: ZOfficeConfig;
  demo: DemoConfig;
  admin: AdminConfig;
}

// 获取原始配置
const rawZoffice = config.get<any>('zoffice');

// 导出配置对象
export const appConfig: AppConfig = {
  server: config.get('server'),
  database: config.get('database'),
  jwt: config.get('jwt'),
  session: config.get('session'),
  upload: config.get('upload'),
  zoffice: {
    ...rawZoffice,
    cors: toBoolean(rawZoffice.cors),
    app: {
      ...rawZoffice.app,
      feIntegration: {
        enable: toBoolean(rawZoffice.app?.feIntegration?.enable),
      },
    },
  },
  demo: config.get('demo'),
  admin: config.get('admin'),
};

// 兼容旧的导出方式
export { appConfig as config };
