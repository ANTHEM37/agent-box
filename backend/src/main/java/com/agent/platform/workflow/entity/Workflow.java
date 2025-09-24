package com.agent.platform.workflow.entity;

import com.agent.platform.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "workflows")
@EqualsAndHashCode(callSuper = false)
public class Workflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private WorkflowDefinition definition;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status = WorkflowStatus.DRAFT;
    
    private String category;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> tags;
    
    @Column(name = "is_template")
    private Boolean isTemplate = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 工作流状态枚举
    public enum WorkflowStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED    // 已归档
    }
    
    // 工作流定义内部类
    @Data
    public static class WorkflowDefinition {
        private List<WorkflowNode> nodes;
        private List<WorkflowEdge> edges;
        private Map<String, Object> variables;
        private Map<String, Object> settings;
    }
    
    // 工作流节点
    @Data
    public static class WorkflowNode {
        private String id;
        private String type;
        private String name;
        private Map<String, Object> data;
        private Position position;
        private Map<String, Object> config;
    }
    
    // 工作流连接
    @Data
    public static class WorkflowEdge {
        private String id;
        private String source;
        private String target;
        private String sourceHandle;
        private String targetHandle;
        private String type;
        private Map<String, Object> data;
    }
    
    // 节点位置
    @Data
    public static class Position {
        private Double x;
        private Double y;
    }
}