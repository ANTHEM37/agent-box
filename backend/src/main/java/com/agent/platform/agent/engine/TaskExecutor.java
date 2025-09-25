package com.agent.platform.agent.engine;

import com.agent.platform.agent.entity.Task;
import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.entity.Message;
import com.agent.platform.agent.service.TaskService;
import com.agent.platform.agent.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 任务执行器 - 负责具体任务的执行
 */
@Component
public class TaskExecutor {

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AgentProcessor agentProcessor;

    // 线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // 任务执行状态跟踪
    private final Map<Long, Future<?>> executingTasks = new ConcurrentHashMap<>();

    /**
     * 执行任务
     */
    public void executeTask(Task task, AgentInstance agentInstance) {
        Future<?> future = executorService.submit(() -> {
            try {
                // 执行任务逻辑
                String result = processTask(task, agentInstance);
                
                // 完成任务
                taskService.completeTask(task.getId(), result);
                
                System.out.println("任务执行完成: " + task.getId());
                
            } catch (Exception e) {
                System.err.println("任务执行失败: " + task.getId() + " - " + e.getMessage());
                try {
                    taskService.failTask(task.getId(), "执行失败: " + e.getMessage());
                } catch (Exception ex) {
                    System.err.println("更新任务状态失败: " + ex.getMessage());
                }
            } finally {
                // 从执行中任务中移除
                executingTasks.remove(task.getId());
            }
        });
        
        // 记录执行中的任务
        executingTasks.put(task.getId(), future);
    }

    /**
     * 处理任务逻辑
     */
    private String processTask(Task task, AgentInstance agentInstance) throws Exception {
        // 根据任务类型执行不同的处理逻辑
        switch (task.getTaskType()) {
            case TEXT_PROCESSING:
                return processTextTask(task, agentInstance);
                
            case DATA_ANALYSIS:
                return processDataAnalysisTask(task, agentInstance);
                
            case DECISION_MAKING:
                return processDecisionMakingTask(task, agentInstance);
                
            case COLLABORATIVE:
                return processCollaborativeTask(task, agentInstance);
                
            default:
                return processGenericTask(task, agentInstance);
        }
    }

    /**
     * 处理文本任务
     */
    private String processTextTask(Task task, AgentInstance agentInstance) throws Exception {
        String input = task.getInput();
        Map<String, Object> parameters = task.getParameters();
        
        // 调用智能体处理器处理文本
        return agentProcessor.processText(agentInstance, input, parameters);
    }

    /**
     * 处理数据分析任务
     */
    private String processDataAnalysisTask(Task task, AgentInstance agentInstance) throws Exception {
        String input = task.getInput();
        Map<String, Object> parameters = task.getParameters();
        
        // 调用智能体处理器进行数据分析
        return agentProcessor.analyzeData(agentInstance, input, parameters);
    }

    /**
     * 处理决策任务
     */
    private String processDecisionMakingTask(Task task, AgentInstance agentInstance) throws Exception {
        String input = task.getInput();
        Map<String, Object> parameters = task.getParameters();
        
        // 调用智能体处理器进行决策
        return agentProcessor.makeDecision(agentInstance, input, parameters);
    }

    /**
     * 处理协作任务
     */
    private String processCollaborativeTask(Task task, AgentInstance agentInstance) throws Exception {
        // 获取参与协作的智能体列表
        @SuppressWarnings("unchecked")
        java.util.List<Long> participantAgents = (java.util.List<Long>) 
                task.getParameters().get("participantAgents");
        
        if (participantAgents == null || participantAgents.isEmpty()) {
            throw new IllegalArgumentException("协作任务缺少参与者信息");
        }
        
        // 创建协作消息
        Message collaborationMessage = new Message();
        collaborationMessage.setSenderId(agentInstance.getId());
        collaborationMessage.setContent(task.getInput());
        collaborationMessage.setMessageType("COLLABORATION_REQUEST");
        
        // 向所有参与者发送消息
        for (Long participantId : participantAgents) {
            if (!participantId.equals(agentInstance.getId())) {
                collaborationMessage.setReceiverId(participantId);
                messageService.sendMessage(collaborationMessage);
            }
        }
        
        // 等待协作结果（简化实现）
        return "协作任务已启动，等待参与者响应";
    }

    /**
     * 处理通用任务
     */
    private String processGenericTask(Task task, AgentInstance agentInstance) throws Exception {
        String input = task.getInput();
        Map<String, Object> parameters = task.getParameters();
        
        // 调用智能体处理器处理通用任务
        return agentProcessor.processGenericTask(agentInstance, input, parameters);
    }

    /**
     * 取消任务执行
     */
    public boolean cancelTask(Long taskId) {
        Future<?> future = executingTasks.get(taskId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            if (cancelled) {
                executingTasks.remove(taskId);
                System.out.println("任务已取消: " + taskId);
            }
            return cancelled;
        }
        return false;
    }

    /**
     * 获取执行中的任务数量
     */
    public int getExecutingTaskCount() {
        return executingTasks.size();
    }

    /**
     * 检查任务是否正在执行
     */
    public boolean isTaskExecuting(Long taskId) {
        Future<?> future = executingTasks.get(taskId);
        return future != null && !future.isDone();
    }

    /**
     * 停止所有任务执行
     */
    public void stopAllTasks() {
        executingTasks.values().forEach(future -> future.cancel(true));
        executingTasks.clear();
        System.out.println("所有任务执行已停止");
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        stopAllTasks();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}