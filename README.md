# BeatRunner Backend Server

> 🏃‍♂️ AI-driven music-synchronized treadmill training platform

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/kotlin-2.1-blue)]()
[![Ktor](https://img.shields.io/badge/ktor-2.3.12-orange)]()

**BeatRunner** synchronizes treadmill speed and incline with music BPM, using AI to personalize workout parameters based on user physiological data.

[中文文档](README_CN.md)

---

## 📚 Documentation

### 🚀 For Beginners
- [**Getting Started Guide**](docs/GETTING_STARTED.md) - Quick setup in 15 minutes
- [**Getting Started (中文)**](docs/GETTING_STARTED_CN.md) - 15分钟快速上手

### 👔 For Project Managers
- [**Project Overview**](docs/ARCHITECTURE.md) - Architecture, tech stack, and roadmap 
- [**项目概览（中文）**](docs/ARCHITECTURE_CN.md) - 架构、技术栈和路线图

### 💻 For Developers
- [**API Reference**](docs/API_REFERENCE.md) - Complete API documentation
- [**API 文档（中文）**](docs/API_REFERENCE_CN.md) - 完整 API 文档
- [**Deployment Guide**](docs/DEPLOYMENT.md) - Production deployment
- [**部署指南（中文）**](docs/DEPLOYMENT_CN.md) - 生产环境部署

---

## ⚡ Quick Start (30 seconds)

```bash
# 1. Clone and setup
git clone <repo-url>
cd BeatsRunner-server
cp .env.example .env

# 2. Edit .env with your database credentials

# 3. Run
./gradlew run
```

Server starts at **http://localhost:8080** ✅

**Full setup guide**: [Getting Started](docs/GETTING_STARTED.md)

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🎵 **AI Music Analysis** | DeepSeek-powered BPM-to-speed conversion |
| 👤 **User Profiles** | Multi-dimensional physiological data |
| 🔐 **Multi-Auth** | Email, Phone, Apple, WeChat |
| 🏋️ **Workout Tracking** | Sessions, music, metrics |
| 📊 **Device Management** | FTMS treadmill constraints |
| 🔄 **Real-time State** | Redis-backed sessions |

---

## 🏗️ Tech Stack

- **Runtime**: Kotlin 2.1 + Ktor 2.3.12
- **Database**: PostgreSQL 14+ (Exposed ORM)
- **Cache**: Redis 6+
- **AI**: DeepSeek API
- **Auth**: JWT

---

## 📊 Project Status

| Component | Status | Version |
|-----------|--------|---------|
| Core API | ✅ Production Ready | 1.0.0 |
| User System | ✅ Complete | 1.0.0 |
| AI Integration | ✅ Complete | 1.0.0 |
| Workout Tracking | ✅ Complete | 1.0.0 |
| Redis Integration | 🚧 In Progress | - |
| Real-time API | 📋 Planned | - |

---

## 🎯 API Quick Test

```bash
# Health check
curl http://localhost:8080/health

# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"identityType":"email","identifier":"test@example.com","password":"Test123"}'

# Login  
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identityType":"email","identifier":"test@example.com","password":"Test123"}'
```

**Full API documentation**: [API Reference](docs/API_REFERENCE.md)

---

## 📁 Repository Structure

```
BeatsRunner-server/
├── docs/                    # 📚 Documentation
│   ├── GETTING_STARTED.md   # 🚀 Quick start
│   ├── API_REFERENCE.md     # 💻 API docs
│   ├── ARCHITECTURE.md      # 👔 Architecture
│   └── DEPLOYMENT.md        # 🚀 Deployment
├── src/
│   └── main/kotlin/com/beatrunner/
│       ├── Application.kt   # Entry point
│       ├── auth/            # Authentication
│       ├── services/        # Business logic
│       ├── routes/          # API endpoints
│       └── database/        # Data layer
├── .env.example             # Environment template
├── build.gradle.kts         # Build config
└── README.md                # This file
```

---

## 🤝 Contributing

1. Read [Architecture Guide](docs/ARCHITECTURE.md)
2. Fork repository
3. Create feature branch
4. Submit pull request

---

## 📄 License

MIT License - See [LICENSE](LICENSE)

---

## 📧 Support

- 📖 [Documentation](docs/)
- 🐛 [Report Issues](https://github.com/...)
- 💬 [Discussions](https://github.com/.../discussions)

---

**Built with ❤️ by the BeatRunner Team**
