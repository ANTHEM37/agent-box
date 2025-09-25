package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务数据访问接口
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * 根据智能体实例ID查找任务
     */
    List<Task> findByAgentInstanceId(Long agentInstanceId);

    /**
     * 根据状态查找任务
     */
    List<Task> findByStatus(Task.TaskStatus status);

    /**
     * 根据父任务ID查找子任务
     */
    List<Task> findByParentTaskId(Long parentTaskId);

    /**
     * 根据智能体实例ID和状态查找任务
     */
    List<Task> findByAgentInstanceIdAndStatus(Long agentInstanceId, Task.TaskStatus status);

    /**
     * 根据优先级排序查找待处理任务
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' ORDER BY t.priority ASC, t.createdAt ASC")
    List<Task> findPendingTasksOrderByPriority();

    /**
     * 统计指定智能体实例的任务数量
     */
    long countByAgentInstanceId(Long agentInstanceId);

    /**
     * 统计指定状态的任务数量
     */
    long countByStatus(Task.TaskStatus status);

    /**
     * 查找指定时间范围内创建的任务
     */
    List<Task> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找超时的任务（创建时间早于指定时间且状态为运行中）
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'RUNNING' AND t.createdAt < :timeoutTime")
    List<Task> findTimeoutTasks(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 根据任务类型查找任务
     */
    List<Task> findByType(Task.TaskType type);

    /**
     * 查找需要重试的失败任务
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'FAILED' AND t.updatedAt > :retryAfterTime")
    List<Task> findRetryableFailedTasks(@Param("retryAfterTime") LocalDateTime retryAfterTime);
}