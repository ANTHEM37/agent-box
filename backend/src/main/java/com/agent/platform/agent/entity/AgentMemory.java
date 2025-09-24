package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;

/**
 * 智能体记忆实体
 */
@Entity
@Table(name = "agent_memories")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentMemory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的智能体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private AIAgent agent;
    
    /**
     * 记忆类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryType type;
    
    /**
     * 记忆内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * 记忆摘要
     */
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    /**
     * 记忆向量（用于相似性搜索）
     */
    @Column(columnDefinition = "TEXT")
    private String embedding;
    
    /**
     * 重要性评分（0.0-1.0）
     */
    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0.5")
    private Double importance;
    
    /**
     * 访问次数
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer accessCount;
    
    /**
     * 最后访问时间
     */
    @Column
    private java.time.LocalDateTime lastAccessAt;
    
    /**
     * 遗忘时间（用于记忆衰减）
     */
    @Column
    private java.time.LocalDateTime forgetAt;
    
    /**
     * 关联的任务ID
     */
    @Column
    private Long taskId;
    
    /**
     * 关联的会话ID
     */
    @Column
    private Long conversationId;
    
    /**
     * 记忆标签（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 记忆来源
     */
    @Enumerated(EnumType.STRING)
    @Column
    private MemorySource source;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}

/**
 * 记忆类型枚举
 */
enum MemoryType {
    WORKING,        // 工作记忆
    SHORT_TERM,     // 短期记忆
    LONG_TERM,      // 长期记忆
    EPISODIC,       // 情节记忆
    SEMANTIC,       // 语义记忆
    PROCEDURAL,     // 程序记忆
    META           // 元记忆
}

/**
 * 记忆来源枚举
 */
enum MemorySource {
    CONVERSATION,   // 对话
    TASK,          // 任务
    LEARNING,      // 学习
    FEEDBACK,      // 反馈
    COLLABORATION, // 协作
    SYSTEM,        // 系统
    EXTERNAL       // 外部
}