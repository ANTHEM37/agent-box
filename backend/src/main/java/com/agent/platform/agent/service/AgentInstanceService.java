package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.AgentInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 智能体实例服务接口
 */
public interface AgentInstanceService {

    /**
     * 创建智能体实例
     */
    AgentInstance createAgentInstance(Long agentDefinitionId, Long createdBy, String sessionId);

    /**
     * 启动智能体实例
     */
    AgentInstance startAgentInstance(Long instanceId);

    /**
     * 停止智能体实例
     */
    AgentInstance stopAgentInstance(Long instanceId);

    /**
     * 暂停智能体实例
     */
    AgentInstance pauseAgentInstance(Long instanceId);

    /**
     * 恢复智能体实例
     */
    AgentInstance resumeAgentInstance(Long instanceId);

    /**
     * 销毁智能体实例
     */
    void destroyAgentInstance(Long instanceId);

    /**
     * 根据ID获取智能体实例
     */
    Optional<AgentInstance> getAgentInstanceById(Long id);

    /**
     * 根据会话ID获取智能体实例
     */
    Optional<AgentInstance> getAgentInstanceBySessionId(String sessionId);

    /**
     * 获取智能体定义的所有实例
     */
    List<AgentInstance> getAgentInstancesByDefinitionId(Long agentDefinitionId);

    /**
     * 获取所有智能体实例
     */
    List<AgentInstance> getAllAgentInstances();

    /**
     * 分页查询智能体实例
     */
    Page<AgentInstance> getAgentInstances(Pageable pageable);

    /**
     * 根据状态获取智能体实例
     */
    List<AgentInstance> getAgentInstancesByStatus(AgentInstance.InstanceStatus status);

    /**
     * 更新智能体实例状态
     */
    AgentInstance updateAgentInstanceStatus(Long instanceId, AgentInstance.InstanceStatus status);

    /**
     * 更新智能体实例最后活跃时间
     */
    AgentInstance updateLastActiveTime(Long instanceId);

    /**
     * 清理过期实例
     */
    int cleanupExpiredInstances(LocalDateTime expireTime);

    /**
     * 检查实例是否活跃
     */
    boolean isInstanceActive(Long instanceId);

    /**
     * 获取实例统计信息
     */
    InstanceStats getInstanceStats();

    /**
     * 实例统计信息类
     */
    class InstanceStats {
        private long totalInstances;
        private long runningInstances;
        private long stoppedInstances;
        private long errorInstances;

        // 构造函数、getter和setter
        public InstanceStats() {}

        public InstanceStats(long totalInstances, long runningInstances, long stoppedInstances, long errorInstances) {
            this.totalInstances = totalInstances;
            this.runningInstances = runningInstances;
            this.stoppedInstances = stoppedInstances;
            this.errorInstances = errorInstances;
        }

        // getter和setter方法
        public long getTotalInstances() { return totalInstances; }
        public void setTotalInstances(long totalInstances) { this.totalInstances = totalInstances; }
        
        public long getRunningInstances() { return runningInstances; }
        public void setRunningInstances(long runningInstances) { this.runningInstances = runningInstances; }
        
        public long getStoppedInstances() { return stoppedInstances; }
        public void setStoppedInstances(long stoppedInstances) { this.stoppedInstances = stoppedInstances; }
        
        public long getErrorInstances() { return errorInstances; }
        public void setErrorInstances(long errorInstances) { this.errorInstances = errorInstances; }
    }
}