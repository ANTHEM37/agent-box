package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 创建任务
     */
    Task createTask(Task task);

    /**
     * 根据ID获取任务
     */
    Optional<Task> getTaskById(Long id);

    /**
     * 获取智能体实例的所有任务
     */
    List<Task> getTasksByAgentInstanceId(Long agentInstanceId);

    /**
     * 获取指定状态的任务
     */
    List<Task> getTasksByStatus(Task.TaskStatus status);

    /**
     * 获取待处理任务（按优先级排序）
     */
    List<Task> getPendingTasksOrderByPriority();

    /**
     * 更新任务状态
     */
    Task updateTaskStatus(Long taskId, Task.TaskStatus status);

    /**
     * 开始执行任务
     */
    Task startTaskExecution(Long taskId);

    /**
     * 完成任务
     */
    Task completeTask(Long taskId, String result);

    /**
     * 任务执行失败
     */
    Task failTask(Long taskId, String errorMessage);

    /**
     * 取消任务
     */
    Task cancelTask(Long taskId);

    /**
     * 重试失败的任务
     */
    Task retryTask(Long taskId);

    /**
     * 获取子任务
     */
    List<Task> getSubTasks(Long parentTaskId);

    /**
     * 分页查询任务
     */
    Page<Task> getTasks(Pageable pageable);

    /**
     * 处理超时任务
     */
    int handleTimeoutTasks(LocalDateTime timeoutTime);

    /**
     * 获取任务统计信息
     */
    TaskStats getTaskStats();

    /**
     * 任务统计信息类
     */
    class TaskStats {
        private long totalTasks;
        private long pendingTasks;
        private long runningTasks;
        private long completedTasks;
        private long failedTasks;

        public TaskStats() {}

        public TaskStats(long totalTasks, long pendingTasks, long runningTasks, long completedTasks, long failedTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.runningTasks = runningTasks;
            this.completedTasks = completedTasks;
            this.failedTasks = failedTasks;
        }

        // getter和setter方法
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
        
        public long getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }
        
        public long getRunningTasks() { return runningTasks; }
        public void setRunningTasks(long runningTasks) { this.runningTasks = runningTasks; }
        
        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
        
        public long getFailedTasks() { return failedTasks; }
        public void setFailedTasks(long failedTasks) { this.failedTasks = failedTasks; }
    }
}