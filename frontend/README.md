# AI Agent Platform 前端

基于 React + TypeScript + Vite 的前端项目，为 AI Agent Platform 提供用户界面。

## 技术栈

- **框架**: React 18 + TypeScript
- **构建工具**: Vite
- **UI库**: Ant Design 5.x
- **状态管理**: Zustand
- **路由**: React Router v6
- **HTTP客户端**: Axios

## 项目结构

```
src/
├── components/          # 通用组件
│   └── layout/          # 布局组件
├── pages/              # 页面组件
│   ├── auth/           # 认证页面
│   ├── agent/          # 智能体管理页面
│   ├── knowledge/      # 知识库管理页面
│   ├── workflow/       # 工作流管理页面
│   └── mcp/            # MCP服务市场页面
├── services/           # API服务
│   ├── api/            # 各模块API接口
│   └── http/           # HTTP客户端配置
├── stores/             # 状态管理
├── types/              # TypeScript类型定义
├── utils/              # 工具函数
└── assets/             # 静态资源
```

## 开发环境搭建

1. 确保已安装 Node.js (推荐 v18+)

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

4. 构建生产版本
```bash
npm run build
```

## 项目配置

### 环境变量

项目使用 Vite 的环境变量功能，可以在 `.env` 文件中配置：

```env
# API基础URL
VITE_API_BASE_URL=http://localhost:8080
```

### 路由配置

项目使用 React Router 进行路由管理，主要路由包括：
- `/` - 仪表板
- `/login` - 登录页面
- `/register` - 注册页面
- `/agents` - 智能体管理
- `/knowledge` - 知识库管理
- `/workflows` - 工作流管理
- `/mcp` - MCP服务市场

## 开发规范

### 代码风格
- 使用 TypeScript 进行类型检查
- 遵循 ESLint 规则
- 组件使用函数式组件 + Hooks

### 命名规范
- 组件文件名使用 PascalCase
- 组件内部变量使用 camelCase
- 类型定义文件使用小写 + `.ts` 后缀

### 目录规范
- 每个功能模块对应一个目录
- 组件按功能分组存放
- 类型定义统一放在 `types/` 目录

## API 接口

所有 API 接口通过 `services/api/` 目录下的文件进行封装，统一使用 Axios 进行 HTTP 请求。

## 状态管理

使用 Zustand 进行状态管理，主要存储用户认证信息等全局状态。

## 部署

构建后的文件位于 `dist/` 目录，可直接部署到静态服务器。