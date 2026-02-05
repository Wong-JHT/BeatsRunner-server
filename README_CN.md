# BeatRunner 后端服务

> 🏃‍♂️ AI 驱动的音乐同步跑步机训练平台

[![构建](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/kotlin-2.1-blue)]()
[![Ktor](https://img.shields.io/badge/ktor-2.3.12-orange)]()

**BeatRunner** 可根据音乐 BPM 同步跑步机速度和坡度，并使用 AI 基于用户生理数据个性化运动参数。

[English Documentation](README.md)

---

## 📚 文档导航

### 🚀 新手入门
- [**快速上手指南**](docs/GETTING_STARTED_CN.md) - 15分钟快速搭建
- [**Getting Started (English)**](docs/GETTING_STARTED.md) - Quick setup guide

### 👔 项目经理
- [**项目概览**](docs/ARCHITECTURE_CN.md) - 架构、技术栈和路线图
- [**Architecture (English)**](docs/ARCHITECTURE.md) - Technical overview

### 💻 开发人员
- [**API 文档**](docs/API_REFERENCE_CN.md) - 完整 API 接口文档
- [**API Reference (English)**](docs/API_REFERENCE.md) - Complete API docs
- [**部署指南**](docs/DEPLOYMENT_CN.md) - 生产环境部署
- [**Deployment (English)**](docs/DEPLOYMENT.md) - Production guide

---

## ⚡ 快速开始（30秒）

```bash
# 1. 克隆并配置
git clone <仓库地址>
cd BeatsRunner-server
cp .env.example .env

# 2. 编辑 .env 填入数据库凭据

# 3. 运行
./gradlew run
```

服务将在 **http://localhost:8080** 启动 ✅

**完整设置指南**：[快速上手](docs/GETTING_STARTED_CN.md)

---

## ✨ 核心功能

| 功能 | 说明 |
|------|------|
| 🎵 **AI 音乐分析** | DeepSeek 驱动的 BPM 到速度转换 |
| 👤 **用户画像** | 多维度生理数据 |
| 🔐 **多身份认证** | 邮箱、手机、Apple、微信 |
| 🏋️ **运动追踪** | 运动记录、音乐、指标 |
| 📊 **设备管理** | FTMS 跑步机能力限制 |
| 🔄 **实时状态** | Redis 支持的会话 |

---

## 🏗️ 技术栈

- **运行时**: Kotlin 2.1 + Ktor 2.3.12
- **数据库**: PostgreSQL 14+ (Exposed ORM)
- **缓存**: Redis 6+
- **AI**: DeepSeek API
- **认证**: JWT

---

## 📊 项目状态

| 组件 | 状态 | 版本 |
|------|------|------|
| 核心 API | ✅ 生产就绪 | 1.0.0 |
| 用户系统 | ✅ 完成 | 1.0.0 |
| AI 集成 | ✅ 完成 | 1.0.0 |
| 运动追踪 | ✅ 完成 | 1.0.0 |
| Redis 集成 | 🚧 进行中 | - |
| 实时 API | 📋 计划中 | - |

---

## 🎯 API 快速测试

```bash
# 健康检查
curl http://localhost:8080/health

# 注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"identityType":"email","identifier":"test@example.com","password":"Test123"}'

# 登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identityType":"email","identifier":"test@example.com","password":"Test123"}'
```

**完整 API 文档**：[API 参考](docs/API_REFERENCE_CN.md)

---

## 📁 仓库结构

```
BeatsRunner-server/
├── docs/                      # 📚 文档
│   ├── GETTING_STARTED_CN.md  # 🚀 快速开始
│   ├── API_REFERENCE_CN.md    # 💻 API 文档
│   ├── ARCHITECTURE_CN.md     # 👔 架构说明
│   └── DEPLOYMENT_CN.md       # 🚀 部署指南
├── src/
│   └── main/kotlin/com/beatrunner/
│       ├── Application.kt     # 入口文件
│       ├── auth/              # 认证
│       ├── services/          # 业务逻辑
│       ├── routes/            # API 端点
│       └── database/          # 数据层
├── .env.example               # 环境变量模板
├── build.gradle.kts           # 构建配置
└── README_CN.md               # 本文件
```

---

## 🤝 贡献

1. 阅读 [架构指南](docs/ARCHITECTURE_CN.md)
2. Fork 仓库
3. 创建特性分支
4. 提交 Pull Request

---

## 📄 许可证

MIT License - 查看 [LICENSE](LICENSE)

---

## 📧 支持

- 📖 [文档](docs/)
- 🐛 [报告问题](https://github.com/...)
- 💬 [讨论](https://github.com/.../discussions)

---

**由 BeatRunner 团队用 ❤️ 打造**
