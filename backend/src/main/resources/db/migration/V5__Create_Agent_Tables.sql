-- AI智能体系统数据库表结构

-- 智能体表
CREATE TABLE ai_agents (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(100),
    avatar_url VARCHAR(500),
    personality_config TEXT,
    capabilities_config TEXT,
    knowledge_base_config TEXT,
    tools_config TEXT,
    model_config TEXT,
    system_prompt TEXT,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INTEGER DEFAULT 4000,
    memory_enabled BOOLEAN DEFAULT true,
    memory_capacity INTEGER DEFAULT 1000,
    learning_enabled BOOLEAN DEFAULT true,
    collaboration_enabled BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 5,
    tags TEXT,
    is_public BOOLEAN DEFAULT false,
    usage_count BIGINT DEFAULT 0,
    success_rate DECIMAL(5,2) DEFAULT 0.0,
    avg_response_time BIGINT DEFAULT 0,
    last_active_at TIMESTAMP,
    version INTEGER DEFAULT 1,
    extension_config TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 智能体记忆表
CREATE TABLE agent_memories (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL REFERENCES ai_agents(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    embedding TEXT,
    importance DECIMAL(3,2) DEFAULT 0.5,
    access_count INTEGER DEFAULT 0,
    last_access_at TIMESTAMP,
    forget_at TIMESTAMP,
    task_id BIGINT,
    conversation_id BIGINT,
    tags TEXT,
    source VARCHAR(50),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 智能体任务表
CREATE TABLE agent_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority INTEGER DEFAULT 5,
    agent_id BIGINT NOT NULL REFERENCES ai_agents(id) ON DELETE CASCADE,
    creator_id BIGINT NOT NULL,
    parent_task_id BIGINT,
    input TEXT,
    output TEXT,
    result TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_completion_at TIMESTAMP,
    execution_duration BIGINT,
    progress INTEGER DEFAULT 0,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    config TEXT,
    tags TEXT,
    requires_collaboration BOOLEAN DEFAULT false,
    collaborator_agent_ids TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 智能体协作表
CREATE TABLE agent_collaborations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    primary_agent_id BIGINT NOT NULL REFERENCES ai_agents(id) ON DELETE CASCADE,
    participant_agent_ids TEXT NOT NULL,
    task_id BIGINT,
    objective TEXT,
    strategy VARCHAR(50),
    rules TEXT,
    result TEXT,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    duration BIGINT,
    message_count INTEGER DEFAULT 0,
    participant_count INTEGER DEFAULT 0,
    rating INTEGER,
    feedback TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 协作消息表
CREATE TABLE collaboration_messages (
    id BIGSERIAL PRIMARY KEY,
    collaboration_id BIGINT NOT NULL REFERENCES agent_collaborations(id) ON DELETE CASCADE,
    sender_agent_id BIGINT NOT NULL,
    sender_agent_name VARCHAR(200),
    receiver_agent_id BIGINT,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    summary VARCHAR(500),
    intent VARCHAR(50),
    priority INTEGER DEFAULT 5,
    requires_response BOOLEAN DEFAULT false,
    response_deadline TIMESTAMP,
    parent_message_id BIGINT,
    sequence_number INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    received_at TIMESTAMP,
    processed_at TIMESTAMP,
    attachments TEXT,
    tags TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 智能体对话表
CREATE TABLE agent_conversations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    agent_id BIGINT NOT NULL REFERENCES ai_agents(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    message_count INTEGER DEFAULT 0,
    last_message_at TIMESTAMP,
    last_message_content VARCHAR(1000),
    summary TEXT,
    tags TEXT,
    config TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 对话消息表
CREATE TABLE conversation_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES agent_conversations(id) ON DELETE CASCADE,
    sender_type VARCHAR(50) NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_name VARCHAR(200),
    content TEXT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    sequence_number INTEGER NOT NULL,
    parent_message_id BIGINT,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    received_at TIMESTAMP,
    processed_at TIMESTAMP,
    response_time BIGINT,
    token_usage INTEGER,
    attachments TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 任务执行日志表
CREATE TABLE task_execution_logs (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES agent_tasks(id) ON DELETE CASCADE,
    level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    details TEXT,
    step VARCHAR(200),
    phase VARCHAR(50),
    error_code VARCHAR(100),
    stack_trace TEXT,
    timestamp TIMESTAMP NOT NULL,
    duration BIGINT,
    memory_usage BIGINT,
    cpu_usage DECIMAL(5,2),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT false
);

-- 创建索引
CREATE INDEX idx_ai_agents_creator_id ON ai_agents(creator_id);
CREATE INDEX idx_ai_agents_status ON ai_agents(status);
CREATE INDEX idx_ai_agents_type ON ai_agents(type);
CREATE INDEX idx_ai_agents_is_public ON ai_agents(is_public);

CREATE INDEX idx_agent_memories_agent_id ON agent_memories(agent_id);
CREATE INDEX idx_agent_memories_type ON agent_memories(type);
CREATE INDEX idx_agent_memories_importance ON agent_memories(importance);
CREATE INDEX idx_agent_memories_task_id ON agent_memories(task_id);
CREATE INDEX idx_agent_memories_conversation_id ON agent_memories(conversation_id);

CREATE INDEX idx_agent_tasks_agent_id ON agent_tasks(agent_id);
CREATE INDEX idx_agent_tasks_creator_id ON agent_tasks(creator_id);
CREATE INDEX idx_agent_tasks_status ON agent_tasks(status);
CREATE INDEX idx_agent_tasks_type ON agent_tasks(type);
CREATE INDEX idx_agent_tasks_parent_task_id ON agent_tasks(parent_task_id);

CREATE INDEX idx_agent_collaborations_primary_agent_id ON agent_collaborations(primary_agent_id);
CREATE INDEX idx_agent_collaborations_status ON agent_collaborations(status);
CREATE INDEX idx_agent_collaborations_type ON agent_collaborations(type);
CREATE INDEX idx_agent_collaborations_task_id ON agent_collaborations(task_id);

CREATE INDEX idx_collaboration_messages_collaboration_id ON collaboration_messages(collaboration_id);
CREATE INDEX idx_collaboration_messages_sender_agent_id ON collaboration_messages(sender_agent_id);
CREATE INDEX idx_collaboration_messages_receiver_agent_id ON collaboration_messages(receiver_agent_id);
CREATE INDEX idx_collaboration_messages_sequence_number ON collaboration_messages(sequence_number);

CREATE INDEX idx_agent_conversations_agent_id ON agent_conversations(agent_id);
CREATE INDEX idx_agent_conversations_user_id ON agent_conversations(user_id);
CREATE INDEX idx_agent_conversations_status ON agent_conversations(status);
CREATE INDEX idx_agent_conversations_type ON agent_conversations(type);

CREATE INDEX idx_conversation_messages_conversation_id ON conversation_messages(conversation_id);
CREATE INDEX idx_conversation_messages_sender_id ON conversation_messages(sender_id);
CREATE INDEX idx_conversation_messages_sequence_number ON conversation_messages(sequence_number);
CREATE INDEX idx_conversation_messages_parent_message_id ON conversation_messages(parent_message_id);

CREATE INDEX idx_task_execution_logs_task_id ON task_execution_logs(task_id);
CREATE INDEX idx_task_execution_logs_level ON task_execution_logs(level);
CREATE INDEX idx_task_execution_logs_timestamp ON task_execution_logs(timestamp);
CREATE INDEX idx_task_execution_logs_phase ON task_execution_logs(phase);