package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.Message;
import com.agent.platform.agent.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     */
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        try {
            Message sent = messageService.sendMessage(message);
            return ResponseEntity.ok(sent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据ID获取消息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        Optional<Message> message = messageService.getMessageById(id);
        return message.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取发送方的所有消息
     */
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Message>> getMessagesBySenderId(@PathVariable Long senderId) {
        List<Message> messages = messageService.getMessagesBySenderId(senderId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取接收方的所有消息
     */
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<Message>> getReceiversMessages(@PathVariable Long receiverId) {
        List<Message> messages = messageService.getReceiversMessages(receiverId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取两个智能体之间的对话
     */
    @GetMapping("/conversation")
    public ResponseEntity<List<Message>> getConversationBetweenAgents(
            @RequestParam Long agent1Id, 
            @RequestParam Long agent2Id) {
        List<Message> messages = messageService.getConversationBetweenAgents(agent1Id, agent2Id);
        return ResponseEntity.ok(messages);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Message> markMessageAsRead(@PathVariable Long id) {
        try {
            Message message = messageService.markMessageAsRead(id);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 标记消息为已送达
     */
    @PostMapping("/{id}/delivered")
    public ResponseEntity<Message> markMessageAsDelivered(@PathVariable Long id) {
        try {
            Message message = messageService.markMessageAsDelivered(id);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/receiver/{receiverId}/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable Long receiverId) {
        long count = messageService.getUnreadMessageCount(receiverId);
        return ResponseEntity.ok(count);
    }

    /**
     * 获取指定状态的消息
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Message>> getMessagesByStatus(@PathVariable Message.MessageStatus status) {
        List<Message> messages = messageService.getMessagesByStatus(status);
        return ResponseEntity.ok(messages);
    }

    /**
     * 分页查询消息
     */
    @GetMapping("/page")
    public ResponseEntity<Page<Message>> getMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageService.getMessages(pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * 回复消息
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<Message> replyToMessage(
            @PathVariable Long id, 
            @RequestBody Message replyMessage) {
        try {
            Message reply = messageService.replyToMessage(id, replyMessage);
            return ResponseEntity.ok(reply);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 转发消息
     */
    @PostMapping("/{id}/forward")
    public ResponseEntity<Message> forwardMessage(
            @PathVariable Long id, 
            @RequestParam Long newReceiverId) {
        try {
            Message forwarded = messageService.forwardMessage(id, newReceiverId);
            return ResponseEntity.ok(forwarded);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 清理过期消息
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Integer> cleanupExpiredMessages(@RequestParam String expireTime) {
        try {
            LocalDateTime expireDateTime = LocalDateTime.parse(expireTime);
            int cleanedCount = messageService.cleanupExpiredMessages(expireDateTime);
            return ResponseEntity.ok(cleanedCount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取消息统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<MessageService.MessageStats> getMessageStats() {
        MessageService.MessageStats stats = messageService.getMessageStats();
        return ResponseEntity.ok(stats);
    }
}