package com.agent.platform.agent.service.impl;

import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.entity.Message;
import com.agent.platform.agent.repository.AgentInstanceRepository;
import com.agent.platform.agent.repository.MessageRepository;
import com.agent.platform.agent.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 消息服务实现类
 */
@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AgentInstanceRepository agentInstanceRepository;

    @Override
    public Message sendMessage(Message message) {
        // 验证发送方和接收方存在
        if (message.getSender() == null || message.getSender().getId() == null) {
            throw new IllegalArgumentException("消息必须指定发送方");
        }
        if (message.getReceiver() == null || message.getReceiver().getId() == null) {
            throw new IllegalArgumentException("消息必须指定接收方");
        }

        Optional<AgentInstance> sender = agentInstanceRepository.findById(message.getSender().getId());
        Optional<AgentInstance> receiver = agentInstanceRepository.findById(message.getReceiver().getId());

        if (sender.isEmpty()) {
            throw new IllegalArgumentException("发送方不存在: " + message.getSender().getId());
        }
        if (receiver.isEmpty()) {
            throw new IllegalArgumentException("接收方不存在: " + message.getReceiver().getId());
        }

        // 设置默认值
        if (message.getType() == null) {
            message.setType(Message.MessageType.TEXT);
        }
        if (message.getStatus() == null) {
            message.setStatus(Message.MessageStatus.SENT);
        }
        if (message.getPriority() == null) {
            message.setPriority(5); // 默认优先级
        }
        if (message.getSendTime() == null) {
            message.setSendTime(LocalDateTime.now());
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        if (message.getUpdatedAt() == null) {
            message.setUpdatedAt(LocalDateTime.now());
        }

        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesBySenderId(Long senderId) {
        return messageRepository.findBySenderId(senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getReceiversMessages(Long receiverId) {
        return messageRepository.findByReceiverId(receiverId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getConversationBetweenAgents(Long agent1Id, Long agent2Id) {
        return messageRepository.findConversationBetweenAgents(agent1Id, agent2Id);
    }

    @Override
    public Message markMessageAsRead(Long messageId) {
        Optional<Message> existing = messageRepository.findById(messageId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("消息不存在: " + messageId);
        }

        Message message = existing.get();
        if (message.getStatus() != Message.MessageStatus.DELIVERED) {
            throw new IllegalStateException("只有已送达的消息可以标记为已读");
        }

        message.setStatus(Message.MessageStatus.READ);
        message.setReadTime(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    @Override
    public Message markMessageAsDelivered(Long messageId) {
        Optional<Message> existing = messageRepository.findById(messageId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("消息不存在: " + messageId);
        }

        Message message = existing.get();
        if (message.getStatus() != Message.MessageStatus.SENT) {
            throw new IllegalStateException("只有已发送的消息可以标记为已送达");
        }

        message.setStatus(Message.MessageStatus.DELIVERED);
        message.setDeliveredTime(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long receiverId) {
        return messageRepository.countUnreadMessages(receiverId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesByStatus(Message.MessageStatus status) {
        return messageRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getMessages(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    @Override
    public Message replyToMessage(Long originalMessageId, Message replyMessage) {
        Optional<Message> originalMessage = messageRepository.findById(originalMessageId);
        if (originalMessage.isEmpty()) {
            throw new IllegalArgumentException("原始消息不存在: " + originalMessageId);
        }

        Message original = originalMessage.get();
        
        // 设置回复关系
        replyMessage.setReplyTo(original);
        replyMessage.setSender(original.getReceiver()); // 回复方是原消息的接收方
        replyMessage.setReceiver(original.getSender()); // 接收方是原消息的发送方
        
        // 标记原消息为已回复
        original.setRequiresReply(false);
        messageRepository.save(original);

        return sendMessage(replyMessage);
    }

    @Override
    public Message forwardMessage(Long originalMessageId, Long newReceiverId) {
        Optional<Message> originalMessage = messageRepository.findById(originalMessageId);
        Optional<AgentInstance> newReceiver = agentInstanceRepository.findById(newReceiverId);

        if (originalMessage.isEmpty()) {
            throw new IllegalArgumentException("原始消息不存在: " + originalMessageId);
        }
        if (newReceiver.isEmpty()) {
            throw new IllegalArgumentException("新接收方不存在: " + newReceiverId);
        }

        Message original = originalMessage.get();
        
        Message forwardedMessage = new Message();
        forwardedMessage.setSender(original.getSender());
        forwardedMessage.setReceiver(newReceiver.get());
        forwardedMessage.setContent("[转发] " + original.getContent());
        forwardedMessage.setType(original.getType());
        forwardedMessage.setForwardedFrom(original);

        return sendMessage(forwardedMessage);
    }

    @Override
    public void deleteMessage(Long messageId) {
        Optional<Message> existing = messageRepository.findById(messageId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("消息不存在: " + messageId);
        }

        messageRepository.deleteById(messageId);
    }

    @Override
    public int cleanupExpiredMessages(LocalDateTime expireTime) {
        List<Message> expiredMessages = messageRepository.findExpiredMessages(expireTime);
        
        for (Message message : expiredMessages) {
            message.setStatus(Message.MessageStatus.FAILED);
            message.setErrorMessage("消息过期未处理");
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }
        
        return expiredMessages.size();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageStats getMessageStats() {
        long totalMessages = messageRepository.count();
        long sentMessages = messageRepository.countByStatus(Message.MessageStatus.SENT);
        long deliveredMessages = messageRepository.countByStatus(Message.MessageStatus.DELIVERED);
        long readMessages = messageRepository.countByStatus(Message.MessageStatus.READ);
        long failedMessages = messageRepository.countByStatus(Message.MessageStatus.FAILED);
        
        return new MessageStats(totalMessages, sentMessages, deliveredMessages, readMessages, failedMessages);
    }
}