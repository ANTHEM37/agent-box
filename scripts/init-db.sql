-- 创建数据库
CREATE DATABASE agent_platform;

-- 切换到目标数据库
\c agent_platform;

-- 启用 UUID 扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建知识库表
CREATE TABLE knowledge_bases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    embedding_model VARCHAR(50) DEFAULT 'text-embedding-ada-002',
    chunk_size INTEGER DEFAULT 500,
    chunk_overlap INTEGER DEFAULT 50,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建文档表
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL REFERENCES knowledge_bases(id),
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    file_path VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PROCESSING',
    error_message TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 创建文档块表
CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id),
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建工作流表
CREATE TABLE workflows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    definition JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建工作流执行记录表
CREATE TABLE workflow_executions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL REFERENCES workflows(id),
    input_data JSONB,
    output_data JSONB,
    status VARCHAR(20) DEFAULT 'RUNNING',
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 创建 MCP 服务表
CREATE TABLE mcp_services (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    docker_image VARCHAR(255) NOT NULL,
    version VARCHAR(20) NOT NULL,
    pricing_model VARCHAR(20) DEFAULT 'FREE',
    price DECIMAL(10,2) DEFAULT 0.00,
    category VARCHAR(50),
    tags TEXT[],
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建服务调用记录表
CREATE TABLE service_invocations (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES mcp_services(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    input_data JSONB,
    output_data JSONB,
    status VARCHAR(20) DEFAULT 'SUCCESS',
    execution_time INTEGER, -- 毫秒
    cost DECIMAL(10,4) DEFAULT 0.0000,
    invoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_knowledge_bases_user_id ON knowledge_bases(user_id);
CREATE INDEX idx_documents_knowledge_base_id ON documents(knowledge_base_id);
CREATE INDEX idx_document_chunks_document_id ON document_chunks(document_id);
CREATE INDEX idx_workflows_user_id ON workflows(user_id);
CREATE INDEX idx_workflow_executions_workflow_id ON workflow_executions(workflow_id);
CREATE INDEX idx_mcp_services_user_id ON mcp_services(user_id);
CREATE INDEX idx_service_invocations_service_id ON service_invocations(service_id);
CREATE INDEX idx_service_invocations_user_id ON service_invocations(user_id);

-- 插入测试数据
INSERT INTO users (username, email, password_hash, full_name) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvuxL7cyGH0B7uWy', '系统管理员'),
('developer', 'dev@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvuxL7cyGH0B7uWy', '开发者用户');

COMMIT;