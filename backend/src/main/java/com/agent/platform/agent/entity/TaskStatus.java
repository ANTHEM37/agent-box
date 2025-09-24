package com.agent.platform.agent.entity;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    PENDING("待处理"),
    RUNNING("运行中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消"),
    PAUSED("已暂停");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}