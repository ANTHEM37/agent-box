package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;
import java.util.List;

/**
 * 智能体任务实体
 */
@Entity
@Table(name = "agent_tasks")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentTask extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务标题
     */
    @Column(nullable = false, length = 500)
    private String title;
    
    /**
     * 任务描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 任务类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;
    
    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    
    /**
     * 任务优先级（1-10）
     */
    @Column(columnDefinition = "INTEGER DEFAULT 5")
    private Integer priority;
    
    /**
     * 执行智能体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private AIAgent agent;
    
    /**
     * 创建者ID
     */
    @Column(nullable = false)
    private Long creatorId;
    
    /**
     * 父任务ID（用于任务分解）
     */
    @Column
    private Long parentTaskId;
    
    /**
     * 任务输入（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String input;
    
    /**
     * 任务输出（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String output;
    
    /**
     * 任务结果
     */
    @Column(columnDefinition = "TEXT")
    private String result;
    
    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 执行开始时间
     */
    @Column
    private java.time.LocalDateTime startedAt;
    
    /**
     * 执行完成时间
     */
    @Column
    private java.time.LocalDateTime completedAt;
    
    /**
     * 预计完成时间
     */
    @Column
    private java.time.LocalDateTime estimatedCompletionAt;
    
    /**
     * 执行时长（毫秒）
     */
    @Column
    private Long executionDuration;
    
    /**
     * 进度百分比（0-100）
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer progress;
    
    /**
     * 重试次数
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    @Column(columnDefinition = "INTEGER DEFAULT 3")
    private Integer maxRetries;
    
    /**
     * 任务配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String config;
    
    /**
     * 任务标签（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 是否需要协作
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean requiresCollaboration;
    
    /**
     * 协作智能体IDs（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String collaboratorAgentIds;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    // 关联关系
    
    /**
     * 子任务
     */
    @OneToMany(mappedBy = "parentTaskId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgentTask> subTasks;
    
    /**
     * 任务执行日志
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskExecutionLog> executionLogs;
}

