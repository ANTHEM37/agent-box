package com.agent.platform.agent.entity;

/**
 * 对话状态枚举
 */
public enum ConversationStatus {
    ACTIVE("活跃"),
    PAUSED("暂停"),
    COMPLETED("完成"),
    ARCHIVED("归档"),
    DELETED("已删除");

    private final String description;

    ConversationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}