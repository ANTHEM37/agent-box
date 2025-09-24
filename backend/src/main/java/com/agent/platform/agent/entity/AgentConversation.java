package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;
import java.util.List;

/**
 * 智能体对话实体
 */
@Entity
@Table(name = "agent_conversations")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentConversation extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 对话标题
     */
    @Column(nullable = false, length = 500)
    private String title;
    
    /**
     * 关联的智能体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private AIAgent agent;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 用户名称
     */
    @Column(length = 100)
    private String userName;
    
    /**
     * 对话状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status;
    
    /**
     * 对话类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;
    
    /**
     * 消息数量
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer messageCount;
    
    /**
     * 最后消息时间
     */
    @Column
    private java.time.LocalDateTime lastMessageAt;
    
    /**
     * 最后消息内容
     */
    @Column(length = 1000)
    private String lastMessageContent;
    
    /**
     * 对话摘要
     */
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    /**
     * 对话标签（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 对话配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String config;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    // 关联关系
    
    /**
     * 对话消息
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConversationMessage> messages;
}

/**
 * 对话状态枚举
 */
enum ConversationStatus {
    ACTIVE,            // 活跃
    PAUSED,            // 暂停
    COMPLETED,         // 已完成
    ARCHIVED,          // 已归档
    DELETED            // 已删除
}

/**
 * 对话类型枚举
 */
enum ConversationType {
    CHAT,              // 聊天
    TASK,              // 任务
    CONSULTATION,      // 咨询
    LEARNING,          // 学习
    COLLABORATION,     // 协作
    SUPPORT            // 支持
}