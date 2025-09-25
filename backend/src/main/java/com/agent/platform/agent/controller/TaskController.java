package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.Task;
import com.agent.platform.agent.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/tasks")
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
}