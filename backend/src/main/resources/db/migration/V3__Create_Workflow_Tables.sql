-- 工作流表
CREATE TABLE workflows (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    definition JSONB NOT NULL,  -- 工作流图结构 (nodes, edges, variables)
    version INTEGER DEFAULT 1,
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, ARCHIVED
    category VARCHAR(100),
    tags TEXT[],
    is_template BOOLEAN DEFAULT FALSE,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 工作流执行表
CREATE TABLE workflow_executions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT REFERENCES workflows(id),
    workflow_version INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'RUNNING', -- RUNNING, COMPLETED, FAILED, CANCELLED
    input_data JSONB,
    output_data JSONB,
    context JSONB,  -- 执行上下文和变量
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    error_message TEXT,
    user_id BIGINT REFERENCES users(id)
);

-- 节点执行记录表
CREATE TABLE node_executions (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT REFERENCES workflow_executions(id),
    node_id VARCHAR(255) NOT NULL,  -- 节点在工作流中的唯一ID
    node_type VARCHAR(100) NOT NULL, -- start, llm_chat, knowledge_retrieval, etc.
    node_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    input_data JSONB,
    output_data JSONB,
    config JSONB,  -- 节点配置
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms INTEGER,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0
);

-- 工作流模板表
CREATE TABLE workflow_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    tags TEXT[],
    definition JSONB NOT NULL,
    preview_image VARCHAR(500),
    usage_count INTEGER DEFAULT 0,
    is_public BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_workflows_user_id ON workflows(user_id);
CREATE INDEX idx_workflows_status ON workflows(status);
CREATE INDEX idx_workflows_category ON workflows(category);
CREATE INDEX idx_workflows_created_at ON workflows(created_at);

CREATE INDEX idx_workflow_executions_workflow_id ON workflow_executions(workflow_id);
CREATE INDEX idx_workflow_executions_user_id ON workflow_executions(user_id);
CREATE INDEX idx_workflow_executions_status ON workflow_executions(status);
CREATE INDEX idx_workflow_executions_started_at ON workflow_executions(started_at);

CREATE INDEX idx_node_executions_execution_id ON node_executions(execution_id);
CREATE INDEX idx_node_executions_node_type ON node_executions(node_type);
CREATE INDEX idx_node_executions_status ON node_executions(status);

CREATE INDEX idx_workflow_templates_category ON workflow_templates(category);
CREATE INDEX idx_workflow_templates_is_public ON workflow_templates(is_public);
CREATE INDEX idx_workflow_templates_usage_count ON workflow_templates(usage_count);

-- 添加约束
ALTER TABLE workflows ADD CONSTRAINT chk_workflow_status 
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

ALTER TABLE workflow_executions ADD CONSTRAINT chk_execution_status 
    CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'));

ALTER TABLE node_executions ADD CONSTRAINT chk_node_execution_status 
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED'));

-- 添加触发器更新 updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_workflows_updated_at BEFORE UPDATE ON workflows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_templates_updated_at BEFORE UPDATE ON workflow_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();