package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.AgentDefinition;
import com.agent.platform.agent.service.AgentDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 智能体定义控制器
 */
@RestController
@RequestMapping("/agent-definitions")
@CrossOrigin(origins = "*")
public class AgentDefinitionController {

    @Autowired
    private AgentDefinitionService agentDefinitionService;

    /**
     * 创建智能体定义
     */
    @PostMapping
    public ResponseEntity<AgentDefinition> createAgentDefinition(@RequestBody AgentDefinition agentDefinition) {
        try {
            AgentDefinition created = agentDefinitionService.createAgentDefinition(agentDefinition);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新智能体定义
     */
    @PutMapping("/{id}")
    public ResponseEntity<AgentDefinition> updateAgentDefinition(
            @PathVariable Long id, 
            @RequestBody AgentDefinition agentDefinition) {
        try {
            AgentDefinition updated = agentDefinitionService.updateAgentDefinition(id, agentDefinition);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据ID获取智能体定义
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgentDefinition> getAgentDefinitionById(@PathVariable Long id) {
        Optional<AgentDefinition> agentDefinition = agentDefinitionService.getAgentDefinitionById(id);
        return agentDefinition.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有智能体定义
     */
    @GetMapping
    public ResponseEntity<List<AgentDefinition>> getAllAgentDefinitions() {
        List<AgentDefinition> agentDefinitions = agentDefinitionService.getAllAgentDefinitions();
        return ResponseEntity.ok(agentDefinitions);
    }

    /**
     * 删除智能体定义
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgentDefinition(@PathVariable Long id) {
        try {
            agentDefinitionService.deleteAgentDefinition(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}