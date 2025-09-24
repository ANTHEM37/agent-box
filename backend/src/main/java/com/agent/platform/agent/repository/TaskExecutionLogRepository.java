package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.TaskExecutionLog;
import com.agent.platform.agent.entity.LogLevel;
import com.agent.platform.agent.entity.ExecutionPhase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务执行日志仓库接口
 */
@Repository
public interface TaskExecutionLogRepository extends JpaRepository<TaskExecutionLog, Long> {
    
    /**
     * 根据任务ID查找日志
     */
    List<TaskExecutionLog> findByTaskIdOrderByTimestampDesc(Long taskId);
    
    /**
     * 根据任务ID和日志级别查找日志
     */
    List<TaskExecutionLog> findByTaskIdAndLevelOrderByTimestampDesc(Long taskId, LogLevel level);
    
    /**
     * 根据任务ID和执行阶段查找日志
     */
    List<TaskExecutionLog> findByTaskIdAndPhaseOrderByTimestampDesc(Long taskId, ExecutionPhase phase);
    
    /**
     * 查找错误日志
     */
    @Query("SELECT l FROM TaskExecutionLog l WHERE l.level = 'ERROR' ORDER BY l.timestamp DESC")
    List<TaskExecutionLog> findErrorLogs(Pageable pageable);
    
    /**
     * 分页查询日志
     */
    @Query("SELECT l FROM TaskExecutionLog l WHERE " +
           "(:taskId IS NULL OR l.task.id = :taskId) AND " +
           "(:level IS NULL OR l.level = :level) AND " +
           "(:phase IS NULL OR l.phase = :phase) AND " +
           "(:startTime IS NULL OR l.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR l.timestamp <= :endTime) " +
           "ORDER BY l.timestamp DESC")
    Page<TaskExecutionLog> findLogsWithFilters(
        @Param("taskId") Long taskId,
        @Param("level") LogLevel level,
        @Param("phase") ExecutionPhase phase,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    /**
     * 统计日志数量
     */
    Long countByTaskId(Long taskId);
    
    Long countByTaskIdAndLevel(Long taskId, LogLevel level);
    
    Long countByLevel(LogLevel level);
    
    /**
     * 清理旧日志
     */
    @Query("DELETE FROM TaskExecutionLog l WHERE l.timestamp < :threshold")
    void deleteOldLogs(@Param("threshold") LocalDateTime threshold);
}