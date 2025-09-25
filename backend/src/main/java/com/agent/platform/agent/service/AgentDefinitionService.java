package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.AgentDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 智能体定义服务接口
 */
public interface AgentDefinitionService {

    /**
     * 创建智能体定义
     */
    AgentDefinition createAgentDefinition(AgentDefinition agentDefinition);

    /**
     * 更新智能体定义
     */
    AgentDefinition updateAgentDefinition(Long id, AgentDefinition agentDefinition);

    /**
     * 根据ID获取智能体定义
     */
    Optional<AgentDefinition> getAgentDefinitionById(Long id);

    /**
     * 根据名称获取智能体定义
     */
    Optional<AgentDefinition> getAgentDefinitionByName(String name);

    /**
     * 获取所有智能体定义
     */
    List<AgentDefinition> getAllAgentDefinitions();

    /**
     * 分页查询智能体定义
     */
    Page<AgentDefinition> getAgentDefinitions(Pageable pageable);

    /**
     * 删除智能体定义
     */
    void deleteAgentDefinition(Long id);

    /**
     * 启用智能体定义
     */
    AgentDefinition enableAgentDefinition(Long id);

    /**
     * 禁用智能体定义
     */
    AgentDefinition disableAgentDefinition(Long id);

    /**
     * 根据类型获取智能体定义
     */
    List<AgentDefinition> getAgentDefinitionsByType(AgentDefinition.AgentType type);

    /**
     * 获取启用的智能体定义
     */
    List<AgentDefinition> getEnabledAgentDefinitions();

    /**
     * 检查名称是否已存在
     */
    boolean existsByName(String name);

    /**
     * 检查名称和版本是否已存在
     */
    boolean existsByNameAndVersion(String name, String version);

    /**
     * 验证智能体定义配置
     */
    boolean validateAgentDefinition(AgentDefinition agentDefinition);
}