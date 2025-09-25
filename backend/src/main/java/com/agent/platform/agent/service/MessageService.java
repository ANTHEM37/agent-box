package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     */
    Message sendMessage(Message message);

    /**
     * 根据ID获取消息
     */
    Optional<Message> getMessageById(Long id);

    /**
     * 获取发送方的所有消息
     */
    List<Message> getMessagesBySenderId(Long senderId);

    /**
     * 获取接收方的所有消息
     */
    List<Message> getReceiversMessages(Long receiverId);

    /**
     * 获取两个智能体之间的对话
     */
    List<Message> getConversationBetweenAgents(Long agent1Id, Long agent2Id);

    /**
     * 标记消息为已读
     */
    Message markMessageAsRead(Long messageId);

    /**
     * 标记消息为已送达
     */
    Message markMessageAsDelivered(Long messageId);

    /**
     * 获取未读消息数量
     */
    long getUnreadMessageCount(Long receiverId);

    /**
     * 获取指定状态的消息
     */
    List<Message> getMessagesByStatus(Message.MessageStatus status);

    /**
     * 分页查询消息
     */
    Page<Message> getMessages(Pageable pageable);

    /**
     * 回复消息
     */
    Message replyToMessage(Long originalMessageId, Message replyMessage);

    /**
     * 转发消息
     */
    Message forwardMessage(Long originalMessageId, Long newReceiverId);

    /**
     * 删除消息
     */
    void deleteMessage(Long messageId);

    /**
     * 清理过期消息
     */
    int cleanupExpiredMessages(LocalDateTime expireTime);

    /**
     * 获取消息统计信息
     */
    MessageStats getMessageStats();

    /**
     * 消息统计信息类
     */
    class MessageStats {
        private long totalMessages;
        private long sentMessages;
        private long deliveredMessages;
        private long readMessages;
        private long failedMessages;

        public MessageStats() {}

        public MessageStats(long totalMessages, long sentMessages, long deliveredMessages, long readMessages, long failedMessages) {
            this.totalMessages = totalMessages;
            this.sentMessages = sentMessages;
            this.deliveredMessages = deliveredMessages;
            this.readMessages = readMessages;
            this.failedMessages = failedMessages;
        }

        // getter和setter方法
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public long getSentMessages() { return sentMessages; }
        public void setSentMessages(long sentMessages) { this.sentMessages = sentMessages; }
        
        public long getDeliveredMessages() { return deliveredMessages; }
        public void setDeliveredMessages(long deliveredMessages) { this.deliveredMessages = deliveredMessages; }
        
        public long getReadMessages() { return readMessages; }
        public void setReadMessages(long readMessages) { this.readMessages = readMessages; }
        
        public long getFailedMessages() { return failedMessages; }
        public void setFailedMessages(long failedMessages) { this.failedMessages = failedMessages; }
    }
}