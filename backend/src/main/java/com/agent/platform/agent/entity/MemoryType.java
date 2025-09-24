package com.agent.platform.agent.entity;

/**
 * 记忆类型枚举
 */
public enum MemoryType {
    WORKING("工作记忆"),
    SHORT_TERM("短期记忆"),
    LONG_TERM("长期记忆"),
    EPISODIC("情节记忆"),
    SEMANTIC("语义记忆"),
    PROCEDURAL("程序记忆"),
    DECLARATIVE("陈述记忆");

    private final String description;

    MemoryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}