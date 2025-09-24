package com.agent.platform.workflow.repository;

import com.agent.platform.workflow.entity.WorkflowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {
    
    // 根据工作流ID查找执行记录
    Page<WorkflowExecution> findByWorkflowIdOrderByStartedAtDesc(Long workflowId, Pageable pageable);
    
    // 根据用户ID查找执行记录
    Page<WorkflowExecution> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);
    
    // 根据状态查找执行记录
    Page<WorkflowExecution> findByUserIdAndStatusOrderByStartedAtDesc(Long userId, WorkflowExecution.ExecutionStatus status, Pageable pageable);
    
    // 查找正在运行的执行
    List<WorkflowExecution> findByStatusAndStartedAtBefore(WorkflowExecution.ExecutionStatus status, LocalDateTime before);
    
    // 根据工作流和用户查找执行记录
    Page<WorkflowExecution> findByWorkflowIdAndUserIdOrderByStartedAtDesc(Long workflowId, Long userId, Pageable pageable);
    
    // 统计执行次数
    @Query("SELECT COUNT(e) FROM WorkflowExecution e WHERE e.workflow.id = :workflowId")
    Long countByWorkflowId(@Param("workflowId") Long workflowId);
    
    @Query("SELECT COUNT(e) FROM WorkflowExecution e WHERE e.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(e) FROM WorkflowExecution e WHERE e.user.id = :userId AND e.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") WorkflowExecution.ExecutionStatus status);
    
    // 统计成功率
    @Query("SELECT COUNT(e) FROM WorkflowExecution e WHERE e.workflow.id = :workflowId AND e.status = 'COMPLETED'")
    Long countSuccessfulByWorkflowId(@Param("workflowId") Long workflowId);
    
    // 计算平均执行时间
    @Query("SELECT AVG(e.durationMs) FROM WorkflowExecution e WHERE e.workflow.id = :workflowId AND e.status = 'COMPLETED'")
    Double getAverageExecutionTime(@Param("workflowId") Long workflowId);
    
    // 查找最近的执行记录
    @Query("SELECT e FROM WorkflowExecution e WHERE e.workflow.id = :workflowId ORDER BY e.startedAt DESC")
    List<WorkflowExecution> findRecentExecutions(@Param("workflowId") Long workflowId, Pageable pageable);
    
    // 根据时间范围查找执行记录
    @Query("SELECT e FROM WorkflowExecution e WHERE e.user.id = :userId AND e.startedAt BETWEEN :startTime AND :endTime ORDER BY e.startedAt DESC")
    Page<WorkflowExecution> findByUserIdAndTimeRange(@Param("userId") Long userId, 
                                                    @Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime, 
                                                    Pageable pageable);
    
    // 删除旧的执行记录
    void deleteByStartedAtBefore(LocalDateTime before);
}