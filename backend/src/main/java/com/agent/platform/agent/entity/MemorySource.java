package com.agent.platform.agent.entity;

/**
 * 记忆来源枚举
 */
public enum MemorySource {
    USER_INPUT("用户输入"),
    TASK_EXECUTION("任务执行"),
    CONVERSATION("对话"),
    SYSTEM_EVENT("系统事件"),
    EXTERNAL_API("外部API"),
    KNOWLEDGE_BASE("知识库"),
    COLLABORATION("协作"),
    LEARNING("学习");

    private final String description;

    MemorySource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}