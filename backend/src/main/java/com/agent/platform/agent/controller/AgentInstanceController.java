package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.service.AgentInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 智能体实例控制器
 */
@RestController
@RequestMapping("/agent-instances")
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
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}