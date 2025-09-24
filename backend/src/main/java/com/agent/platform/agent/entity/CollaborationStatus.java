package com.agent.platform.agent.entity;

/**
 * 协作状态枚举
 */
public enum CollaborationStatus {
    INITIATED("已发起"),
    IN_PROGRESS("进行中"),
    WAITING_RESPONSE("等待响应"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消"),
    TIMEOUT("超时");

    private final String description;

    CollaborationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}