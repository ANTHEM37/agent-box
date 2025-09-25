package com.agent.platform.agent.engine;

import com.agent.platform.agent.entity.Task;
import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.service.TaskService;
import com.agent.platform.agent.service.AgentInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 任务分发器 - 负责任务的调度和分发
 */
@Component
public class TaskDispatcher {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AgentInstanceService agentInstanceService;

    @Autowired
    private AgentTaskExecutor agentTaskExecutor;

    // 任务队列
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    
    // 线程池
    private final ExecutorService dispatcherThread = Executors.newSingleThreadExecutor();
    
    // 运行标志
    private volatile boolean running = false;

    /**
     * 启动任务分发器
     */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        dispatcherThread.submit(this::dispatchLoop);
        System.out.println("任务分发器已启动");
    }

    /**
     * 停止任务分发器
     */
    public void stop() {
        running = false;
        dispatcherThread.shutdown();
        try {
            if (!dispatcherThread.awaitTermination(5, TimeUnit.SECONDS)) {
                dispatcherThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            dispatcherThread.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("任务分发器已停止");
    }

    /**
     * 提交任务到队列
     */
    public void submitTask(Task task) {
        try {
            taskQueue.put(task);
            System.out.println("任务已提交到队列: " + task.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("提交任务被中断", e);
        }
    }

    /**
     * 提交协作任务
     */
    public void submitCollaborativeTask(Task mainTask, List<Long> agentInstanceIds) {
        // 设置协作标记
        mainTask.getParameters().put("collaborative", true);
        mainTask.getParameters().put("participantAgents", agentInstanceIds);
        
        submitTask(mainTask);
    }

    /**
     * 分发循环
     */
    private void dispatchLoop() {
        while (running) {
            try {
                // 从队列中获取任务
                Task task = taskQueue.poll(1, TimeUnit.SECONDS);
                
                if (task != null) {
                    dispatchTask(task);
                }
                
                // 检查超时任务
                checkTimeoutTasks();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("任务分发异常: " + e.getMessage());
                // 继续运行，不中断循环
            }
        }
    }

    /**
     * 分发单个任务
     */
    private void dispatchTask(Task task) {
        try {
            // 检查任务状态
            if (task.getStatus() != Task.TaskStatus.PENDING) {
                System.out.println("任务状态非待处理，跳过分发: " + task.getId());
                return;
            }

            // 获取关联的智能体实例
            AgentInstance agentInstance = agentInstanceService.getAgentInstanceById(task.getAgentInstance().getId())
                    .orElseThrow(() -> new IllegalArgumentException("智能体实例不存在: " + task.getAgentInstance().getId()));

            // 检查智能体实例状态
            if (agentInstance.getStatus() != AgentInstance.InstanceStatus.RUNNING) {
                System.out.println("智能体实例未运行，无法执行任务: " + agentInstance.getId());
                taskService.failTask(task.getId(), "智能体实例未运行");
                return;
            }

            // 更新任务状态为执行中
            taskService.startTaskExecution(task.getId());

            // 提交给任务执行器
            agentTaskExecutor.executeTask(task, agentInstance);

            System.out.println("任务已分发给智能体实例: " + task.getId() + " -> " + agentInstance.getId());

        } catch (Exception e) {
            System.err.println("任务分发失败: " + task.getId() + " - " + e.getMessage());
            try {
                taskService.failTask(task.getId(), "分发失败: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("更新任务状态失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 检查超时任务
     */
    private void checkTimeoutTasks() {
        try {
            // 获取超时任务（超过30分钟未完成）
            long timeoutThreshold = System.currentTimeMillis() - (30 * 60 * 1000);
            
            List<Task> timeoutTasks = taskService.getTasksByStatus(Task.TaskStatus.RUNNING)
                    .stream()
                    .filter(task -> task.getStartedAt() != null && 
                                   task.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() < timeoutThreshold)
                    .toList();
            
            for (Task task : timeoutTasks) {
                System.out.println("检测到超时任务: " + task.getId());
                taskService.failTask(task.getId(), "任务执行超时");
            }
            
        } catch (Exception e) {
            System.err.println("检查超时任务异常: " + e.getMessage());
        }
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * 获取待处理任务
     */
    public List<Task> getPendingTasks() {
        return taskQueue.stream().toList();
    }

    /**
     * 清空任务队列
     */
    public void clearQueue() {
        taskQueue.clear();
        System.out.println("任务队列已清空");
    }
}