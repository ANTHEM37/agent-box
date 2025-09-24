package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.*;
import com.agent.platform.agent.repository.*;
import com.agent.platform.agent.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能体管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentManagementService {
    
    private final AIAgentRepository agentRepository;
    private final AgentMemoryRepository memoryRepository;
    private final AgentTaskRepository taskRepository;
    private final AgentConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;
    private final AgentExecutionService executionService;
    private final MemoryService memoryService;
    
    /**
     * 创建智能体
     */
    @Transactional
    public AgentResponse createAgent(CreateAgentRequest request) {
        log.info("Creating agent: {}", request.getName());
        
        // 验证智能体名称唯一性
        if (agentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Agent name already exists: " + request.getName());
        }
        
        AIAgent agent = new AIAgent();
        agent.setName(request.getName());
        agent.setDisplayName(request.getDisplayName());
        agent.setDescription(request.getDescription());
        agent.setType(AgentType.valueOf(request.getType()));
        agent.setStatus(AgentStatus.DRAFT);
        agent.setCreatorId(request.getCreatorId());
        agent.setCreatorName(request.getCreatorName());
        agent.setAvatarUrl(request.getAvatarUrl());
        
        // 设置个性配置
        if (request.getPersonalityConfig() != null) {
            try {
                agent.setPersonalityConfig(objectMapper.writeValueAsString(request.getPersonalityConfig()));
            } catch (Exception e) {
                log.error("Failed to serialize personality config", e);
                throw new RuntimeException("Invalid personality config");
            }
        }
        
        // 设置能力配置
        if (request.getCapabilities() != null) {
            try {
                agent.setCapabilitiesConfig(objectMapper.writeValueAsString(request.getCapabilities()));
            } catch (Exception e) {
                log.error("Failed to serialize capabilities config", e);
                throw new RuntimeException("Invalid capabilities config");
            }
        }
        
        // 设置知识库配置
        if (request.getKnowledgeBaseIds() != null) {
            try {
                agent.setKnowledgeBaseConfig(objectMapper.writeValueAsString(request.getKnowledgeBaseIds()));
            } catch (Exception e) {
                log.error("Failed to serialize knowledge base config", e);
                throw new RuntimeException("Invalid knowledge base config");
            }
        }
        
        // 设置工具配置
        if (request.getTools() != null) {
            try {
                agent.setToolsConfig(objectMapper.writeValueAsString(request.getTools()));
            } catch (Exception e) {
                log.error("Failed to serialize tools config", e);
                throw new RuntimeException("Invalid tools config");
            }
        }
        
        // 设置模型配置
        if (request.getModelConfig() != null) {
            try {
                agent.setModelConfig(objectMapper.writeValueAsString(request.getModelConfig()));
            } catch (Exception e) {
                log.error("Failed to serialize model config", e);
                throw new RuntimeException("Invalid model config");
            }
        }
        
        agent.setSystemPrompt(request.getSystemPrompt());
        agent.setTemperature(request.getTemperature());
        agent.setMaxTokens(request.getMaxTokens());
        agent.setMemoryEnabled(request.getMemoryEnabled());
        agent.setMemoryCapacity(request.getMemoryCapacity());
        agent.setLearningEnabled(request.getLearningEnabled());
        agent.setCollaborationEnabled(request.getCollaborationEnabled());
        agent.setPriority(request.getPriority());
        agent.setIsPublic(request.getIsPublic());
        
        // 设置标签
        if (request.getTags() != null) {
            try {
                agent.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (Exception e) {
                log.error("Failed to serialize tags", e);
                throw new RuntimeException("Invalid tags");
            }
        }
        
        agent = agentRepository.save(agent);
        
        log.info("Agent created successfully: {}", agent.getId());
        return convertToResponse(agent);
    }
    
    /**
     * 更新智能体
     */
    @Transactional
    public AgentResponse updateAgent(Long agentId, UpdateAgentRequest request) {
        log.info("Updating agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        // 更新基本信息
        if (request.getDisplayName() != null) {
            agent.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            agent.setDescription(request.getDescription());
        }
        if (request.getAvatarUrl() != null) {
            agent.setAvatarUrl(request.getAvatarUrl());
        }
        
        // 更新配置信息
        if (request.getPersonalityConfig() != null) {
            try {
                agent.setPersonalityConfig(objectMapper.writeValueAsString(request.getPersonalityConfig()));
            } catch (Exception e) {
                log.error("Failed to serialize personality config", e);
                throw new RuntimeException("Invalid personality config");
            }
        }
        
        if (request.getSystemPrompt() != null) {
            agent.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getTemperature() != null) {
            agent.setTemperature(request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            agent.setMaxTokens(request.getMaxTokens());
        }
        
        // 增加版本号
        agent.setVersion(agent.getVersion() + 1);
        
        agent = agentRepository.save(agent);
        
        log.info("Agent updated successfully: {}", agent.getId());
        return convertToResponse(agent);
    }
    
    /**
     * 激活智能体
     */
    @Transactional
    public void activateAgent(Long agentId) {
        log.info("Activating agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        agent.setStatus(AgentStatus.ACTIVE);
        agent.setLastActiveAt(LocalDateTime.now());
        agentRepository.save(agent);
        
        log.info("Agent activated successfully: {}", agentId);
    }
    
    /**
     * 停用智能体
     */
    @Transactional
    public void deactivateAgent(Long agentId) {
        log.info("Deactivating agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        agent.setStatus(AgentStatus.INACTIVE);
        agentRepository.save(agent);
        
        log.info("Agent deactivated successfully: {}", agentId);
    }
    
    /**
     * 删除智能体
     */
    @Transactional
    public void deleteAgent(Long agentId) {
        log.info("Deleting agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        agent.setStatus(AgentStatus.DELETED);
        agent.setIsDeleted(true);
        agentRepository.save(agent);
        
        log.info("Agent deleted successfully: {}", agentId);
    }
    
    /**
     * 获取智能体详情
     */
    public AgentResponse getAgent(Long agentId) {
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        return convertToResponse(agent);
    }
    
    /**
     * 获取智能体列表
     */
    public Page<AgentResponse> getAgents(AgentSearchRequest request, Pageable pageable) {
        Page<AIAgent> agents = agentRepository.findAgentsWithFilters(
            request.getKeyword(),
            request.getType(),
            request.getStatus(),
            request.getCreatorId(),
            request.getIsPublic(),
            pageable
        );
        
        return agents.map(this::convertToResponse);
    }
    
    /**
     * 执行任务
     */
    @Transactional
    public TaskExecutionResult executeTask(Long agentId, TaskExecutionRequest request) {
        log.info("Executing task for agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        if (agent.getStatus() != AgentStatus.ACTIVE) {
            throw new IllegalStateException("Agent is not active: " + agentId);
        }
        
        // 创建任务记录
        AgentTask task = new AgentTask();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(TaskType.valueOf(request.getType()));
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(request.getPriority());
        task.setAgent(agent);
        task.setCreatorId(request.getCreatorId());
        task.setRequiresCollaboration(request.getRequiresCollaboration());
        
        try {
            task.setInput(objectMapper.writeValueAsString(request.getInput()));
        } catch (Exception e) {
            log.error("Failed to serialize task input", e);
            throw new RuntimeException("Invalid task input");
        }
        
        task = taskRepository.save(task);
        
        // 执行任务
        TaskExecutionResult result = executionService.executeTask(agent, task, request);
        
        // 更新智能体统计信息
        updateAgentStats(agent, result);
        
        return result;
    }
    
    /**
     * 获取智能体统计信息
     */
    public AgentStatsResponse getAgentStats(Long agentId) {
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        AgentStatsResponse stats = new AgentStatsResponse();
        stats.setAgentId(agentId);
        stats.setUsageCount(agent.getUsageCount());
        stats.setSuccessRate(agent.getSuccessRate());
        stats.setAvgResponseTime(agent.getAvgResponseTime());
        
        // 获取任务统计
        Long totalTasks = taskRepository.countByAgentId(agentId);
        Long completedTasks = taskRepository.countByAgentIdAndStatus(agentId, TaskStatus.COMPLETED);
        Long failedTasks = taskRepository.countByAgentIdAndStatus(agentId, TaskStatus.FAILED);
        
        stats.setTotalTasks(totalTasks);
        stats.setCompletedTasks(completedTasks);
        stats.setFailedTasks(failedTasks);
        
        // 获取记忆统计
        Long totalMemories = memoryRepository.countByAgentId(agentId);
        stats.setTotalMemories(totalMemories);
        
        // 获取对话统计
        Long totalConversations = conversationRepository.countByAgentId(agentId);
        stats.setTotalConversations(totalConversations);
        
        return stats;
    }
    
    /**
     * 更新智能体统计信息
     */
    private void updateAgentStats(AIAgent agent, TaskExecutionResult result) {
        agent.setUsageCount(agent.getUsageCount() + 1);
        agent.setLastActiveAt(LocalDateTime.now());
        
        if (result.getResponseTime() != null) {
            // 计算平均响应时间
            long currentAvg = agent.getAvgResponseTime();
            long newAvg = (currentAvg * (agent.getUsageCount() - 1) + result.getResponseTime()) / agent.getUsageCount();
            agent.setAvgResponseTime(newAvg);
        }
        
        // 计算成功率
        Long completedTasks = taskRepository.countByAgentIdAndStatus(agent.getId(), TaskStatus.COMPLETED);
        Long totalTasks = taskRepository.countByAgentId(agent.getId());
        if (totalTasks > 0) {
            double successRate = (double) completedTasks / totalTasks * 100;
            agent.setSuccessRate(successRate);
        }
        
        agentRepository.save(agent);
    }
    
    /**
     * 转换为响应对象
     */
    private AgentResponse convertToResponse(AIAgent agent) {
        AgentResponse response = new AgentResponse();
        response.setId(agent.getId());
        response.setName(agent.getName());
        response.setDisplayName(agent.getDisplayName());
        response.setDescription(agent.getDescription());
        response.setType(agent.getType().name());
        response.setStatus(agent.getStatus().name());
        response.setCreatorId(agent.getCreatorId());
        response.setCreatorName(agent.getCreatorName());
        response.setAvatarUrl(agent.getAvatarUrl());
        response.setSystemPrompt(agent.getSystemPrompt());
        response.setTemperature(agent.getTemperature());
        response.setMaxTokens(agent.getMaxTokens());
        response.setMemoryEnabled(agent.getMemoryEnabled());
        response.setMemoryCapacity(agent.getMemoryCapacity());
        response.setLearningEnabled(agent.getLearningEnabled());
        response.setCollaborationEnabled(agent.getCollaborationEnabled());
        response.setPriority(agent.getPriority());
        response.setIsPublic(agent.getIsPublic());
        response.setUsageCount(agent.getUsageCount());
        response.setSuccessRate(agent.getSuccessRate());
        response.setAvgResponseTime(agent.getAvgResponseTime());
        response.setLastActiveAt(agent.getLastActiveAt());
        response.setVersion(agent.getVersion());
        response.setCreatedAt(agent.getCreatedAt());
        response.setUpdatedAt(agent.getUpdatedAt());
        
        // 解析JSON配置
        try {
            if (agent.getPersonalityConfig() != null) {
                response.setPersonalityConfig(objectMapper.readValue(agent.getPersonalityConfig(), Map.class));
            }
            if (agent.getCapabilitiesConfig() != null) {
                response.setCapabilities(objectMapper.readValue(agent.getCapabilitiesConfig(), List.class));
            }
            if (agent.getKnowledgeBaseConfig() != null) {
                response.setKnowledgeBaseIds(objectMapper.readValue(agent.getKnowledgeBaseConfig(), List.class));
            }
            if (agent.getToolsConfig() != null) {
                response.setTools(objectMapper.readValue(agent.getToolsConfig(), List.class));
            }
            if (agent.getModelConfig() != null) {
                response.setModelConfig(objectMapper.readValue(agent.getModelConfig(), Map.class));
            }
            if (agent.getTags() != null) {
                response.setTags(objectMapper.readValue(agent.getTags(), List.class));
            }
        } catch (Exception e) {
            log.error("Failed to parse agent config", e);
        }
        
        return response;
    }
}