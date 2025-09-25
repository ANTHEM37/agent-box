package com.agent.platform.agent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 智能体实例实体
 * 运行时的智能体实例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent_instance")
public class AgentInstance extends BaseEntity {

    /**
     * 关联的智能体定义
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_definition_id", nullable = false)
    private AgentDefinition agentDefinition;

    /**
     * 实例名称
     */
    @Column(nullable = false)
    private String instanceName;

    /**
     * 实例状态
     * CREATED: 已创建, RUNNING: 运行中, PAUSED: 已暂停, STOPPED: 已停止, ERROR: 错误
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InstanceStatus status = InstanceStatus.CREATED;

    /**
     * 会话ID
     */
    @Column(unique = true)
    private String sessionId;

    /**
     * 启动时间
     */
    private LocalDateTime startTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 实例配置参数
     */
    @Column(columnDefinition = "TEXT")
    private String instanceConfig;

    /**
     * 内存状态（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String memoryState;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建者用户ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 实例状态枚举
     */
    public enum InstanceStatus {
        CREATED,    // 已创建
        RUNNING,    // 运行中
        PAUSED,     // 已暂停
        STOPPED,    // 已停止
        ERROR       // 错误
    }
}