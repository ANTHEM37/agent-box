package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.Message;
import com.agent.platform.agent.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/messages")
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
}