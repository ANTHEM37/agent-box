package com.agent.platform.agent.engine;

import com.agent.platform.agent.entity.AgentDefinition;
import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.entity.Task;
import com.agent.platform.agent.entity.Message;
import com.agent.platform.agent.service.AgentDefinitionService;
import com.agent.platform.agent.service.AgentInstanceService;
import com.agent.platform.agent.service.TaskService;
import com.agent.platform.agent.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 智能体引擎 - 核心调度和协作引擎
 */
@Component
public class AgentEngine {

    @Autowired
    private AgentDefinitionService agentDefinitionService;

    @Autowired
    private AgentInstanceService agentInstanceService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskDispatcher taskDispatcher;

    @Autowired
    private MessageRouter messageRouter;

    // 线程池管理
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // 活跃实例缓存
    private final Map<Long, AgentInstance> activeInstances = new ConcurrentHashMap<>();
    
    // 任务执行状态跟踪
    private final Map<Long, Future<?>> runningTasks = new ConcurrentHashMap<>();

    /**
     * 启动智能体引擎
     */
    public void startEngine() {
        // 加载所有启用的智能体定义
        List<AgentDefinition> enabledDefinitions = agentDefinitionService.getEnabledAgentDefinitions();
        
        // 初始化消息路由
        messageRouter.initialize();
        
        // 启动任务分发器
        taskDispatcher.start();
        
        System.out.println("智能体引擎已启动，加载了 " + enabledDefinitions.size() + " 个智能体定义");
    }

    /**
     * 停止智能体引擎
     */
    public void stopEngine() {
        // 停止所有运行中的任务
        runningTasks.values().forEach(future -> future.cancel(true));
        
        // 停止所有活跃实例
        activeInstances.values().forEach(instance -> {
            try {
                agentInstanceService.stopAgentInstance(instance.getId());
            } catch (Exception e) {
                // 忽略停止过程中的异常
            }
        });
        
        // 关闭线程池
        executorService.shutdownNow();
        
        // 停止任务分发器
        taskDispatcher.stop();
        
        System.out.println("智能体引擎已停止");
    }

    /**
     * 创建智能体实例并启动
     */
    public AgentInstance createAndStartAgentInstance(Long agentDefinitionId, Long createdBy, String sessionId) {
        try {
            // 创建实例
            AgentInstance instance = agentInstanceService.createAgentInstance(agentDefinitionId, createdBy, sessionId);
            
            // 启动实例
            instance = agentInstanceService.startAgentInstance(instance.getId());
            
            // 添加到活跃实例缓存
            activeInstances.put(instance.getId(), instance);
            
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("创建并启动智能体实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提交任务给智能体
     */
    public Task submitTaskToAgent(Long agentInstanceId, String taskType, String input, Map<String, Object> parameters) {
        try {
            // 创建任务
            Task task = new Task();
            task.setAgentInstanceId(agentInstanceId);
            task.setTaskTypeString(taskType);
            task.setInput(input);
            task.setParameters(parameters);
            task.setPriority(5); // 默认优先级
            task.setStatus(Task.TaskStatus.PENDING);
            
            Task createdTask = taskService.createTask(task);
            
            // 提交给任务分发器
            taskDispatcher.submitTask(createdTask);
            
            return createdTask;
        } catch (Exception e) {
            throw new RuntimeException("提交任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送消息给智能体
     */
    public Message sendMessageToAgent(Long senderId, Long receiverId, String content, String messageType) {
        try {
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content);
            message.setMessageType(messageType);
            message.setStatus(Message.MessageStatus.SENT);
            
            Message sentMessage = messageService.sendMessage(message);
            
            // 通过消息路由发送
            messageRouter.routeMessage(sentMessage);
            
            return sentMessage;
        } catch (Exception e) {
            throw new RuntimeException("发送消息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取智能体状态
     */
    public AgentEngineStatus getEngineStatus() {
        AgentEngineStatus status = new AgentEngineStatus();
        status.setActiveInstanceCount(activeInstances.size());
        status.setRunningTaskCount(runningTasks.size());
        status.setTotalAgentDefinitions(agentDefinitionService.getAllAgentDefinitions().size());
        status.setTotalAgentInstances(agentInstanceService.getAllAgentInstances().size());
        
        return status;
    }

    /**
     * 执行智能体协作任务
     */
    public Task executeCollaborativeTask(List<Long> agentInstanceIds, String taskDescription, Map<String, Object> parameters) {
        try {
            // 创建主任务
            Task mainTask = new Task();
            mainTask.setTaskTypeString("COLLABORATIVE");
            mainTask.setInput(taskDescription);
            mainTask.setParameters(parameters);
            mainTask.setPriority(1); // 高优先级
            mainTask.setStatus(Task.TaskStatus.PENDING);
            
            // 设置协作参数
            mainTask.getParameters().put("collaborativeAgents", agentInstanceIds);
            mainTask.getParameters().put("isCollaborative", true);
            
            Task createdTask = taskService.createTask(mainTask);
            
            // 提交给协作任务处理器
            taskDispatcher.submitCollaborativeTask(createdTask, agentInstanceIds);
            
            return createdTask;
        } catch (Exception e) {
            throw new RuntimeException("执行协作任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取智能体实例的当前状态
     */
    public AgentInstance getAgentInstanceStatus(Long agentInstanceId) {
        return activeInstances.get(agentInstanceId);
    }

    /**
     * 强制停止智能体实例
     */
    public void forceStopAgentInstance(Long agentInstanceId) {
        try {
            // 停止实例
            agentInstanceService.stopAgentInstance(agentInstanceId);
            
            // 从活跃实例中移除
            activeInstances.remove(agentInstanceId);
            
            // 取消相关任务
            runningTasks.entrySet().stream()
                .filter(entry -> {
                    // 这里需要根据任务关联的实例ID来过滤
                    // 简化实现：取消所有任务
                    return true;
                })
                .forEach(entry -> entry.getValue().cancel(true));
                
        } catch (Exception e) {
            throw new RuntimeException("强制停止智能体实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 引擎状态类
     */
    public static class AgentEngineStatus {
        private int activeInstanceCount;
        private int runningTaskCount;
        private int totalAgentDefinitions;
        private int totalAgentInstances;
        
        // getters and setters
        public int getActiveInstanceCount() { return activeInstanceCount; }
        public void setActiveInstanceCount(int activeInstanceCount) { this.activeInstanceCount = activeInstanceCount; }
        
        public int getRunningTaskCount() { return runningTaskCount; }
        public void setRunningTaskCount(int runningTaskCount) { this.runningTaskCount = runningTaskCount; }
        
        public int getTotalAgentDefinitions() { return totalAgentDefinitions; }
        public void setTotalAgentDefinitions(int totalAgentDefinitions) { this.totalAgentDefinitions = totalAgentDefinitions; }
        
        public int getTotalAgentInstances() { return totalAgentInstances; }
        public void setTotalAgentInstances(int totalAgentInstances) { this.totalAgentInstances = totalAgentInstances; }
    }
}