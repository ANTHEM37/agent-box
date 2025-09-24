package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;
import java.time.LocalDateTime;

/**
 * 任务执行日志实体
 */
@Entity
@Table(name = "task_execution_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskExecutionLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联任务
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private AgentTask task;
    
    /**
     * 日志级别
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;
    
    /**
     * 日志消息
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    /**
     * 详细信息
     */
    @Column(columnDefinition = "TEXT")
    private String details;
    
    /**
     * 执行阶段
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionPhase phase;
    
    /**
     * 时间戳
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 执行时长（毫秒）
     */
    @Column
    private Long duration;
    
    /**
     * 错误代码
     */
    @Column
    private String errorCode;
    
    /**
     * 堆栈跟踪
     */
    @Column(columnDefinition = "TEXT")
    private String stackTrace;
    
    /**
     * 上下文数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String contextData;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}