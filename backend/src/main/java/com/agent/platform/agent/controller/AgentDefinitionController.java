package com.agent.platform.agent.controller;

import com.agent.platform.agent.entity.AgentDefinition;
import com.agent.platform.agent.service.AgentDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 智能体定义控制器
 */
@RestController
@RequestMapping("/api/agent-definitions")
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
     * 根据名称获取智能体定义
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<AgentDefinition> getAgentDefinitionByName(@PathVariable String name) {
        Optional<AgentDefinition> agentDefinition = agentDefinitionService.getAgentDefinitionByName(name);
        return agentDefinition.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有智能体定义
     */
    @GetMapping
    public ResponseEntity<List<AgentDefinition>> getAllAgentDefinitions() {
        List<AgentDefinition> definitions = agentDefinitionService.getAllAgentDefinitions();
        return ResponseEntity.ok(definitions);
    }

    /**
     * 分页查询智能体定义
     */
    @GetMapping("/page")
    public ResponseEntity<Page<AgentDefinition>> getAgentDefinitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AgentDefinition> definitions = agentDefinitionService.getAgentDefinitions(pageable);
        return ResponseEntity.ok(definitions);
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

    /**
     * 启用智能体定义
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<AgentDefinition> enableAgentDefinition(@PathVariable Long id) {
        try {
            AgentDefinition enabled = agentDefinitionService.enableAgentDefinition(id);
            return ResponseEntity.ok(enabled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 禁用智能体定义
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<AgentDefinition> disableAgentDefinition(@PathVariable Long id) {
        try {
            AgentDefinition disabled = agentDefinitionService.disableAgentDefinition(id);
            return ResponseEntity.ok(disabled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据类型获取智能体定义
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AgentDefinition>> getAgentDefinitionsByType(@PathVariable AgentDefinition.AgentType type) {
        List<AgentDefinition> definitions = agentDefinitionService.getAgentDefinitionsByType(type);
        return ResponseEntity.ok(definitions);
    }

    /**
     * 获取启用的智能体定义
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<AgentDefinition>> getEnabledAgentDefinitions() {
        List<AgentDefinition> definitions = agentDefinitionService.getEnabledAgentDefinitions();
        return ResponseEntity.ok(definitions);
    }

    /**
     * 检查名称是否已存在
     */
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        boolean exists = agentDefinitionService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    /**
     * 验证智能体定义配置
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateAgentDefinition(@RequestBody AgentDefinition agentDefinition) {
        boolean isValid = agentDefinitionService.validateAgentDefinition(agentDefinition);
        return ResponseEntity.ok(isValid);
    }
}