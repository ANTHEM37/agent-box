## 前端应用

### 本地启动

```bash
cd frontend
npm install
# 如无 .env，请创建并设置 VITE_API_BASE_URL
npm run dev
```

默认地址：`http://localhost:3000`

### 环境变量

在 `.env` 中配置：

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=AI Agent Platform
```

### 目录结构

- `src/services`：HTTP 客户端与 API 封装
- `src/stores`：全局状态（Zustand）
- `src/components/layout`：布局组件
- `src/pages`：页面（登录、概览、知识库、工作流、MCP）

### 登录

后端启用后，使用已注册账号登录；成功后可访问各模块页面。
