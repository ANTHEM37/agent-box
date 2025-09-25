package com.agent.platform.agent.service.impl;

import com.agent.platform.agent.entity.AgentDefinition;
import com.agent.platform.agent.repository.AgentDefinitionRepository;
import com.agent.platform.agent.service.AgentDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 智能体定义服务实现类
 */
@Service
@Transactional
public class AgentDefinitionServiceImpl implements AgentDefinitionService {

    @Autowired
    private AgentDefinitionRepository agentDefinitionRepository;

    @Override
    public AgentDefinition createAgentDefinition(AgentDefinition agentDefinition) {
        // 验证名称唯一性
        if (agentDefinitionRepository.existsByName(agentDefinition.getName())) {
            throw new IllegalArgumentException("智能体定义名称已存在: " + agentDefinition.getName());
        }

        // 设置默认值
        if (agentDefinition.getVersion() == null) {
            agentDefinition.setVersion("1.0.0");
        }
        if (agentDefinition.getEnabled() == null) {
            agentDefinition.setEnabled(true);
        }
        if (agentDefinition.getCreatedAt() == null) {
            agentDefinition.setCreatedAt(LocalDateTime.now());
        }
        if (agentDefinition.getUpdatedAt() == null) {
            agentDefinition.setUpdatedAt(LocalDateTime.now());
        }

        return agentDefinitionRepository.save(agentDefinition);
    }

    @Override
    public AgentDefinition updateAgentDefinition(Long id, AgentDefinition agentDefinition) {
        Optional<AgentDefinition> existing = agentDefinitionRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体定义不存在: " + id);
        }

        AgentDefinition existingDefinition = existing.get();
        
        // 更新字段
        if (agentDefinition.getName() != null) {
            // 检查名称是否与其他定义冲突
            if (!existingDefinition.getName().equals(agentDefinition.getName()) && 
                agentDefinitionRepository.existsByName(agentDefinition.getName())) {
                throw new IllegalArgumentException("智能体定义名称已存在: " + agentDefinition.getName());
            }
            existingDefinition.setName(agentDefinition.getName());
        }
        
        if (agentDefinition.getDescription() != null) {
            existingDefinition.setDescription(agentDefinition.getDescription());
        }
        
        if (agentDefinition.getType() != null) {
            existingDefinition.setType(agentDefinition.getType());
        }
        
        if (agentDefinition.getConfig() != null) {
            existingDefinition.setConfig(agentDefinition.getConfig());
        }
        
        if (agentDefinition.getCapabilities() != null) {
            existingDefinition.setCapabilities(agentDefinition.getCapabilities());
        }
        
        if (agentDefinition.getVersion() != null) {
            existingDefinition.setVersion(agentDefinition.getVersion());
        }
        
        existingDefinition.setUpdatedAt(LocalDateTime.now());

        return agentDefinitionRepository.save(existingDefinition);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentDefinition> getAgentDefinitionById(Long id) {
        return agentDefinitionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentDefinition> getAgentDefinitionByName(String name) {
        return agentDefinitionRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDefinition> getAllAgentDefinitions() {
        return agentDefinitionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentDefinition> getAgentDefinitions(Pageable pageable) {
        return agentDefinitionRepository.findAll(pageable);
    }

    @Override
    public void deleteAgentDefinition(Long id) {
        Optional<AgentDefinition> existing = agentDefinitionRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体定义不存在: " + id);
        }
        agentDefinitionRepository.deleteById(id);
    }

    @Override
    public AgentDefinition enableAgentDefinition(Long id) {
        Optional<AgentDefinition> existing = agentDefinitionRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体定义不存在: " + id);
        }

        AgentDefinition definition = existing.get();
        definition.setEnabled(true);
        definition.setUpdatedAt(LocalDateTime.now());
        return agentDefinitionRepository.save(definition);
    }

    @Override
    public AgentDefinition disableAgentDefinition(Long id) {
        Optional<AgentDefinition> existing = agentDefinitionRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体定义不存在: " + id);
        }

        AgentDefinition definition = existing.get();
        definition.setEnabled(false);
        definition.setUpdatedAt(LocalDateTime.now());
        return agentDefinitionRepository.save(definition);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDefinition> getAgentDefinitionsByType(AgentDefinition.AgentType type) {
        return agentDefinitionRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDefinition> getEnabledAgentDefinitions() {
        return agentDefinitionRepository.findByEnabledTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return agentDefinitionRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndVersion(String name, String version) {
        return agentDefinitionRepository.existsByNameAndVersion(name, version);
    }

    @Override
    public boolean validateAgentDefinition(AgentDefinition agentDefinition) {
        if (agentDefinition.getName() == null || agentDefinition.getName().trim().isEmpty()) {
            return false;
        }
        if (agentDefinition.getType() == null) {
            return false;
        }
        if (agentDefinition.getConfig() == null || agentDefinition.getConfig().trim().isEmpty()) {
            return false;
        }
        return true;
    }
}