-- 创建索引
CREATE INDEX idx_agent_definitions_user_id ON agent_definitions(user_id);
CREATE INDEX idx_agent_definitions_type ON agent_definitions(type);
CREATE INDEX idx_agent_definitions_status ON agent_definitions(status);

CREATE INDEX idx_agent_instances_definition_id ON agent_instances(agent_definition_id);
CREATE INDEX idx_agent_instances_user_id ON agent_instances(user_id);
CREATE INDEX idx_agent_instances_status ON agent_instances(status);

CREATE INDEX idx_tasks_agent_instance_id ON tasks(agent_instance_id);
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_task_type ON tasks(task_type);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);

CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX idx_messages_message_type ON messages(message_type);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_sent_at ON messages(sent_at);

-- 添加约束
ALTER TABLE agent_definitions ADD CONSTRAINT chk_agent_definition_type 
    CHECK (type IN ('TEXT_PROCESSING', 'DATA_ANALYSIS', 'DECISION_MAKING', 'COLLABORATIVE'));

ALTER TABLE agent_definitions ADD CONSTRAINT chk_agent_definition_status 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED'));

ALTER TABLE agent_instances ADD CONSTRAINT chk_agent_instance_status 
    CHECK (status IN ('STOPPED', 'STARTING', 'RUNNING', 'STOPPING', 'ERROR'));

ALTER TABLE tasks ADD CONSTRAINT chk_task_type 
    CHECK (task_type IN ('TEXT_PROCESSING', 'DATA_ANALYSIS', 'DECISION_MAKING', 'COLLABORATIVE'));

ALTER TABLE tasks ADD CONSTRAINT chk_task_status 
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'));

ALTER TABLE messages ADD CONSTRAINT chk_message_status 
    CHECK (status IN ('SENT', 'DELIVERED', 'READ', 'FAILED'));

-- 创建更新时间触发器
CREATE TRIGGER update_agent_definitions_updated_at BEFORE UPDATE ON agent_definitions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agent_instances_updated_at BEFORE UPDATE ON agent_instances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 插入示例数据
INSERT INTO agent_definitions (name, description, type, capabilities, prompt_template, user_id) VALUES
('文本处理助手', '专门处理文本相关任务的智能体', 'TEXT_PROCESSING', ARRAY['文本分析', '内容生成', '语言翻译'], '你是一个专业的文本处理助手，请根据用户的需求处理以下文本：{input}', 1),
('数据分析师', '专门进行数据分析的智能体', 'DATA_ANALYSIS', ARRAY['数据统计', '趋势分析', '报告生成'], '你是一个专业的数据分析师，请分析以下数据：{input}', 1),
('决策顾问', '提供决策建议的智能体', 'DECISION_MAKING', ARRAY['方案评估', '风险分析', '建议生成'], '你是一个专业的决策顾问，请针对以下情况提供建议：{input}', 1);