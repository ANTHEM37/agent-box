package com.agent.platform.agent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 任务实体
 * 智能体执行的任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent_task")
public class Task extends BaseEntity {

    /**
     * 关联的智能体实例
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_instance_id", nullable = false)
    private AgentInstance agentInstance;

    /**
     * 任务名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 任务描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 任务类型
     * SINGLE: 单次任务, SEQUENTIAL: 顺序任务, PARALLEL: 并行任务
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type = TaskType.SINGLE;

    /**
     * 任务子类型（用于引擎处理）
     * TEXT_PROCESSING: 文本处理, DATA_ANALYSIS: 数据分析, DECISION_MAKING: 决策制定, COLLABORATIVE: 协作任务
     */
    @Column(name = "sub_type")
    @Enumerated(EnumType.STRING)
    private TaskSubType subType;

    /**
     * 任务状态
     * PENDING: 等待中, RUNNING: 执行中, COMPLETED: 已完成, FAILED: 失败, CANCELLED: 已取消
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * 任务输入参数
     */
    @Column(columnDefinition = "TEXT")
    private String inputParameters;

    /**
     * 任务输出结果
     */
    @Column(columnDefinition = "TEXT")
    private String outputResult;

    /**
     * 优先级 (1-10, 1为最高)
     */
    private Integer priority = 5;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 任务完成时间（兼容引擎调用）
     */
    private LocalDateTime completedAt;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 预计执行时间（分钟）
     */
    private Integer estimatedDuration;

    /**
     * 实际执行时间（分钟）
     */
    private Integer actualDuration;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 父任务ID（用于任务链）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    /**
     * 任务类型枚举
     */
    public enum TaskType {
        SINGLE,         // 单次任务
        SEQUENTIAL,     // 顺序任务
        PARALLEL,       // 并行任务
        WORKFLOW        // 工作流任务
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,        // 等待中
        RUNNING,        // 执行中
        COMPLETED,      // 已完成
        FAILED,         // 失败
        CANCELLED       // 已取消
    }

    /**
     * 任务子类型枚举
     */
    public enum TaskSubType {
        TEXT_PROCESSING,    // 文本处理
        DATA_ANALYSIS,      // 数据分析
        DECISION_MAKING,    // 决策制定
        COLLABORATIVE       // 协作任务
    }

    // 便捷方法 - 用于引擎处理
    public void setTaskType(TaskSubType subType) {
        this.subType = subType;
    }

    public TaskSubType getTaskType() {
        return this.subType;
    }

    public void setInput(String input) {
        this.inputParameters = input;
    }

    public String getInput() {
        return this.inputParameters;
    }

    public void setResult(String result) {
        this.outputResult = result;
    }

    public String getResult() {
        return this.outputResult;
    }

    // 临时参数存储（不持久化到数据库）
    @Transient
    private Map<String, Object> tempParameters = new HashMap<>();

    public void setParameters(Map<String, Object> parameters) {
        this.tempParameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return this.tempParameters;
    }

    /**
     * 获取关联的智能体实例ID（便捷方法）
     */
    public Long getAgentInstanceId() {
        return agentInstance != null ? agentInstance.getId() : null;
    }

    /**
     * 设置关联的智能体实例ID（便捷方法）
     */
    public void setAgentInstanceId(Long agentInstanceId) {
        if (this.agentInstance == null) {
            this.agentInstance = new AgentInstance();
        }
        this.agentInstance.setId(agentInstanceId);
    }

    /**
     * 获取任务类型字符串（便捷方法）
     */
    public String getTaskTypeString() {
        return subType != null ? subType.name() : null;
    }

    /**
     * 设置任务类型字符串（便捷方法）
     */
    public void setTaskTypeString(String taskType) {
        try {
            this.subType = TaskSubType.valueOf(taskType);
        } catch (IllegalArgumentException e) {
            this.subType = TaskSubType.TEXT_PROCESSING; // 默认值
        }
    }

    /**
     * 获取任务状态字符串（便捷方法）
     */
    public String getStatusString() {
        return status != null ? status.name() : null;
    }

    /**
     * 设置任务状态字符串（便捷方法）
     */
    public void setStatusString(String status) {
        try {
            this.status = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            this.status = TaskStatus.PENDING; // 默认值
        }
    }

    /**
     * 获取开始时间（兼容引擎调用）
     */
    public LocalDateTime getStartedAt() {
        return startTime;
    }

    /**
     * 设置开始时间（兼容引擎调用）
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startTime = startedAt;
    }
}