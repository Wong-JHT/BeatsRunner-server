# BeatRunner 后端服务 - 启动指南

## 1. 环境配置

### 1.1 编辑 `.env` 文件

已为您创建 `.env` 文件，请根据您的实际环境修改以下配置：

```bash
# 数据库配置（PostgreSQL）
DB_HOST=localhost          # 数据库主机
DB_PORT=5432              # 数据库端口
DB_NAME=beatrunner        # 数据库名称
DB_USER=postgres          # 数据库用户名
DB_PASSWORD=your_password # ⚠️ 修改为您的实际密码

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT 配置
JWT_SECRET=your_jwt_secret_key_change_in_production  # ⚠️ 建议生成随机密钥
JWT_ISSUER=beatrunner
JWT_AUDIENCE=beatrunner-users
JWT_REALM=beatrunner

# DeepSeek AI 配置
DEEPSEEK_API_KEY=your_deepseek_api_key_here  # ⚠️ 填入您的 DeepSeek API Key

# 服务器配置
SERVER_PORT=8080
SERVER_HOST=0.0.0.0
```

### 1.2 生成安全的 JWT Secret（推荐）

```bash
# macOS/Linux
openssl rand -base64 32
```

将输出结果替换 `.env` 文件中的 `JWT_SECRET`

---

## 2. 数据库初始化

### 2.1 创建数据库

```bash
# 连接到 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE beatrunner;

# 退出
\q
```

### 2.2 验证数据库连接

```bash
psql -U postgres -d beatrunner -c "SELECT version();"
```

---

## 3. Redis 启动（如果本地运行）

### 使用 Docker（推荐）

```bash
docker run -d --name beatrunner-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 使用 Homebrew（macOS）

```bash
brew services start redis
```

### 验证 Redis

```bash
redis-cli ping
# 应返回: PONG
```

---

## 4. 启动服务

### 4.1 开发模式启动

```bash
# 方式 1: 使用 Gradle（推荐开发时使用）
./gradlew run

# 方式 2: 先编译再运行
./gradlew build -x test
java -jar build/libs/BeatsRunner-server-all.jar
```

### 4.2 验证服务启动

服务启动后会显示：

```
✅ BeatRunner Server started successfully!
🌐 Server running at http://0.0.0.0:8080
```

### 4.3 测试健康检查

```bash
curl http://localhost:8080/health
```

**预期响应：**
```json
{
  "status": "healthy",
  "service": "beatrunner-server",
  "version": "1.0.0"
}
```

---

## 5. 数据库表自动创建

服务首次启动时会自动创建以下表：

- `accounts` - 账户信息
- `identities` - 多身份认证
- `profiles` - 用户生理数据
- `settings` - 用户设置
- `user_devices` - 跑步机设备信息
- `workout_sessions` - 运动记录
- `workout_musics` - 音乐播放记录

### 验证表创建

```bash
psql -U postgres -d beatrunner

\dt  # 查看所有表

# 应该看到上述 7 个表
```

---

## 6. 测试 API（可选）

### 6.1 注册用户

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "identityType": "email",
    "identifier": "test@example.com",
    "password": "Test123456",
    "nickname": "测试用户"
  }'
```

**成功响应：**
```json
{
  "accountId": "uuid-here",
  "token": "jwt-token-here",
  "profileCompleted": false
}
```

### 6.2 登录

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identityType": "email",
    "identifier": "test@example.com",
    "password": "Test123456"
  }'
```

### 6.3 获取用户资料（需要 JWT）

```bash
# 使用上面获取的 token
TOKEN="your-jwt-token-here"

curl http://localhost:8080/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

## 7. 常见问题排查

### 问题 1: 数据库连接失败

**错误信息：** `Connection refused` 或 `password authentication failed`

**解决方案：**
1. 检查 PostgreSQL 是否运行: `pg_isready`
2. 验证 `.env` 中的数据库配置
3. 检查 `pg_hba.conf` 认证配置

### 问题 2: Redis 连接失败

**错误信息：** `Could not connect to Redis`

**解决方案：**
1. 检查 Redis 是否运行: `redis-cli ping`
2. 验证 `.env` 中的 Redis 配置

### 问题 3: 端口被占用

**错误信息：** `Address already in use`

**解决方案：**
```bash
# 查找占用 8080 端口的进程
lsof -i :8080

# 修改 .env 中的 SERVER_PORT
```

### 问题 4: DeepSeek API 未配置

服务可以正常启动，但 AI 分析功能需要有效的 API Key。

**解决方案：**
- 前往 [DeepSeek 官网](https://platform.deepseek.com/) 获取 API Key
- 更新 `.env` 中的 `DEEPSEEK_API_KEY`
- 重启服务

---

## 8. 生产环境部署建议

### 8.1 环境变量安全

- ⚠️ **绝不要提交 `.env` 文件到 Git**
- 生产环境使用环境变量或密钥管理服务
- JWT_SECRET 使用至少 32 字节的随机密钥

### 8.2 数据库

- 生产环境建议使用托管的 PostgreSQL（如 AWS RDS）
- 配置数据库备份策略
- 启用 SSL 连接

### 8.3 Redis

- 生产环境建议使用 Redis Cluster
- 配置持久化（RDB + AOF）
- 启用密码认证

### 8.4 性能优化

- 使用反向代理（Nginx）
- 配置连接池大小
- 启用 Gzip 压缩

---

## 9. 停止服务

```bash
# Ctrl+C 停止 ./gradlew run

# 或者找到进程并停止
ps aux | grep beatrunner
kill <PID>
```

---

## 完整启动清单 ✅

- [ ] PostgreSQL 已安装并运行
- [ ] Redis 已安装并运行
- [ ] 已创建数据库 `beatrunner`
- [ ] 已配置 `.env` 文件（密码、JWT Secret、API Key）
- [ ] 已生成安全的 JWT Secret
- [ ] 运行 `./gradlew run`
- [ ] 访问 `http://localhost:8080/health` 验证
- [ ] 测试注册/登录 API

**祝您使用愉快！** 🎉
