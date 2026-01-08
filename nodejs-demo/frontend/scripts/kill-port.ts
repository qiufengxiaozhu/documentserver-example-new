/**
 * 跨平台端口清理脚本
 * 检测并 kill 占用指定端口的进程
 */
import { execSync } from 'child_process';

const PORT = process.env.PORT || 5173;

function killPort(port: number | string): void {
  const isWindows = process.platform === 'win32';
  
  console.log(`🔍 检查端口 ${port} 是否被占用...`);
  
  try {
    if (isWindows) {
      // Windows: 查找占用端口的进程
      const result = execSync(`netstat -ano | findstr :${port}`, { 
        encoding: 'utf8', 
        stdio: ['pipe', 'pipe', 'pipe'] 
      });
      const lines = result.trim().split('\n');
      
      const pids = new Set<string>();
      for (const line of lines) {
        // 匹配 LISTENING 状态的进程
        if (line.includes('LISTENING')) {
          const parts = line.trim().split(/\s+/);
          const pid = parts[parts.length - 1];
          if (pid && pid !== '0') {
            pids.add(pid);
          }
        }
      }
      
      if (pids.size > 0) {
        for (const pid of pids) {
          console.log(`⚠️  端口 ${port} 被进程 ${pid} 占用，正在终止...`);
          try {
            execSync(`taskkill /F /PID ${pid}`, { stdio: 'inherit' });
            console.log(`✅ 进程 ${pid} 已终止`);
          } catch {
            console.log(`❌ 无法终止进程 ${pid}，可能需要管理员权限`);
          }
        }
      } else {
        console.log(`✅ 端口 ${port} 未被占用`);
      }
    } else {
      // Linux/Mac: 使用 lsof 查找
      const result = execSync(`lsof -ti:${port}`, { 
        encoding: 'utf8', 
        stdio: ['pipe', 'pipe', 'pipe'] 
      });
      const pids = result.trim().split('\n').filter(Boolean);
      
      if (pids.length > 0) {
        for (const pid of pids) {
          console.log(`⚠️  端口 ${port} 被进程 ${pid} 占用，正在终止...`);
          try {
            execSync(`kill -9 ${pid}`, { stdio: 'inherit' });
            console.log(`✅ 进程 ${pid} 已终止`);
          } catch {
            console.log(`❌ 无法终止进程 ${pid}`);
          }
        }
      } else {
        console.log(`✅ 端口 ${port} 未被占用`);
      }
    }
  } catch {
    // 如果命令执行失败，说明端口没有被占用
    console.log(`✅ 端口 ${port} 未被占用`);
  }
}

killPort(PORT);

