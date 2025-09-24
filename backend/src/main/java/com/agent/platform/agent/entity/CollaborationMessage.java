package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;

/**
 * 协作消息实体
 */
@Entity
@Table(name = "collaboration_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class CollaborationMessage extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的协作会话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id", nullable = false)
    private AgentCollaboration collaboration;
    
    /**
     * 发送者智能体ID
     */
    @Column(nullable = false)
    private Long senderAgentId;
    
    /**
     * 发送者智能体名称
     */
    @Column(length = 200)
    private String senderAgentName;
    
    /**
     * 接收者智能体ID（null表示广播）
     */
    @Column
    private Long receiverAgentId;
    
    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;
    
    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * 消息摘要
     */
    @Column(length = 500)
    private String summary;
    
    /**
     * 消息意图
     */
    @Enumerated(EnumType.STRING)
    @Column
    private MessageIntent intent;
    
    /**
     * 消息优先级（1-10）
     */
    @Column(columnDefinition = "INTEGER DEFAULT 5")
    private Integer priority;
    
    /**
     * 是否需要回复
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean requiresResponse;
    
    /**
     * 回复截止时间
     */
    @Column
    private java.time.LocalDateTime responseDeadline;
    
    /**
     * 父消息ID（用于回复链）
     */
    @Column
    private Long parentMessageId;
    
    /**
     * 消息序号（在协作会话中的顺序）
     */
    @Column(nullable = false)
    private Integer sequenceNumber;
    
    /**
     * 消息状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;
    
    /**
     * 发送时间
     */
    @Column
    private java.time.LocalDateTime sentAt;
    
    /**
     * 接收时间
     */
    @Column
    private java.time.LocalDateTime receivedAt;
    
    /**
     * 处理时间
     */
    @Column
    private java.time.LocalDateTime processedAt;
    
    /**
     * 附件信息（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String attachments;
    
    /**
     * 消息标签（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}

/**
 * 消息类型枚举
 */
enum MessageType {
    TEXT,              // 文本消息
    TASK_REQUEST,      // 任务请求
    TASK_RESPONSE,     // 任务响应
    QUESTION,          // 问题
    ANSWER,            // 回答
    PROPOSAL,          // 提议
    VOTE,              // 投票
    FEEDBACK,          // 反馈
    STATUS_UPDATE,     // 状态更新
    SYSTEM,            // 系统消息
    NOTIFICATION       // 通知
}

/**
 * 消息意图枚举
 */
enum MessageIntent {
    INFORM,            // 告知
    REQUEST,           // 请求
    PROPOSE,           // 提议
    AGREE,             // 同意
    DISAGREE,          // 不同意
    QUESTION,          // 询问
    CLARIFY,           // 澄清
    DELEGATE,          // 委派
    REPORT,            // 报告
    WARN               // 警告
}

/**
 * 消息状态枚举
 */
enum MessageStatus {
    DRAFT,             // 草稿
    SENT,              // 已发送
    DELIVERED,         // 已送达
    READ,              // 已读
    PROCESSED,         // 已处理
    REPLIED,           // 已回复
    IGNORED,           // 已忽略
    FAILED             // 发送失败
}