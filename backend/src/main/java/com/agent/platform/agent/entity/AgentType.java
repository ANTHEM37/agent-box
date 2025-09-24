package com.agent.platform.agent.entity;

/**
 * 智能体类型枚举
 */
public enum AgentType {
    ASSISTANT("助手"),
    ANALYST("分析师"),
    CREATOR("创作者"),
    RESEARCHER("研究员"),
    TRANSLATOR("翻译员"),
    CODER("程序员"),
    TEACHER("教师"),
    CONSULTANT("顾问"),
    CUSTOM("自定义");

    private final String description;

    AgentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}