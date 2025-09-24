package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;

/**
 * 对话消息实体
 */
@Entity
@Table(name = "conversation_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationMessage extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的对话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AgentConversation conversation;
    
    /**
     * 消息发送者类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;
    
    /**
     * 发送者ID
     */
    @Column(nullable = false)
    private Long senderId;
    
    /**
     * 发送者名称
     */
    @Column(length = 200)
    private String senderName;
    
    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMessageType messageType;
    
    /**
     * 消息序号
     */
    @Column(nullable = false)
    private Integer sequenceNumber;
    
    /**
     * 父消息ID（用于回复）
     */
    @Column
    private Long parentMessageId;
    
    /**
     * 消息状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMessageStatus status;
    
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
     * 响应时间（毫秒）
     */
    @Column
    private Long responseTime;
    
    /**
     * 令牌使用量
     */
    @Column
    private Integer tokenUsage;
    
    /**
     * 附件信息（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String attachments;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}

/**
 * 发送者类型枚举
 */
enum SenderType {
    USER,              // 用户
    AGENT,             // 智能体
    SYSTEM             // 系统
}

/**
 * 对话消息类型枚举
 */
enum ConversationMessageType {
    TEXT,              // 文本
    IMAGE,             // 图片
    FILE,              // 文件
    AUDIO,             // 音频
    VIDEO,             // 视频
    SYSTEM,            // 系统消息
    FUNCTION_CALL,     // 函数调用
    FUNCTION_RESULT    // 函数结果
}

/**
 * 对话消息状态枚举
 */
enum ConversationMessageStatus {
    DRAFT,             // 草稿
    SENT,              // 已发送
    DELIVERED,         // 已送达
    READ,              // 已读
    PROCESSED,         // 已处理
    FAILED             // 失败
}