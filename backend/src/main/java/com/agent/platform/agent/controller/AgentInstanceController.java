package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.service.AgentInstanceService;
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
 * 智能体实例控制器
 */
@RestController
@RequestMapping("/api/agent-instances")
@CrossOrigin(origins = "*")
public class AgentInstanceController {

    @Autowired
    private AgentInstanceService agentInstanceService;

    /**
     * 创建智能体实例
     */
    @PostMapping
    public ResponseEntity<AgentInstance> createAgentInstance(
            @RequestParam Long agentDefinitionId,
            @RequestParam Long createdBy,
            @RequestParam(required = false) String sessionId) {
        try {
            AgentInstance instance = agentInstanceService.createAgentInstance(agentDefinitionId, createdBy, sessionId);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 启动智能体实例
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<AgentInstance> startAgentInstance(@PathVariable Long id) {
        try {
            AgentInstance instance = agentInstanceService.startAgentInstance(id);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 停止智能体实例
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<AgentInstance> stopAgentInstance(@PathVariable Long id) {
        try {
            AgentInstance instance = agentInstanceService.stopAgentInstance(id);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 暂停智能体实例
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<AgentInstance> pauseAgentInstance(@PathVariable Long id) {
        try {
            AgentInstance instance = agentInstanceService.pauseAgentInstance(id);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 恢复智能体实例
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<AgentInstance> resumeAgentInstance(@PathVariable Long id) {
        try {
            AgentInstance instance = agentInstanceService.resumeAgentInstance(id);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 销毁智能体实例
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroyAgentInstance(@PathVariable Long id) {
        try {
            agentInstanceService.destroyAgentInstance(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据ID获取智能体实例
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgentInstance> getAgentInstanceById(@PathVariable Long id) {
        Optional<AgentInstance> instance = agentInstanceService.getAgentInstanceById(id);
        return instance.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据会话ID获取智能体实例
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<AgentInstance> getAgentInstanceBySessionId(@PathVariable String sessionId) {
        Optional<AgentInstance> instance = agentInstanceService.getAgentInstanceBySessionId(sessionId);
        return instance.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取智能体定义的所有实例
     */
    @GetMapping("/definition/{agentDefinitionId}")
    public ResponseEntity<List<AgentInstance>> getAgentInstancesByDefinitionId(@PathVariable Long agentDefinitionId) {
        List<AgentInstance> instances = agentInstanceService.getAgentInstancesByDefinitionId(agentDefinitionId);
        return ResponseEntity.ok(instances);
    }

    /**
     * 获取所有智能体实例
     */
    @GetMapping
    public ResponseEntity<List<AgentInstance>> getAllAgentInstances() {
        List<AgentInstance> instances = agentInstanceService.getAllAgentInstances();
        return ResponseEntity.ok(instances);
    }

    /**
     * 分页查询智能体实例
     */
    @GetMapping("/page")
    public ResponseEntity<Page<AgentInstance>> getAgentInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AgentInstance> instances = agentInstanceService.getAgentInstances(pageable);
        return ResponseEntity.ok(instances);
    }

    /**
     * 根据状态获取智能体实例
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgentInstance>> getAgentInstancesByStatus(@PathVariable AgentInstance.InstanceStatus status) {
        List<AgentInstance> instances = agentInstanceService.getAgentInstancesByStatus(status);
        return ResponseEntity.ok(instances);
    }

    /**
     * 更新智能体实例状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<AgentInstance> updateAgentInstanceStatus(
            @PathVariable Long id, 
            @RequestParam AgentInstance.InstanceStatus status) {
        try {
            AgentInstance instance = agentInstanceService.updateAgentInstanceStatus(id, status);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新智能体实例最后活跃时间
     */
    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<AgentInstance> updateLastActiveTime(@PathVariable Long id) {
        try {
            AgentInstance instance = agentInstanceService.updateLastActiveTime(id);
            return ResponseEntity.ok(instance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 清理过期实例
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Integer> cleanupExpiredInstances(@RequestParam String expireTime) {
        try {
            LocalDateTime expireDateTime = LocalDateTime.parse(expireTime);
            int cleanedCount = agentInstanceService.cleanupExpiredInstances(expireDateTime);
            return ResponseEntity.ok(cleanedCount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 检查实例是否活跃
     */
    @GetMapping("/{id}/active")
    public ResponseEntity<Boolean> isInstanceActive(@PathVariable Long id) {
        boolean isActive = agentInstanceService.isInstanceActive(id);
        return ResponseEntity.ok(isActive);
    }

    /**
     * 获取实例统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<AgentInstanceService.InstanceStats> getInstanceStats() {
        AgentInstanceService.InstanceStats stats = agentInstanceService.getInstanceStats();
        return ResponseEntity.ok(stats);
    }
}