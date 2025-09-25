package com.agent.platform.agent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息实体
 * 智能体之间的通信消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent_message")
public class Message extends BaseEntity {

    /**
     * 发送方智能体实例
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_instance_id")
    private AgentInstance sender;

    /**
     * 接收方智能体实例
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_instance_id")
    private AgentInstance receiver;

    /**
     * 关联的任务
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    /**
     * 消息类型
     * TEXT: 文本消息, COMMAND: 命令消息, RESULT: 结果消息, ERROR: 错误消息
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 消息状态
     * SENT: 已发送, DELIVERED: 已送达, READ: 已读, PROCESSED: 已处理
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    /**
     * 发送时间
     */
    @Column(nullable = false)
    private LocalDateTime sendTime = LocalDateTime.now();

    /**
     * 送达时间
     */
    private LocalDateTime deliverTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 处理时间
     */
    private LocalDateTime processedTime;

    /**
     * 消息优先级 (1-10, 1为最高)
     */
    private Integer priority = 5;

    /**
     * 是否需要回复
     */
    private Boolean requiresReply = false;

    /**
     * 回复消息ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyTo;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT,           // 文本消息
        COMMAND,        // 命令消息
        RESULT,         // 结果消息
        ERROR,          // 错误消息
        NOTIFICATION,   // 通知消息
        QUERY           // 查询消息
    }

    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        SENT,           // 已发送
        DELIVERED,      // 已送达
        READ,           // 已读
        PROCESSED,      // 已处理
        FAILED          // 发送失败
    }

    // 便捷方法 - 用于引擎处理
    public void setSenderId(Long senderId) {
        if (this.sender == null) {
            this.sender = new AgentInstance();
        }
        this.sender.setId(senderId);
    }

    public Long getSenderId() {
        return this.sender != null ? this.sender.getId() : null;
    }

    public void setReceiverId(Long receiverId) {
        if (this.receiver == null) {
            this.receiver = new AgentInstance();
        }
        this.receiver.setId(receiverId);
    }

    public Long getReceiverId() {
        return this.receiver != null ? this.receiver.getId() : null;
    }

    public void setMessageType(String messageType) {
        try {
            this.type = MessageType.valueOf(messageType);
        } catch (IllegalArgumentException e) {
            this.type = MessageType.TEXT; // 默认值
        }
    }

    public String getMessageType() {
        return this.type != null ? this.type.name() : null;
    }

    public void setDeliveredTime(LocalDateTime deliveredTime) {
        this.deliverTime = deliveredTime;
    }

    public void setForwardedFrom(Message message) {
        // 这里可以添加转发逻辑，当前版本简化处理
        this.content = "Forwarded: " + (message != null ? message.getContent() : "");
    }

    public void setErrorMessage(String errorMessage) {
        this.content = "Error: " + errorMessage;
        this.type = MessageType.ERROR;
    }
}