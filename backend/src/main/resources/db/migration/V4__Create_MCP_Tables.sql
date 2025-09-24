-- MCP 服务市场相关表

-- MCP 服务表
CREATE TABLE mcp_services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    author_id BIGINT NOT NULL,
    repository_url VARCHAR(500),
    documentation_url VARCHAR(500),
    icon_url VARCHAR(500),
    price_model VARCHAR(20) DEFAULT 'FREE' CHECK (price_model IN ('FREE', 'PAID', 'FREEMIUM')),
    price_per_request DECIMAL(10,4),
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'DEPRECATED')),
    featured BOOLEAN DEFAULT FALSE,
    downloads_count BIGINT DEFAULT 0,
    rating_average DECIMAL(3,2) DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- MCP 服务标签表
CREATE TABLE mcp_service_tags (
    service_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (service_id, tag),
    FOREIGN KEY (service_id) REFERENCES mcp_services(id) ON DELETE CASCADE
);

-- 服务版本表
CREATE TABLE service_versions (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    changelog TEXT,
    docker_image VARCHAR(200),
    config_schema JSONB,
    api_spec JSONB,
    is_latest BOOLEAN DEFAULT FALSE,
    download_count BIGINT DEFAULT 0,
    size_mb DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES mcp_services(id) ON DELETE CASCADE,
    UNIQUE(service_id, version)
);

-- 服务部署表
CREATE TABLE service_deployments (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    deployment_name VARCHAR(100) NOT NULL,
    config JSONB,
    status VARCHAR(20) DEFAULT 'DEPLOYING' CHECK (status IN ('DEPLOYING', 'RUNNING', 'STOPPED', 'FAILED', 'UPDATING')),
    endpoint_url VARCHAR(500),
    replicas INTEGER DEFAULT 1,
    cpu_limit VARCHAR(20),
    memory_limit VARCHAR(20),
    auto_scaling BOOLEAN DEFAULT FALSE,
    min_replicas INTEGER DEFAULT 1,
    max_replicas INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES mcp_services(id) ON DELETE CASCADE,
    FOREIGN KEY (version_id) REFERENCES service_versions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(deployment_name, user_id)
);

-- 服务实例表
CREATE TABLE service_instances (
    id BIGSERIAL PRIMARY KEY,
    deployment_id BIGINT NOT NULL,
    container_id VARCHAR(100),
    host VARCHAR(100),
    port INTEGER,
    status VARCHAR(20) DEFAULT 'STARTING' CHECK (status IN ('STARTING', 'RUNNING', 'STOPPING', 'STOPPED', 'FAILED', 'UNHEALTHY')),
    health_check_url VARCHAR(500),
    last_health_check TIMESTAMP,
    cpu_usage DECIMAL(5,2),
    memory_usage DECIMAL(5,2),
    request_count BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    avg_response_time BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (deployment_id) REFERENCES service_deployments(id) ON DELETE CASCADE
);

-- 服务评论表
CREATE TABLE service_reviews (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES mcp_services(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(service_id, user_id)
);

-- 服务使用统计表
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (deployment_id) REFERENCES service_deployments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(deployment_id, user_id, date)
);

-- 创建索引
CREATE INDEX idx_mcp_services_author_id ON mcp_services(author_id);
CREATE INDEX idx_mcp_services_category ON mcp_services(category);
CREATE INDEX idx_mcp_services_status ON mcp_services(status);
CREATE INDEX idx_mcp_services_featured ON mcp_services(featured);
CREATE INDEX idx_mcp_services_rating ON mcp_services(rating_average);
CREATE INDEX idx_mcp_services_downloads ON mcp_services(downloads_count);

CREATE INDEX idx_service_versions_service_id ON service_versions(service_id);
CREATE INDEX idx_service_versions_latest ON service_versions(service_id, is_latest);

CREATE INDEX idx_service_deployments_user_id ON service_deployments(user_id);
CREATE INDEX idx_service_deployments_service_id ON service_deployments(service_id);
CREATE INDEX idx_service_deployments_status ON service_deployments(status);

CREATE INDEX idx_service_instances_deployment_id ON service_instances(deployment_id);
CREATE INDEX idx_service_instances_status ON service_instances(status);
CREATE INDEX idx_service_instances_container_id ON service_instances(container_id);

CREATE INDEX idx_service_reviews_service_id ON service_reviews(service_id);
CREATE INDEX idx_service_reviews_user_id ON service_reviews(user_id);
CREATE INDEX idx_service_reviews_rating ON service_reviews(rating);

CREATE INDEX idx_service_usage_deployment_id ON service_usage(deployment_id);
CREATE INDEX idx_service_usage_user_id ON service_usage(user_id);
CREATE INDEX idx_service_usage_date ON service_usage(date);

-- 创建更新时间触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_mcp_services_updated_at BEFORE UPDATE ON mcp_services FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_service_versions_updated_at BEFORE UPDATE ON service_versions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_service_deployments_updated_at BEFORE UPDATE ON service_deployments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_service_instances_updated_at BEFORE UPDATE ON service_instances FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_service_reviews_updated_at BEFORE UPDATE ON service_reviews FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_service_usage_updated_at BEFORE UPDATE ON service_usage FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();