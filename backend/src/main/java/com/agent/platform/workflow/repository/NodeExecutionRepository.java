package com.agent.platform.workflow.repository;

import com.agent.platform.workflow.entity.NodeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeExecutionRepository extends JpaRepository<NodeExecution, Long> {
    
    // 根据执行ID查找节点执行记录
    List<NodeExecution> findByExecutionIdOrderByStartedAt(Long executionId);
    
    // 根据执行ID和状态查找节点执行记录
    List<NodeExecution> findByExecutionIdAndStatus(Long executionId, NodeExecution.NodeExecutionStatus status);
    
    // 根据节点类型查找执行记录
    List<NodeExecution> findByExecutionIdAndNodeType(Long executionId, String nodeType);
    
    // 查找失败的节点执行
    List<NodeExecution> findByExecutionIdAndStatusOrderByStartedAt(Long executionId, NodeExecution.NodeExecutionStatus status);
    
    // 统计节点执行情况
    @Query("SELECT COUNT(n) FROM NodeExecution n WHERE n.execution.id = :executionId")
    Long countByExecutionId(@Param("executionId") Long executionId);
    
    @Query("SELECT COUNT(n) FROM NodeExecution n WHERE n.execution.id = :executionId AND n.status = :status")
    Long countByExecutionIdAndStatus(@Param("executionId") Long executionId, @Param("status") NodeExecution.NodeExecutionStatus status);
    
    // 计算节点平均执行时间
    @Query("SELECT AVG(n.durationMs) FROM NodeExecution n WHERE n.nodeType = :nodeType AND n.status = 'COMPLETED'")
    Double getAverageExecutionTimeByNodeType(@Param("nodeType") String nodeType);
    
    // 查找重试次数最多的节点
    @Query("SELECT n FROM NodeExecution n WHERE n.execution.id = :executionId ORDER BY n.retryCount DESC")
    List<NodeExecution> findByExecutionIdOrderByRetryCountDesc(@Param("executionId") Long executionId);
    
    // 根据节点ID查找执行记录
    List<NodeExecution> findByExecutionIdAndNodeId(Long executionId, String nodeId);
    
    // 删除执行记录
    void deleteByExecutionId(Long executionId);
}