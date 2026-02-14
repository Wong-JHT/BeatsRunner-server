# Spotify 开发者后台配置指南 (解决 403 错误)

如果你遇到了 **403 Forbidden** 错误，且报错信息为 `"Check settings on developer.spotify.com/dashboard, the user may not be registered"`，请按照以下步骤解决。

## 为什么会出现这个问题？

即使你使用的是 **公开搜索 API** (Public Search) 且使用的是 **Client Credentials Flow** (不需要用户登录)，Spotify 对处于 **Development Mode (开发模式)** 的应用也会实施严格限制。

在开发模式下，Spotify **拦截所有 API 请求**，除非这些请求来自（或关联到）已在后台白名单中注册的用户。这是 Spotify 的反滥用机制。

## 解决方法

### 1. 登录开发者后台
访问 [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard) 并登录你的 Spotify 账号。

### 2. 选择你的应用
点击你为 **BeatsRunner** 创建的应用 (Client ID: `470833c0fbd64c03b44c23fa7a532ee4`)。

### 3. 添加用户 (关键步骤！)
1.  点击 **Settings** (设置) 按钮。
2.  找到 **User Management** (用户管理) 部分。
3.  点击 **Add New User** (添加新用户)。
4.  输入你的 **Spotify 账号邮箱** (Name 和 Email)。
5.  点击 **Add** (添加)。

**注意**：必须添加你当前使用的 Client ID 所归属的账号邮箱，或者你打算用来测试的账号邮箱。

### 4. 重试
添加完成后，等待约 1 分钟，然后再次运行服务器或测试脚本。此时 403 错误应该会消失。

---

**只有当你申请将应用切换到 "Quota Extension" (生产模式) 后，这个限制才会解除，届时所有的 Spotify 用户（包括未注册用户）才能通过你的 App 访问公开数据。但在目前的开发阶段，必须手动添加用户。**
