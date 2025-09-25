package com.agent.platform.agent.service.impl;

import com.agent.platform.agent.entity.AgentDefinition;
import com.agent.platform.agent.entity.AgentInstance;
import com.agent.platform.agent.repository.AgentDefinitionRepository;
import com.agent.platform.agent.repository.AgentInstanceRepository;
import com.agent.platform.agent.service.AgentInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 智能体实例服务实现类
 */
@Service
@Transactional
public class AgentInstanceServiceImpl implements AgentInstanceService {

    @Autowired
    private AgentInstanceRepository agentInstanceRepository;

    @Autowired
    private AgentDefinitionRepository agentDefinitionRepository;

    @Override
    public AgentInstance createAgentInstance(Long agentDefinitionId, Long createdBy, String sessionId) {
        // 验证智能体定义存在
        Optional<AgentDefinition> agentDefinition = agentDefinitionRepository.findById(agentDefinitionId);
        if (agentDefinition.isEmpty()) {
            throw new IllegalArgumentException("智能体定义不存在: " + agentDefinitionId);
        }

        // 验证智能体定义是否启用
        if (!agentDefinition.get().getEnabled()) {
            throw new IllegalArgumentException("智能体定义未启用: " + agentDefinitionId);
        }

        // 生成会话ID（如果未提供）
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = generateSessionId();
        } else {
            // 检查会话ID是否已存在
            if (agentInstanceRepository.existsBySessionId(sessionId)) {
                throw new IllegalArgumentException("会话ID已存在: " + sessionId);
            }
        }

        AgentInstance instance = new AgentInstance();
        instance.setAgentDefinition(agentDefinition.get());
        instance.setSessionId(sessionId);
        instance.setCreatedBy(createdBy);
        instance.setStatus(AgentInstance.InstanceStatus.CREATED);
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setLastActiveTime(LocalDateTime.now());

        return agentInstanceRepository.save(instance);
    }

    @Override
    public AgentInstance startAgentInstance(Long instanceId) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        if (instance.getStatus() != AgentInstance.InstanceStatus.CREATED && 
            instance.getStatus() != AgentInstance.InstanceStatus.STOPPED) {
            throw new IllegalStateException("智能体实例状态不允许启动: " + instance.getStatus());
        }

        instance.setStatus(AgentInstance.InstanceStatus.RUNNING);
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setLastActiveTime(LocalDateTime.now());

        return agentInstanceRepository.save(instance);
    }

    @Override
    public AgentInstance stopAgentInstance(Long instanceId) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        instance.setStatus(AgentInstance.InstanceStatus.STOPPED);
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setLastActiveTime(LocalDateTime.now());

        return agentInstanceRepository.save(instance);
    }

    @Override
    public AgentInstance pauseAgentInstance(Long instanceId) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        if (instance.getStatus() != AgentInstance.InstanceStatus.RUNNING) {
            throw new IllegalStateException("只有运行中的实例可以暂停");
        }

        instance.setStatus(AgentInstance.InstanceStatus.PAUSED);
        instance.setUpdatedAt(LocalDateTime.now());

        return agentInstanceRepository.save(instance);
    }

    @Override
    public AgentInstance resumeAgentInstance(Long instanceId) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        if (instance.getStatus() != AgentInstance.InstanceStatus.PAUSED) {
            throw new IllegalStateException("只有暂停的实例可以恢复");
        }

        instance.setStatus(AgentInstance.InstanceStatus.RUNNING);
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setLastActiveTime(LocalDateTime.now());

        return agentInstanceRepository.save(instance);
    }

    @Override
    public void destroyAgentInstance(Long instanceId) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        if (instance.getStatus() == AgentInstance.InstanceStatus.RUNNING) {
            throw new IllegalStateException("运行中的实例不能销毁，请先停止");
        }

        agentInstanceRepository.deleteById(instanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentInstance> getAgentInstanceById(Long id) {
        return agentInstanceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentInstance> getAgentInstanceBySessionId(String sessionId) {
        return agentInstanceRepository.findBySessionId(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentInstance> getAgentInstancesByDefinitionId(Long agentDefinitionId) {
        return agentInstanceRepository.findByAgentDefinitionId(agentDefinitionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentInstance> getAllAgentInstances() {
        return agentInstanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentInstance> getAgentInstances(Pageable pageable) {
        return agentInstanceRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentInstance> getAgentInstancesByStatus(AgentInstance.InstanceStatus status) {
        return agentInstanceRepository.findByStatus(status);
    }

    @Override
    public AgentInstance updateAgentInstanceStatus(Long instanceId, AgentInstance.InstanceStatus status) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        instance.setStatus(status);
        instance.setUpdatedAt(LocalDateTime.now());
        
        if (status == AgentInstance.InstanceStatus.RUNNING) {
            instance.setLastActiveTime(LocalDateTime.now());
        }

        return agentInstanceRepository.save(instance);
    }

    @Override
    public AgentInstance updateLastActiveTime(Long instanceId) {
        Optional<AgentInstance> existing = agentInstanceRepository.findById(instanceId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("智能体实例不存在: " + instanceId);
        }

        AgentInstance instance = existing.get();
        instance.setLastActiveTime(LocalDateTime.now());
        return agentInstanceRepository.save(instance);
    }

    @Override
    public int cleanupExpiredInstances(LocalDateTime expireTime) {
        List<AgentInstance.InstanceStatus> cleanupStatuses = Arrays.asList(
            AgentInstance.InstanceStatus.STOPPED,
            AgentInstance.InstanceStatus.ERROR
        );
        
        List<AgentInstance> expiredInstances = agentInstanceRepository.findExpiredInstances(expireTime, cleanupStatuses);
        
        for (AgentInstance instance : expiredInstances) {
            agentInstanceRepository.delete(instance);
        }
        
        return expiredInstances.size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInstanceActive(Long instanceId) {
        Optional<AgentInstance> instance = agentInstanceRepository.findById(instanceId);
        if (instance.isEmpty()) {
            return false;
        }
        
        AgentInstance agentInstance = instance.get();
        return agentInstance.getStatus() == AgentInstance.InstanceStatus.RUNNING && 
               agentInstance.getLastActiveTime().isAfter(LocalDateTime.now().minusMinutes(5));
    }

    @Override
    @Transactional(readOnly = true)
    public InstanceStats getInstanceStats() {
        long totalInstances = agentInstanceRepository.count();
        long runningInstances = agentInstanceRepository.findByStatus(AgentInstance.InstanceStatus.RUNNING).size();
        long stoppedInstances = agentInstanceRepository.findByStatus(AgentInstance.InstanceStatus.STOPPED).size();
        long errorInstances = agentInstanceRepository.findByStatus(AgentInstance.InstanceStatus.ERROR).size();
        
        return new InstanceStats(totalInstances, runningInstances, stoppedInstances, errorInstances);
    }

    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}