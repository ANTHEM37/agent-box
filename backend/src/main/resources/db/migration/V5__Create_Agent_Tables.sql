-- 智能体定义表
CREATE TABLE agent_definitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    capabilities TEXT[],
    model_config JSONB,
    prompt_template TEXT,
    max_tokens INTEGER DEFAULT 2000,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 智能体实例表
CREATE TABLE agent_instances (
    id BIGSERIAL PRIMARY KEY,
    agent_definition_id BIGINT NOT NULL REFERENCES agent_definitions(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    config JSONB,
    status VARCHAR(20) DEFAULT 'STOPPED',
    last_activity TIMESTAMP,
    resource_usage JSONB,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 任务表
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    agent_instance_id BIGINT NOT NULL REFERENCES agent_instances(id),
    task_type VARCHAR(50) NOT NULL,
    input TEXT NOT NULL,
    output TEXT,
    parameters JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    priority INTEGER DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT REFERENCES agent_instances(id),
    receiver_id BIGINT REFERENCES agent_instances(id),
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    metadata JSONB,
    status VARCHAR(20) DEFAULT 'SENT',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP
);