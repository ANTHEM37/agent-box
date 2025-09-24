package com.agent.platform.workflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "node_executions")
@EqualsAndHashCode(callSuper = false)
public class NodeExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private WorkflowExecution execution;
    
    @Column(name = "node_id", nullable = false)
    private String nodeId;
    
    @Column(name = "node_type", nullable = false)
    private String nodeType;
    
    @Column(name = "node_name")
    private String nodeName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeExecutionStatus status = NodeExecutionStatus.PENDING;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data", columnDefinition = "jsonb")
    private Map<String, Object> inputData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_data", columnDefinition = "jsonb")
    private Map<String, Object> outputData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> config;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "duration_ms")
    private Integer durationMs;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    // 开始执行
    public void start() {
        status = NodeExecutionStatus.RUNNING;
        startedAt = LocalDateTime.now();
    }
    
    // 完成执行
    public void complete(Map<String, Object> output) {
        status = NodeExecutionStatus.COMPLETED;
        completedAt = LocalDateTime.now();
        outputData = output;
        calculateDuration();
    }
    
    // 执行失败
    public void fail(String errorMessage) {
        status = NodeExecutionStatus.FAILED;
        completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        calculateDuration();
    }
    
    // 跳过执行
    public void skip() {
        status = NodeExecutionStatus.SKIPPED;
        completedAt = LocalDateTime.now();
        calculateDuration();
    }
    
    // 计算执行时长
    private void calculateDuration() {
        if (startedAt != null && completedAt != null) {
            durationMs = (int) java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }
    
    // 节点执行状态枚举
    public enum NodeExecutionStatus {
        PENDING,    // 等待执行
        RUNNING,    // 执行中
        COMPLETED,  // 已完成
        FAILED,     // 失败
        SKIPPED     // 跳过
    }
}