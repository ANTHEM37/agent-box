package com.agent.platform.agent.service.impl;

import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.entity.Task;
import com.agent.platform.agent.repository.AgentInstanceRepository;
import com.agent.platform.agent.repository.TaskRepository;
import com.agent.platform.agent.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务服务实现类
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AgentInstanceRepository agentInstanceRepository;

    @Override
    public Task createTask(Task task) {
        // 验证智能体实例存在
        if (task.getAgentInstance() == null || task.getAgentInstance().getId() == null) {
            throw new IllegalArgumentException("任务必须关联有效的智能体实例");
        }

        Optional<AgentInstance> agentInstance = agentInstanceRepository.findById(task.getAgentInstance().getId());
        if (agentInstance.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + task.getAgentInstance().getId());
        }

        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus(Task.TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(5); // 默认优先级
        }
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }
        if (task.getUpdatedAt() == null) {
            task.setUpdatedAt(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByAgentInstanceId(Long agentInstanceId) {
        return taskRepository.findByAgentInstanceId(agentInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getPendingTasksOrderByPriority() {
        return taskRepository.findPendingTasksOrderByPriority();
    }

    @Override
    public Task updateTaskStatus(Long taskId, Task.TaskStatus status) {
        Optional<Task> existing = taskRepository.findById(taskId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        Task task = existing.get();
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public Task startTaskExecution(Long taskId) {
        Optional<Task> existing = taskRepository.findById(taskId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        Task task = existing.get();
        if (task.getStatus() != Task.TaskStatus.PENDING) {
            throw new IllegalStateException("只有待处理的任务可以开始执行");
        }

        task.setStatus(Task.TaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public Task completeTask(Long taskId, String result) {
        Optional<Task> existing = taskRepository.findById(taskId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        Task task = existing.get();
        if (task.getStatus() != Task.TaskStatus.RUNNING) {
            throw new IllegalStateException("只有运行中的任务可以完成");
        }

        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setResult(result);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public Task failTask(Long taskId, String errorMessage) {
        Optional<Task> existing = taskRepository.findById(taskId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        Task task = existing.get();
        task.setStatus(Task.TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public Task cancelTask(Long taskId) {
        Optional<Task> existing = taskRepository.findById(taskId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        Task task = existing.get();
        if (task.getStatus() != Task.TaskStatus.PENDING && task.getStatus() != Task.TaskStatus.RUNNING) {
            throw new IllegalStateException("只有待处理或运行中的任务可以取消");
        }

        task.setStatus(Task.TaskStatus.CANCELLED);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public Task retryTask(Long taskId) {
        Optional<Task> existing = taskRepository.findById(taskId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        Task task = existing.get();
        if (task.getStatus() != Task.TaskStatus.FAILED) {
            throw new IllegalStateException("只有失败的任务可以重试");
        }

        task.setStatus(Task.TaskStatus.PENDING);
        task.setErrorMessage(null);
        task.setRetryCount(task.getRetryCount() + 1);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getSubTasks(Long parentTaskId) {
        return taskRepository.findByParentTaskId(parentTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @Override
    public int handleTimeoutTasks(LocalDateTime timeoutTime) {
        List<Task> timeoutTasks = taskRepository.findTimeoutTasks(timeoutTime);
        
        for (Task task : timeoutTasks) {
            task.setStatus(Task.TaskStatus.FAILED);
            task.setErrorMessage("任务执行超时");
            task.setCompletedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);
        }
        
        return timeoutTasks.size();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStats getTaskStats() {
        long totalTasks = taskRepository.count();
        long pendingTasks = taskRepository.countByStatus(Task.TaskStatus.PENDING);
        long runningTasks = taskRepository.countByStatus(Task.TaskStatus.RUNNING);
        long completedTasks = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
        long failedTasks = taskRepository.countByStatus(Task.TaskStatus.FAILED);
        
        return new TaskStats(totalTasks, pendingTasks, runningTasks, completedTasks, failedTasks);
    }
}