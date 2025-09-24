# AI Agent Platform

基于 LangChain4j 的智能平台，集成知识库、工作流引擎和 MCP 服务市场功能。

## 功能特性

### 🧠 知识库模块
- 多格式文档上传解析 (PDF, Word, Markdown, TXT)
- 智能文档分块和向量化存储
- 语义检索和相似度搜索
- RAG 问答接口

### 🔄 工作流引擎
- 可视化工作流设计器
- 预置节点类型 (LLM、知识库检索、条件判断等)
- 工作流执行引擎和版本管理
- 变量传递和上下文管理

### 🛒 MCP 服务市场
- MCP 服务注册和发现
- 服务容器化部署和调用
- 服务监控和统计
- 服务评分和计费

## 技术栈

### 后端
- Spring Boot 3.2 + LangChain4j
- PostgreSQL + Chroma (向量数据库)
- Redis + MinIO
- Docker + Docker Compose

### 前端
- React 18 + TypeScript + Vite
- Ant Design 5.x + React Flow
- Zustand + React Query

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 15+

### 启动步骤

1. 克隆项目
```bash
git clone <repository-url>
cd agent-box
```

2. 启动基础服务
```bash
docker-compose up -d postgres redis chroma minio
```

3. 启动后端服务
```bash
cd backend
./mvnw spring-boot:run
```

4. 启动前端服务
```bash
cd frontend
npm install
npm run dev
```

5. 访问应用
- 前端界面: http://localhost:3000
- 后端API: http://localhost:8080
- MinIO控制台: http://localhost:9001

## 项目结构

```
agent-box/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/agent/platform/
│   │       ├── knowledge/   # 知识库模块
│   │       ├── workflow/    # 工作流模块
│   │       ├── mcp/         # MCP服务市场
│   │       └── common/      # 公共组件
│   └── pom.xml
├── frontend/                # React 前端
│   ├── src/
│   │   ├── components/      # 通用组件
│   │   ├── pages/          # 页面组件
│   │   ├── services/       # API服务
│   │   └── stores/         # 状态管理
│   └── package.json
├── docker-compose.yml       # 容器编排
└── README.md
```

## API 文档

启动后端服务后，访问 http://localhost:8080/swagger-ui.html 查看完整的 API 文档。

## 许可证

MIT License