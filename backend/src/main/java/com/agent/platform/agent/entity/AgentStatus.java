package com.agent.platform.agent.entity;

/**
 * 智能体状态枚举
 */
public enum AgentStatus {
    DRAFT("草稿"),
    ACTIVE("活跃"),
    INACTIVE("非活跃"),
    TRAINING("训练中"),
    MAINTENANCE("维护中"),
    ARCHIVED("已归档");

    private final String description;

    AgentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}