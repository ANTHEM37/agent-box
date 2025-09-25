package com.agent.platform.agent.engine;

import com.agent.platform.agent.entity.Message;
import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.service.MessageService;
import com.agent.platform.agent.service.AgentInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消息路由器 - 负责消息的路由和传递
 */
@Component
public class MessageRouter {

    @Autowired
    private MessageService messageService;

    @Autowired
    private AgentInstanceService agentInstanceService;

    @Autowired
    private AgentProcessor agentProcessor;

    // 消息队列
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    
    // 消息处理器线程池
    private final ExecutorService routerThread = Executors.newSingleThreadExecutor();
    
    // 运行标志
    private volatile boolean running = false;
    
    // 消息处理回调注册表
    private final Map<String, MessageHandler> messageHandlers = new ConcurrentHashMap<>();

    /**
     * 初始化消息路由器
     */
    public void initialize() {
        // 注册默认消息处理器
        registerMessageHandler("TEXT_MESSAGE", this::handleTextMessage);
        registerMessageHandler("COLLABORATION_REQUEST", this::handleCollaborationRequest);
        registerMessageHandler("TASK_RESULT", this::handleTaskResult);
        registerMessageHandler("SYSTEM_NOTIFICATION", this::handleSystemNotification);
    }

    /**
     * 启动消息路由器
     */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        routerThread.submit(this::routeLoop);
        System.out.println("消息路由器已启动");
    }

    /**
     * 停止消息路由器
     */
    public void stop() {
        running = false;
        routerThread.shutdown();
        System.out.println("消息路由器已停止");
    }

    /**
     * 路由消息
     */
    public void routeMessage(Message message) {
        try {
            messageQueue.put(message);
            System.out.println("消息已加入路由队列: " + message.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("路由消息被中断", e);
        }
    }

    /**
     * 注册消息处理器
     */
    public void registerMessageHandler(String messageType, MessageHandler handler) {
        messageHandlers.put(messageType, handler);
        System.out.println("注册消息处理器: " + messageType);
    }

    /**
     * 路由循环
     */
    private void routeLoop() {
        while (running) {
            try {
                // 从队列中获取消息
                Message message = messageQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                
                if (message != null) {
                    processMessage(message);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("消息路由异常: " + e.getMessage());
                // 继续运行，不中断循环
            }
        }
    }

    /**
     * 处理单个消息
     */
    private void processMessage(Message message) {
        try {
            // 检查接收者是否存在且活跃
            AgentInstance receiver = agentInstanceService.getAgentInstanceById(message.getReceiverId())
                    .orElseThrow(() -> new IllegalArgumentException("接收者不存在: " + message.getReceiverId()));

            if (receiver.getStatus() != AgentInstance.InstanceStatus.RUNNING) {
                System.out.println("接收者未运行，无法传递消息: " + receiver.getId());
                messageService.markMessageAsDelivered(message.getId());
                return;
            }

            // 根据消息类型选择处理器
            MessageHandler handler = messageHandlers.get(message.getMessageType());
            if (handler != null) {
                handler.handle(message, receiver);
            } else {
                // 使用默认处理器
                handleDefaultMessage(message, receiver);
            }

            // 标记消息为已送达
            messageService.markMessageAsDelivered(message.getId());

            System.out.println("消息已路由到接收者: " + message.getId() + " -> " + receiver.getId());

        } catch (Exception e) {
            System.err.println("消息处理失败: " + message.getId() + " - " + e.getMessage());
            try {
                messageService.markMessageAsDelivered(message.getId());
            } catch (Exception ex) {
                System.err.println("更新消息状态失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(Message message, AgentInstance receiver) {
        // 调用智能体处理器处理文本消息
        String response = agentProcessor.processMessage(receiver, message.getContent());
        
        // 发送回复消息（如果需要）
        if (response != null && !response.isEmpty()) {
            Message reply = new Message();
            reply.setSenderId(receiver.getId());
            reply.setReceiverId(message.getSenderId());
            reply.setContent(response);
            reply.setMessageType("TEXT_REPLY");
            
            messageService.sendMessage(reply);
            routeMessage(reply);
        }
    }

    /**
     * 处理协作请求
     */
    private void handleCollaborationRequest(Message message, AgentInstance receiver) {
        // 处理协作请求逻辑
        String collaborationResponse = agentProcessor.processCollaborationRequest(receiver, message.getContent());
        
        // 发送协作响应
        Message response = new Message();
        response.setSenderId(receiver.getId());
        response.setReceiverId(message.getSenderId());
        response.setContent(collaborationResponse);
        response.setMessageType("COLLABORATION_RESPONSE");
        
        messageService.sendMessage(response);
        routeMessage(response);
    }

    /**
     * 处理任务结果
     */
    private void handleTaskResult(Message message, AgentInstance receiver) {
        // 处理任务结果逻辑
        agentProcessor.processTaskResult(receiver, message.getContent());
    }

    /**
     * 处理系统通知
     */
    private void handleSystemNotification(Message message, AgentInstance receiver) {
        // 处理系统通知逻辑
        agentProcessor.processSystemNotification(receiver, message.getContent());
    }

    /**
     * 处理默认消息
     */
    private void handleDefaultMessage(Message message, AgentInstance receiver) {
        // 默认消息处理逻辑
        System.out.println("处理默认消息: " + message.getMessageType() + " -> " + receiver.getId());
        
        // 调用智能体处理器的通用消息处理方法
        agentProcessor.processGenericMessage(receiver, message);
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return messageQueue.size();
    }

    /**
     * 清空消息队列
     */
    public void clearQueue() {
        messageQueue.clear();
        System.out.println("消息队列已清空");
    }

    /**
     * 消息处理器接口
     */
    @FunctionalInterface
    public interface MessageHandler {
        void handle(Message message, AgentInstance receiver);
    }
}