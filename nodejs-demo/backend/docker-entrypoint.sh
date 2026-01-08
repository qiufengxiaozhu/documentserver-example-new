#!/bin/sh
set -e

DB_FILE="/app/data/filez_demo.db"

# 初始化数据库（如果数据库文件不存在或为空）
if [ ! -f "$DB_FILE" ] || [ ! -s "$DB_FILE" ]; then
  echo "🔧 数据库文件不存在或为空，初始化数据库..."
  node dist/src/database/Seed.js || true
else
  # 检查是否有用户数据（简单检查文件中是否包含 admin 字符串）
  if ! grep -q "admin" "$DB_FILE" 2>/dev/null; then
    echo "🔧 数据库无用户数据，初始化数据库..."
    node dist/src/database/Seed.js || true
  fi
fi

# 启动应用
echo "🚀 启动服务..."
exec node dist/src/app.js

