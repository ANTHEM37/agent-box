# 第四阶段：MCP 服务市场开发

## 🎯 阶段目标

构建一个完整的 MCP（Model Context Protocol）服务市场，实现服务的注册、发现、部署、监控和管理功能。

## 📋 功能规划

### 1. **MCP 服务管理**
- 🏪 服务注册和发布
- 🔍 服务发现和搜索
- 📊 服务详情和文档
- ⭐ 服务评分和评论
- 🏷️ 服务分类和标签

### 2. **服务部署系统**
- 🐳 Docker 容器化部署
- ⚡ 自动扩缩容
- 🔄 版本管理和回滚
- 🌐 负载均衡
- 📈 健康检查

### 3. **服务监控**
- 📊 实时监控面板
- 📈 性能指标统计
- 🚨 告警和通知
- 📋 日志管理
- 💰 使用计费

### 4. **开发者工具**
- 🛠️ SDK 和 CLI 工具
- 📚 API 文档生成
- 🧪 测试和调试工具
- 📦 服务模板
- 🔧 配置管理

## 🏗️ 技术架构

### 后端架构
```
mcp/
├── entity/                     # 实体类
│   ├── McpService.java        # MCP 服务实体
│   ├── ServiceVersion.java    # 服务版本
│   ├── ServiceDeployment.java # 服务部署
│   ├── ServiceInstance.java   # 服务实例
│   ├── ServiceReview.java     # 服务评论
│   └── ServiceUsage.java      # 使用统计
├── repository/                # 数据访问层
├── service/                   # 业务逻辑层
│   ├── McpServiceService.java
│   ├── DeploymentService.java
│   ├── MonitoringService.java
│   └── BillingService.java
├── controller/                # 控制器层
├── deployment/                # 部署管理
│   ├── DockerManager.java
│   ├── KubernetesManager.java
│   └── LoadBalancer.java
└── monitoring/                # 监控系统
    ├── MetricsCollector.java
    ├── AlertManager.java
    └── LogAggregator.java
```

### 前端架构
```
mcp/
├── types/
│   └── mcp.ts                 # MCP 类型定义
├── services/api/
│   └── mcp.ts                 # MCP API 服务
├── stores/
│   └── mcp.ts                 # MCP 状态管理
└── pages/mcp/
    ├── ServiceMarket.tsx      # 服务市场
    ├── ServiceDetail.tsx      # 服务详情
    ├── ServiceDeploy.tsx      # 服务部署
    ├── ServiceMonitor.tsx     # 服务监控
    └── DeveloperConsole.tsx   # 开发者控制台
```

## 📊 数据库设计

### MCP 服务表
```sql
CREATE TABLE mcp_services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    tags TEXT[],
    author_id BIGINT NOT NULL,
    repository_url VARCHAR(500),
    documentation_url VARCHAR(500),
    icon_url VARCHAR(500),
    price_model VARCHAR(20) DEFAULT 'FREE', -- FREE, PAID, FREEMIUM
    price_per_request DECIMAL(10,4),
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, DEPRECATED
    featured BOOLEAN DEFAULT FALSE,
    downloads_count BIGINT DEFAULT 0,
    rating_average DECIMAL(3,2) DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_versions (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    changelog TEXT,
    docker_image VARCHAR(200),
    config_schema JSONB,
    api_spec JSONB,
    is_latest BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_deployments (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    deployment_name VARCHAR(100) NOT NULL,
    config JSONB,
    status VARCHAR(20) DEFAULT 'DEPLOYING',
    endpoint_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_instances (
    id BIGSERIAL PRIMARY KEY,
    deployment_id BIGINT NOT NULL,
    container_id VARCHAR(100),
    host VARCHAR(100),
    port INTEGER,
    status VARCHAR(20) DEFAULT 'STARTING',
    health_check_url VARCHAR(500),
    last_health_check TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_reviews (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(service_id, user_id)
);

CREATE TABLE service_usage (
    id BIGSERIAL PRIMARY KEY,
    deployment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    request_count BIGINT DEFAULT 0,
    response_time_avg DECIMAL(10,2),
    error_count BIGINT DEFAULT 0,
    data_transfer_mb DECIMAL(15,2) DEFAULT 0,
    cost DECIMAL(10,4) DEFAULT 0,
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(deployment_id, user_id, date)
);
```

## 🚀 开发步骤

### 第一步：MCP 服务管理
1. 创建 MCP 服务实体和仓库
2. 实现服务注册和发布功能
3. 开发服务搜索和分类功能
4. 构建服务详情页面

### 第二步：服务部署系统
1. 集成 Docker 容器管理
2. 实现服务自动部署
3. 开发版本管理功能
4. 构建部署监控界面

### 第三步：监控和计费
1. 实现服务监控系统
2. 开发使用统计功能
3. 构建计费系统
4. 创建监控面板

### 第四步：开发者工具
1. 开发 SDK 和 CLI 工具
2. 实现 API 文档生成
3. 构建测试工具
4. 创建开发者控制台

## 📈 预期成果

- 🏪 完整的 MCP 服务市场
- 🐳 自动化部署系统
- 📊 实时监控和统计
- 💰 灵活的计费模式
- 🛠️ 丰富的开发者工具

让我们开始第四阶段的开发！