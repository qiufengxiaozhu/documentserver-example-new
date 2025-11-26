# Filez Demo - 文档管理集成示例项目

[![许可证](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)

[English](README.md) | 简体中文

## 📖 项目简介

Filez Demo 是一个基于 Spring Boot 的文档管理集成示例项目，主要用于演示如何与 Filez 文档中台进行集成。项目提供了完整的文档上传、下载、编辑、预览、对比等功能。

**⚠️ 重要提示**: 本项目仅用于**测试和演示目的**。请勿在生产服务器上直接使用此集成示例，必须进行适当的代码修改和安全加固!

## ✨ 核心功能

### 🔐 用户认证系统
- 基于 JWT 的身份认证
- 用户会话管理
- 登录/登出功能
- 用户信息管理

### 📁 文档管理功能
- **上传**: 支持单文件和批量文件上传
- **下载**: 文档内容获取
- **删除**: 单个和批量文件删除
- **新建**: 创建新文档
- **预览**: 实时文档预览

### 📝 ZOffice 集成功能
- 在线文档编辑
- 实时协作
- 文档对比
- 版本控制
- 评论和提及通知
- 支持多种格式 (Word、Excel、PowerPoint、PDF)

### 🔧 系统管理功能
- 内嵌 SQLite 数据库
- 文件存储管理
- API 文档 (Swagger/Knife4j)
- 完善的日志系统

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 2.5.4
- **数据库**: SQLite (内嵌数据库，无需额外安装)
- **ORM**: MyBatis Plus 3.4.3.4
- **模板引擎**: FreeMarker
- **API文档**: Knife4j (Swagger)
- **JSON处理**: Fastjson 1.2.83
- **JWT**: JJWT 0.9.1
- **HTTP客户端**: Apache HttpClient 4.5.13

### 项目结构
```
filez-demo/
├── src/main/java/com/filez/demo/
│   ├── common/                # 公共组件
│   │   ├── aspect/            # AOP切面 (日志记录)
│   │   ├── constant/          # 常量定义
│   │   ├── context/           # 上下文管理 (用户上下文)
│   │   ├── interceptor/       # 拦截器 (登录拦截)
│   │   ├── listener/          # 监听器
│   │   └── utils/             # 工具类 (JWT等)
│   ├── config/                # 配置类
│   │   ├── DatabaseConfig.java    # 数据库配置
│   │   ├── DemoConfig.java        # 业务配置
│   │   ├── SwaggerConfig.java     # API文档配置
│   │   └── ZOfficeConfig.java     # ZOffice集成配置
│   ├── controller/            # 控制器层
│   │   ├── LoginController.java   # 登录控制器
│   │   ├── HomeController.java    # 主页控制器
│   │   ├── FileController.java    # 文件操作控制器
│   │   └── ZOfficeController.java # ZOffice集成控制器
│   ├── dao/                   # 数据访问层
│   ├── entity/                # 实体类
│   ├── model/                 # 数据模型
│   ├── service/               # 业务逻辑层
│   └── FilezDemoApplication.java  # 启动类
├── src/main/resources/
│   ├── application.yml        # 主配置文件
│   ├── zoffice.yml           # ZOffice集成配置
│   ├── mapper/               # MyBatis映射文件
│   ├── sql/                  # 数据库脚本
│   ├── static/               # 静态资源
│   └── templates/            # FreeMarker模板
├── data/                     # SQLite数据库文件
├── local-file/              # 本地文件存储
└── logs/                    # 日志文件
```

## 🚀 快速开始

### 前置要求
在开始之前，请确保已安装以下软件：
- **Java**: JDK 8 或更高版本 ([下载地址](https://www.oracle.com/java/technologies/downloads/))
- **Maven**: 3.6 或更高版本 ([下载地址](https://maven.apache.org/download.cgi))
- **Git**: 用于克隆代码仓库

### 安装步骤

#### 步骤 1: 克隆代码仓库
```bash
git clone <repository-url>
cd filez-demo
```

#### 步骤 2: 编译项目
```bash
# 清理并编译
mvn clean package

# 跳过测试（可选）
mvn clean package -DskipTests
```

编译成功后，将在 `target/` 目录下生成 `filez-demo-1.0.0.RELEASE.jar` 文件。

#### 步骤 3: 配置应用

项目支持多种配置方式，可根据部署环境选择合适的配置方法：

##### 方式 1: 使用内置配置（推荐用于开发环境）
直接使用项目内置的 SQLite 数据库和默认配置：

```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar
```

##### 方式 2: 使用外部配置文件
在 JAR 文件同级目录下创建 `application-external.yml` 文件：

```yaml
server:
  port: 8000

zoffice:
  service:
    host: 172.16.34.165    # ZOffice 服务器地址
    port: 8001              # ZOffice 服务器端口

demo:
  host: 172.16.34.165      # 当前应用服务器地址
  context: /v2/context
  repoId: 3rd-party
```

然后使用以下命令启动：
```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar --spring.profiles.active=external
```

##### 方式 3: 使用命令行参数
```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar \
  --server.port=8000 \
  --zoffice.service.host=172.16.34.165 \
  --zoffice.service.port=8001 \
  --demo.host=172.16.34.165
```

##### 方式 4: 使用环境变量
```bash
export SERVER_PORT=8000
export ZOFFICE_SERVICE_HOST=172.16.34.165
export ZOFFICE_SERVICE_PORT=8001
export DEMO_HOST=172.16.34.165
java -jar target/filez-demo-1.0.0.RELEASE.jar
```

#### 步骤 4: 运行应用

##### 开发环境（前台运行）
```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar
```

##### 生产环境（后台运行）
```bash
# Linux/macOS
nohup java -jar target/filez-demo-1.0.0.RELEASE.jar --spring.profiles.active=external > logs/app.log 2>&1 &

# Windows (使用 PowerShell)
Start-Process java -ArgumentList "-jar","target/filez-demo-1.0.0.RELEASE.jar" -WindowStyle Hidden
```

#### 步骤 5: 验证安装

启动成功后，验证应用是否正常运行：

```bash
# 检查应用是否响应
curl http://localhost:8000

# 或在浏览器中打开
# http://localhost:8000
```

### 🛑 停止服务

#### 查找进程
```bash
# Linux/macOS - 查看端口占用
sudo netstat -tunlp | grep ':8000'
# 或
lsof -i :8000

# Windows - 查看端口占用
netstat -ano | findstr :8000

# 查看 Java 进程
# Linux/macOS
ps aux | grep filez-demo

# Windows
tasklist | findstr java
```

#### 停止进程
```bash
# Linux/macOS - 优雅停止（推荐）
kill <PID>

# Linux/macOS - 强制停止（谨慎使用）
kill -9 <PID>

# Linux/macOS - 停止所有相关进程
pkill -f filez-demo

# Windows - 根据 PID 停止
taskkill /PID <PID> /F
```

## 🌐 访问应用

启动成功后，可通过以下地址访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| **主页** | http://localhost:8000 | 应用主入口 |
| **登录页** | http://localhost:8000/login | 用户认证 |
| **API 文档** | http://localhost:8000/doc.html | Swagger/Knife4j API 文档 |
| **文件管理** | http://localhost:8000/home/local | 文档管理界面 |

### 默认登录凭据

测试环境使用以下默认账号：

| 字段 | 值 |
|------|-----|
| **用户名** | `admin` |
| **密码** | `zOffice` |

⚠️ **安全警告**: 部署到生产环境前请务必修改默认凭据!

## 📚 API 接口文档

应用提供了完整的 REST API。启动应用后，访问 http://localhost:8000/doc.html 查看详细的 API 文档。

### 认证接口

| 方法 | 端点 | 说明 |
|------|------|------|
| `GET` | `/login` | 显示登录页面 |
| `POST` | `/login` | 用户认证 |
| `GET` | `/logout` | 用户登出 |

### 文件管理接口

| 方法 | 端点 | 说明 |
|------|------|------|
| `POST` | `/v2/context/file/upload` | 上传单个文件 |
| `POST` | `/v2/context/file/batchOp/upload` | 批量上传文件 |
| `DELETE` | `/v2/context/file/delete/{docId}` | 根据 ID 删除文件 |
| `POST` | `/v2/context/file/batchOp/delete` | 批量删除文件 |
| `POST` | `/v2/context/file/new` | 创建新文档 |

### ZOffice 集成接口

| 方法 | 端点 | 说明 |
|------|------|------|
| `GET` | `/v2/context/openDoc` | 获取集成文档的URL |
| `GET` | `/v2/context/{docId}/content` | 下载文档内容 |
| `POST` | `/v2/context/{docId}/content` | 上传文档内容 |
| `GET` | `/v2/context/{docId}/meta` | 获取文档元数据 |
| `GET` | `/v2/context/profiles` | 获取用户信息 |
| `POST` | `/v2/context/{docId}/notify` | 文档状态通知回调 |
| `POST` | `/v2/context/{docId}/mention` | 文档提及通知 |
| `GET` | `/v2/context/compareDoc` | 对比两个文档 |

### 页面路由

| 方法 | 端点 | 说明 |
|------|------|------|
| `GET` | `/home/` | 应用主页 |
| `GET` | `/home/local` | 文件列表和管理 |
| `GET` | `/home/user` | 用户信息页面 |
| `GET` | `/home/compare` | 文档对比界面 |

## 🔍 监控与故障排查

### 日志文件

应用日志存储在 `logs/` 目录下：

```bash
# Linux/macOS - 查看实时日志
tail -f logs/filezDemo.log

# Linux/macOS - 查看错误日志
grep -i error logs/filezDemo.log

# Linux/macOS - 查看最近的日志
tail -n 100 logs/filezDemo.log

# Windows - 查看实时日志
Get-Content logs/filezDemo.log -Wait -Tail 50

# Windows - 搜索错误
Select-String -Path logs/filezDemo.log -Pattern "error" -CaseSensitive:$false
```

### 常见问题

#### 端口已被占用
```bash
# 在配置中更改端口
--server.port=8080
```

#### 数据库连接问题
- 确保 `data/` 目录具有读写权限
- 检查 SQLite 数据库文件是否存在且未损坏

#### ZOffice 集成问题
- 验证 ZOffice 服务是否运行且可访问
- 检查 `zoffice.service.host` 和 `zoffice.service.port` 配置
- 确保服务之间的网络连通性

## 👨‍💻 开发指南

### 搭建开发环境

1. **安装前置软件**
   - Java JDK 8 或更高版本
   - Maven 3.6 或更高版本
   - IDE（推荐使用 IntelliJ IDEA）

2. **克隆并导入项目**
   ```bash
   git clone <repository-url>
   cd filez-demo
   ```

3. **导入到 IDE**
   - 打开 IntelliJ IDEA
   - 文件 → 打开 → 选择 `filez-demo` 目录
   - 等待 Maven 下载依赖

4. **运行应用**
   - 找到 `FilezDemoApplication.java`
   - 右键 → 运行 'FilezDemoApplication.main()'
   - 或使用 Maven: `mvn spring-boot:run`

### 项目结构说明

```
src/main/java/com/filez/demo/
├── common/              # 共享组件
│   ├── aspect/         # AOP 切面，用于日志和横切关注点
│   ├── constant/       # 应用常量
│   ├── context/        # 请求上下文管理
│   ├── interceptor/    # HTTP 拦截器（认证等）
│   └── utils/          # 工具类（JWT等）
├── config/             # Spring 配置类
├── controller/         # REST API 控制器
├── dao/                # 数据访问层（MyBatis）
├── entity/             # 数据库实体
├── model/              # DTO 和请求/响应模型
└── service/            # 业务逻辑层
```

### 生产环境构建

```bash
# 带测试构建
mvn clean package

# 不带测试构建（更快）
mvn clean package -DskipTests

# 使用特定配置文件构建
mvn clean package -Pproduction
```

## 🔒 重要安全考虑

**⚠️ 这是一个演示项目。部署到生产环境前，请考虑以下安全措施：**

1. **认证与授权**
   - 实现完善的用户认证机制
   - 添加基于角色的访问控制（RBAC）
   - 使用强密码策略
   - 启用 HTTPS/TLS 加密

2. **文件存储安全**
   - 验证文件类型和大小
   - 实施病毒扫描
   - 限制文件访问权限
   - 使用安全的文件存储位置

3. **API 安全**
   - 启用 JWT 令牌验证
   - 实施速率限制
   - 添加请求验证和清理
   - 正确配置 CORS

4. **数据库安全**
   - 使用生产级数据库（PostgreSQL、MySQL）
   - 实施适当的备份策略
   - 加密敏感数据
   - 使用参数化查询（已通过 MyBatis 实现）

5. **网络安全**
   - 部署在反向代理后（Nginx、Apache）
   - 配置防火墙规则
   - 使用私有网络进行服务通信
   - 实施网络分段

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 详见 LICENSE 文件。

## 📞 技术支持

如果遇到任何问题或有疑问：

- **问题反馈**: 在 GitHub 上提交 Issue
- **文档**: 查看 `/doc.html` 的 API 文档
- **邮箱**: 联系技术支持团队

## 🙏 致谢

- 基于 [Spring Boot](https://spring.io/projects/spring-boot) 构建
- 集成 ZOffice 文档服务

---

**免责声明**: 本项目是用于演示和测试目的的集成示例。在生产环境中使用前，请根据具体业务需求进行适当的修改、安全加固和优化。
