/**
 * 端口占用检测与清理脚本
 * 
 * 在启动开发服务器前检测端口是否被占用，如果被占用则自动终止占用进程
 */

import { execSync } from 'child_process';
import * as path from 'path';
import * as fs from 'fs';

// 直接读取 config 文件获取端口，避免 config 库的模块导入问题
function getPortFromConfig(): number {
  const configPath = path.resolve(__dirname, '../config/default.json');
  try {
    const configContent = fs.readFileSync(configPath, 'utf8');
    const config = JSON.parse(configContent);
    return config.server?.port || 8000;
  } catch {
    return 8000;
  }
}

const PORT = parseInt(process.env.PORT || String(getPortFromConfig()), 10);

/**
 * 获取占用指定端口的进程 PID
 */
function getProcessOnPort(port: number): string[] {
  const platform = process.platform;
  const pids: Set<string> = new Set();

  try {
    if (platform === 'win32') {
      // Windows: 使用 netstat 查找端口占用
      const result = execSync(`netstat -ano | findstr :${port}`, { encoding: 'utf8' });
      const lines = result.trim().split('\n');
      for (const line of lines) {
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 5) {
          const pid = parts[parts.length - 1];
          if (pid && pid !== '0') {
            pids.add(pid);
          }
        }
      }
    } else {
      // macOS/Linux: 使用 lsof 查找端口占用
      const result = execSync(`lsof -ti :${port}`, { encoding: 'utf8' });
      const lines = result.trim().split('\n');
      for (const line of lines) {
        const pid = line.trim();
        if (pid) {
          pids.add(pid);
        }
      }
    }
  } catch {
    // 命令执行失败通常意味着端口未被占用
  }

  return Array.from(pids);
}

/**
 * 终止指定 PID 的进程
 */
function killProcess(pid: string): boolean {
  const platform = process.platform;

  try {
    if (platform === 'win32') {
      execSync(`taskkill /F /PID ${pid}`, { encoding: 'utf8' });
    } else {
      execSync(`kill -9 ${pid}`, { encoding: 'utf8' });
    }
    return true;
  } catch {
    return false;
  }
}

/**
 * 检测并清理端口
 */
function killPort(port: number): void {
  console.log(`🔍 检测端口 ${port} 占用情况...`);
  
  const pids = getProcessOnPort(port);
  
  if (pids.length === 0) {
    console.log(`✅ 端口 ${port} 未被占用，可以正常启动`);
    return;
  }

  for (const pid of pids) {
    console.log(`⚠️  端口 ${port} 被进程 ${pid} 占用，正在终止...`);
    if (killProcess(pid)) {
      console.log(`✅ 进程 ${pid} 已终止`);
    } else {
      console.log(`❌ 无法终止进程 ${pid}，请手动处理`);
    }
  }

  // 等待一小段时间确保端口释放
  const waitMs = 500;
  console.log(`⏳ 等待 ${waitMs}ms 确保端口释放...`);
  execSync(process.platform === 'win32' ? `ping -n 1 127.0.0.1 > nul` : `sleep 0.5`);
  
  console.log(`✅ 端口 ${port} 清理完成`);
}

// 执行端口清理
killPort(PORT);
