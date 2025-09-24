package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.AgentTask;
import com.agent.platform.agent.entity.TaskStatus;
import com.agent.platform.agent.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体任务仓库接口
 */
@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {
    
    /**
     * 根据智能体ID查找任务
     */
    List<AgentTask> findByAgentIdOrderByCreatedAtDesc(Long agentId);
    
    /**
     * 根据智能体ID和状态查找任务
     */
    List<AgentTask> findByAgentIdAndStatusOrderByCreatedAtDesc(Long agentId, TaskStatus status);
    
    /**
     * 根据智能体ID和类型查找任务
     */
    List<AgentTask> findByAgentIdAndTypeOrderByCreatedAtDesc(Long agentId, TaskType type);
    
    /**
     * 根据创建者ID查找任务
     */
    List<AgentTask> findByCreatorIdOrderByCreatedAtDesc(String creatorId);
    
    /**
     * 查找运行中的任务
     */
    @Query("SELECT t FROM AgentTask t WHERE t.status IN ('PENDING', 'RUNNING') ORDER BY t.priority DESC, t.createdAt ASC")
    List<AgentTask> findRunningTasks();
    
    /**
     * 查找超时任务
     */
    @Query("SELECT t FROM AgentTask t WHERE t.status = 'RUNNING' AND t.startedAt < :timeoutThreshold")
    List<AgentTask> findTimeoutTasks(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    /**
     * 分页查询任务
     */
    @Query("SELECT t FROM AgentTask t WHERE " +
           "(:agentId IS NULL OR t.agent.id = :agentId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:creatorId IS NULL OR t.creatorId = :creatorId) " +
           "ORDER BY t.createdAt DESC")
    Page<AgentTask> findTasksWithFilters(
        @Param("agentId") Long agentId,
        @Param("status") TaskStatus status,
        @Param("type") TaskType type,
        @Param("creatorId") String creatorId,
        Pageable pageable
    );
    
    /**
     * 统计任务数量
     */
    Long countByAgentId(Long agentId);
    
    Long countByAgentIdAndStatus(Long agentId, TaskStatus status);
    
    Long countByAgentIdAndType(Long agentId, TaskType type);
    
    Long countByStatus(TaskStatus status);
    
    /**
     * 统计时间范围内的任务
     */
    @Query("SELECT COUNT(t) FROM AgentTask t WHERE t.agent.id = :agentId " +
           "AND t.createdAt BETWEEN :startTime AND :endTime")
    Long countByAgentIdAndCreatedAtBetween(
        @Param("agentId") Long agentId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 查找需要协作的任务
     */
    List<AgentTask> findByRequiresCollaborationTrueAndStatusOrderByPriorityDescCreatedAtAsc(TaskStatus status);
    
    /**
     * 根据优先级查找任务
     */
    List<AgentTask> findByPriorityGreaterThanEqualAndStatusOrderByPriorityDescCreatedAtAsc(Integer priority, TaskStatus status);
}