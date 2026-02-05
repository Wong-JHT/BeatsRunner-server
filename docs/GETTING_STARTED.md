# Getting Started with BeatRunner Backend

> 🚀 Get your BeatRunner server running in 15 minutes

This guide will help you set up the BeatRunner backend server from scratch, even if you're new to backend development.

---

## 📋 What You'll Need

Before starting, ensure you have:

- ✅ **Java 17 or newer** - [Download here](https://adoptium.net/)
- ✅ **PostgreSQL 14+** - [Download here](https://www.postgresql.org/download/)
- ✅ **Redis** - [Installation guide](https://redis.io/docs/getting-started/)
- ✅ **Code editor** - VS Code, IntelliJ IDEA, or any text editor
- ⚠️ **DeepSeek API Key** (optional for AI features) - [Get it here](https://platform.deepseek.com/)

**Don't worry!** We'll guide you through everything step by step.

---

## 🎯 Step 1: Install Prerequisites

### Option A: Using Docker (Easiest)

```bash
# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop

# Start PostgreSQL
docker run -d --name beatrunner-postgres \
  -e POSTGRES_DB=beatrunner \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=yourpassword \
  -p 5432:5432 \
  postgres:16

# Start Redis
docker run -d --name beatrunner-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Option B: Manual Installation

**macOS (using Homebrew)**:
```bash
# Install PostgreSQL
brew install postgresql@16
brew services start postgresql@16

# Install Redis
brew install redis
brew services start redis
```

**Windows**:
- PostgreSQL: Download installer from [postgresql.org](https://www.postgresql.org/download/windows/)
- Redis: Use [Memurai](https://www.memurai.com/) (Redis for Windows)

**Linux (Ubuntu/Debian)**:
```bash
# PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Redis
sudo apt install redis-server
```

---

## 🎯 Step 2: Download the Project

```bash
# Clone the repository (replace with actual URL)
git clone https://github.com/your-org/BeatsRunner-server.git

# Navigate to the project folder
cd BeatsRunner-server
```

---

## 🎯 Step 3: Configure Your Environment

### 3.1 Create Configuration File

```bash
# Copy the example file
cp .env.example .env
```

### 3.2 Edit `.env` File

Open `.env` in your text editor and update these values:

```bash
# Database settings
DB_HOST=localhost
DB_PORT=5432
DB_NAME=beatrunner
DB_USER=postgres
DB_PASSWORD=yourpassword          # ⚠️ Change this!

# Redis settings
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Security (generate a secure key)
JWT_SECRET=paste-generated-key-here   # ⚠️ See next step
JWT_ISSUER=beatrunner
JWT_AUDIENCE=beatrunner-users

# AI (optional - get from platform.deepseek.com)
DEEPSEEK_API_KEY=sk-your-api-key-here

# Server
SERVER_PORT=8080
SERVER_HOST=0.0.0.0
```

### 3.3 Generate JWT Secret

Run this command to generate a secure JWT secret:

```bash
# macOS/Linux
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

Copy the output and paste it as your `JWT_SECRET` in `.env`.

---

## 🎯 Step 4: Create the Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE beatrunner;

# Exit PostgreSQL
\q
```

**Verify it worked**:
```bash
psql -U postgres -d beatrunner -c "SELECT version();"
```

You should see PostgreSQL version information.

---

## 🎯 Step 5: Start the Server

```bash
# Make the gradle wrapper executable (macOS/Linux)
chmod +x gradlew

# Run the server
./gradlew run
```

**On Windows**:
```bash
gradlew.bat run
```

### What to Expect

You'll see output like this:

```
> Task :run
15:37:10 INFO  - BeatRunner Server started successfully!
🌐 Server running at http://0.0.0.0:8080
```

**✅ Congratulations! Your server is running!**

---

## 🎯 Step 6: Test Your Server

Open a new terminal window (keep the server running) and try these commands:

### Test 1: Health Check

```bash
curl http://localhost:8080/health
```

**Expected response**:
```json
{"status":"healthy","service":"beatrunner-server","version":"1.0.0"}
```

### Test 2: Register a User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "identityType": "email",
    "identifier": "test@example.com",
    "password": "Test123456",
    "nickname": "Test Runner"
  }'
```

**You should get**:
```json
{
  "accountId": "some-uuid",
  "token": "jwt-token-here",
  "profileCompleted": false
}
```

### Test 3: Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identityType": "email",
    "identifier": "test@example.com",
    "password": "Test123456"
  }'
```

**✅ If all three tests work, you're all set!**

---

## 🎯 Step 7: Explore the API

Now that everything works, you can:

1. **Read the full API documentation**: [API Reference](API_REFERENCE.md)
2. **Test more endpoints** with tools like:
   - [Postman](https://www.postman.com/)
   - [Insomnia](https://insomnia.rest/)
   - [Bruno](https://www.usebruno.com/)

---

## 🛠️ Troubleshooting

### Problem: "Connection refused" when starting server

**Solution**: Make sure PostgreSQL and Redis are running:

```bash
# Check PostgreSQL
pg_isready

# Check Redis
redis-cli ping  # Should return: PONG
```

### Problem: "Database 'beatrunner' does not exist"

**Solution**: Create the database:

```bash
psql -U postgres -c "CREATE DATABASE beatrunner;"
```

### Problem: "Port 8080 already in use"

**Solution**: Either:
- Stop the other application using port 8080
- Change `SERVER_PORT` in `.env` to a different port (e.g., 8081)

### Problem: Gradle build errors

**Solution**: Make sure you have Java 17+:

```bash
java -version
```

If not, install Java 17 from [Adoptium](https://adoptium.net/).

### Problem: JWT token errors

**Solution**: Make sure your `JWT_SECRET` is properly set in `.env` and is at least 32 characters long.

---

## 📱 Next Steps

Now that your server is running:

1. 📖 Read the [API Reference](API_REFERENCE.md) to learn all endpoints
2. 🏗️ Understand the [Architecture](ARCHITECTURE.md)
3. 🚀 Learn about [Production Deployment](DEPLOYMENT.md)
4. 💻 Start building your frontend/mobile app!

---

## 🆘 Need Help?

- 📖 Check the [Full Documentation](../README.md)
- 🐛 [Report an Issue](https://github.com/.../issues)
- 💬 [Ask in Discussions](https://github.com/.../discussions)

---

**Happy coding! 🎉**
