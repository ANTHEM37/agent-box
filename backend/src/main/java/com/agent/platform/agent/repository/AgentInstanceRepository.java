package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.AgentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 智能体实例数据访问接口
 */
@Repository
public interface AgentInstanceRepository extends JpaRepository<AgentInstance, Long>, JpaSpecificationExecutor<AgentInstance> {

    /**
     * 根据会话ID查找智能体实例
     */
    Optional<AgentInstance> findBySessionId(String sessionId);

    /**
     * 根据智能体定义ID查找实例
     */
    List<AgentInstance> findByAgentDefinitionId(Long agentDefinitionId);

    /**
     * 根据状态查找智能体实例
     */
    List<AgentInstance> findByStatus(AgentInstance.InstanceStatus status);

    /**
     * 根据创建者查找智能体实例
     */
    List<AgentInstance> findByCreatedBy(Long createdBy);

    /**
     * 查找最后活跃时间早于指定时间的实例
     */
    List<AgentInstance> findByLastActiveTimeBefore(LocalDateTime time);

    /**
     * 统计指定智能体定义的实例数量
     */
    long countByAgentDefinitionId(Long agentDefinitionId);

    /**
     * 检查会话ID是否存在
     */
    boolean existsBySessionId(String sessionId);

    /**
     * 根据智能体定义ID和状态查找实例
     */
    List<AgentInstance> findByAgentDefinitionIdAndStatus(Long agentDefinitionId, AgentInstance.InstanceStatus status);

    /**
     * 查找需要清理的过期实例
     */
    @Query("SELECT ai FROM AgentInstance ai WHERE ai.lastActiveTime < :expireTime AND ai.status IN :statuses")
    List<AgentInstance> findExpiredInstances(@Param("expireTime") LocalDateTime expireTime, 
                                           @Param("statuses") List<AgentInstance.InstanceStatus> statuses);
}