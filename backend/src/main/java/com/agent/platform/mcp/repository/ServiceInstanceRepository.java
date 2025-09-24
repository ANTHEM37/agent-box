package com.agent.platform.mcp.repository;

import com.agent.platform.mcp.entity.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 服务实例仓库接口
 */
@Repository
public interface ServiceInstanceRepository extends JpaRepository<ServiceInstance, Long> {

    /**
     * 根据部署ID查找实例
     */
    List<ServiceInstance> findByDeploymentId(Long deploymentId);

    /**
     * 根据容器ID查找实例
     */
    Optional<ServiceInstance> findByContainerId(String containerId);

    /**
     * 根据状态查找实例
     */
    List<ServiceInstance> findByStatus(ServiceInstance.InstanceStatus status);

    /**
     * 查找部署的运行中实例
     */
    @Query("SELECT i FROM ServiceInstance i WHERE i.deployment.id = :deploymentId AND i.status = 'RUNNING'")
    List<ServiceInstance> findRunningInstancesByDeploymentId(@Param("deploymentId") Long deploymentId);

    /**
     * 查找不健康的实例
     */
    @Query("SELECT i FROM ServiceInstance i WHERE i.status = 'UNHEALTHY' OR " +
           "(i.lastHealthCheck IS NOT NULL AND i.lastHealthCheck < :threshold)")
    List<ServiceInstance> findUnhealthyInstances(@Param("threshold") LocalDateTime threshold);

    /**
     * 统计部署的实例数量
     */
    Long countByDeploymentId(Long deploymentId);

    /**
     * 统计部署的运行中实例数量
     */
    @Query("SELECT COUNT(i) FROM ServiceInstance i WHERE i.deployment.id = :deploymentId AND i.status = 'RUNNING'")
    Long countRunningInstancesByDeploymentId(@Param("deploymentId") Long deploymentId);

    /**
     * 查找需要健康检查的实例
     */
    @Query("SELECT i FROM ServiceInstance i WHERE i.status = 'RUNNING' AND " +
           "(i.lastHealthCheck IS NULL OR i.lastHealthCheck < :threshold)")
    List<ServiceInstance> findInstancesNeedingHealthCheck(@Param("threshold") LocalDateTime threshold);
}