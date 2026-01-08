/**
 * 日志系统 - 支持每日轮询
 */
import winston from 'winston';
import DailyRotateFile from 'winston-daily-rotate-file';
import path from 'path';
import fs from 'fs';

// 日志目录
const LOG_DIR = process.env.LOG_DIR || './logs';

// 确保日志目录存在
if (!fs.existsSync(LOG_DIR)) {
  fs.mkdirSync(LOG_DIR, { recursive: true });
}

// 自定义日志格式
const customFormat = winston.format.combine(
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss.SSS' }),
  winston.format.errors({ stack: true }),
  winston.format.printf(({ timestamp, level, message, stack, ...meta }) => {
    let log = `[${timestamp}] [${level.toUpperCase()}] ${message}`;
    if (Object.keys(meta).length > 0) {
      log += ` ${JSON.stringify(meta)}`;
    }
    if (stack) {
      log += `\n${stack}`;
    }
    return log;
  })
);

// 控制台格式（带颜色）
const consoleFormat = winston.format.combine(
  winston.format.colorize(),
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss.SSS' }),
  winston.format.printf(({ timestamp, level, message, ...meta }) => {
    let log = `[${timestamp}] [${level}] ${message}`;
    if (Object.keys(meta).length > 0) {
      log += ` ${JSON.stringify(meta)}`;
    }
    return log;
  })
);

// 每日轮询配置 - 应用日志
const appTransport: DailyRotateFile = new DailyRotateFile({
  filename: path.join(LOG_DIR, 'app-%DATE%.log'),
  datePattern: 'YYYY-MM-DD',
  zippedArchive: true,
  maxSize: '20m',
  maxFiles: '30d', // 保留30天
  level: 'info',
  format: customFormat,
});

// 每日轮询配置 - 错误日志
const errorTransport: DailyRotateFile = new DailyRotateFile({
  filename: path.join(LOG_DIR, 'error-%DATE%.log'),
  datePattern: 'YYYY-MM-DD',
  zippedArchive: true,
  maxSize: '20m',
  maxFiles: '30d',
  level: 'error',
  format: customFormat,
});

// 每日轮询配置 - 访问日志
const accessTransport: DailyRotateFile = new DailyRotateFile({
  filename: path.join(LOG_DIR, 'access-%DATE%.log'),
  datePattern: 'YYYY-MM-DD',
  zippedArchive: true,
  maxSize: '50m',
  maxFiles: '14d', // 访问日志保留14天
  level: 'info',
  format: customFormat,
});

// 监听轮询事件
appTransport.on('rotate', (oldFilename, newFilename) => {
  console.log(`日志轮询: ${oldFilename} -> ${newFilename}`);
});

// 创建主日志记录器
const winstonLogger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  transports: [
    // 控制台输出
    new winston.transports.Console({
      format: consoleFormat,
    }),
    // 应用日志文件
    appTransport,
    // 错误日志文件
    errorTransport,
  ],
});

// 创建访问日志记录器
const accessLogger = winston.createLogger({
  level: 'info',
  transports: [accessTransport],
});

/**
 * 日志类 - 封装 winston
 */
export class Logger {
  private prefix: string;

  constructor(prefix: string = '') {
    this.prefix = prefix;
  }

  private formatMessage(message: string): string {
    return this.prefix ? `[${this.prefix}] ${message}` : message;
  }

  info(message: string, ...args: any[]): void {
    if (args.length > 0 && typeof args[0] === 'object') {
      winstonLogger.info(this.formatMessage(message), args[0]);
    } else if (args.length > 0) {
      winstonLogger.info(this.formatMessage(`${message} ${args.join(' ')}`));
    } else {
      winstonLogger.info(this.formatMessage(message));
    }
  }

  warn(message: string, ...args: any[]): void {
    if (args.length > 0 && typeof args[0] === 'object') {
      winstonLogger.warn(this.formatMessage(message), args[0]);
    } else if (args.length > 0) {
      winstonLogger.warn(this.formatMessage(`${message} ${args.join(' ')}`));
    } else {
      winstonLogger.warn(this.formatMessage(message));
    }
  }

  error(message: string, ...args: any[]): void {
    if (args.length > 0 && args[0] instanceof Error) {
      winstonLogger.error(this.formatMessage(message), { error: args[0].message, stack: args[0].stack });
    } else if (args.length > 0 && typeof args[0] === 'object') {
      winstonLogger.error(this.formatMessage(message), args[0]);
    } else if (args.length > 0) {
      winstonLogger.error(this.formatMessage(`${message} ${args.join(' ')}`));
    } else {
      winstonLogger.error(this.formatMessage(message));
    }
  }

  debug(message: string, ...args: any[]): void {
    if (args.length > 0 && typeof args[0] === 'object') {
      winstonLogger.debug(this.formatMessage(message), args[0]);
    } else if (args.length > 0) {
      winstonLogger.debug(this.formatMessage(`${message} ${args.join(' ')}`));
    } else {
      winstonLogger.debug(this.formatMessage(message));
    }
  }

  /**
   * 记录访问日志
   */
  access(message: string, meta?: Record<string, any>): void {
    accessLogger.info(this.formatMessage(message), meta);
  }
}

// 默认日志实例
export const logger = new Logger('FilezDemo');

// 导出 winston 实例供高级用法
export { winstonLogger, accessLogger };
