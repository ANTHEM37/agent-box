package com.agent.platform.agent.entity;

/**
 * 执行阶段枚举
 */
public enum ExecutionPhase {
    INITIALIZATION("初始化"),
    PLANNING("规划"),
    EXECUTION("执行"),
    MONITORING("监控"),
    EVALUATION("评估"),
    COMPLETION("完成"),
    ERROR_HANDLING("错误处理"),
    CLEANUP("清理");

    private final String description;

    ExecutionPhase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}