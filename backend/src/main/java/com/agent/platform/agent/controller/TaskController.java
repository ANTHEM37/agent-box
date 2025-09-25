package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.Task;
import com.agent.platform.agent.service.TaskService;
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
 * 任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        try {
            Task created = taskService.createTask(task);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据ID获取任务
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取智能体实例的所有任务
     */
    @GetMapping("/agent-instance/{agentInstanceId}")
    public ResponseEntity<List<Task>> getTasksByAgentInstanceId(@PathVariable Long agentInstanceId) {
        List<Task> tasks = taskService.getTasksByAgentInstanceId(agentInstanceId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取指定状态的任务
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取待处理任务（按优先级排序）
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Task>> getPendingTasksOrderByPriority() {
        List<Task> tasks = taskService.getPendingTasksOrderByPriority();
        return ResponseEntity.ok(tasks);
    }

    /**
     * 更新任务状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long id, 
            @RequestParam Task.TaskStatus status) {
        try {
            Task task = taskService.updateTaskStatus(id, status);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 开始执行任务
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Task> startTaskExecution(@PathVariable Long id) {
        try {
            Task task = taskService.startTaskExecution(id);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 完成任务
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(
            @PathVariable Long id, 
            @RequestParam String result) {
        try {
            Task task = taskService.completeTask(id, result);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 任务执行失败
     */
    @PostMapping("/{id}/fail")
    public ResponseEntity<Task> failTask(
            @PathVariable Long id, 
            @RequestParam String errorMessage) {
        try {
            Task task = taskService.failTask(id, errorMessage);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Task> cancelTask(@PathVariable Long id) {
        try {
            Task task = taskService.cancelTask(id);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 重试失败的任务
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Task> retryTask(@PathVariable Long id) {
        try {
            Task task = taskService.retryTask(id);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取子任务
     */
    @GetMapping("/{id}/subtasks")
    public ResponseEntity<List<Task>> getSubTasks(@PathVariable Long id) {
        List<Task> subTasks = taskService.getSubTasks(id);
        return ResponseEntity.ok(subTasks);
    }

    /**
     * 分页查询任务
     */
    @GetMapping("/page")
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 处理超时任务
     */
    @PostMapping("/handle-timeout")
    public ResponseEntity<Integer> handleTimeoutTasks(@RequestParam String timeoutTime) {
        try {
            LocalDateTime timeoutDateTime = LocalDateTime.parse(timeoutTime);
            int handledCount = taskService.handleTimeoutTasks(timeoutDateTime);
            return ResponseEntity.ok(handledCount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskService.TaskStats> getTaskStats() {
        TaskService.TaskStats stats = taskService.getTaskStats();
        return ResponseEntity.ok(stats);
    }
}