package com.agent.platform.mcp.repository;

import com.agent.platform.mcp.entity.ServiceDeployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 服务部署仓库接口
 */
@Repository
public interface ServiceDeploymentRepository extends JpaRepository<ServiceDeployment, Long> {

    /**
     * 根据用户ID查找部署
     */
    Page<ServiceDeployment> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据服务ID查找部署
     */
    List<ServiceDeployment> findByServiceId(Long serviceId);

    /**
     * 根据用户ID和服务ID查找部署
     */
    List<ServiceDeployment> findByUserIdAndServiceId(Long userId, Long serviceId);

    /**
     * 根据部署名称和用户ID查找
     */
    Optional<ServiceDeployment> findByDeploymentNameAndUserId(String deploymentName, Long userId);

    /**
     * 根据状态查找部署
     */
    List<ServiceDeployment> findByStatus(ServiceDeployment.DeploymentStatus status);

    /**
     * 查找用户的运行中部署
     */
    @Query("SELECT d FROM ServiceDeployment d WHERE d.userId = :userId AND d.status = 'RUNNING'")
    List<ServiceDeployment> findRunningDeploymentsByUserId(@Param("userId") Long userId);

    /**
     * 查找服务的活跃部署数量
     */
    @Query("SELECT COUNT(d) FROM ServiceDeployment d WHERE d.service.id = :serviceId AND d.status IN ('RUNNING', 'DEPLOYING')")
    Long countActiveDeploymentsByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 统计用户的部署数量
     */
    Long countByUserId(Long userId);

    /**
     * 统计用户运行中的部署数量
     */
    @Query("SELECT COUNT(d) FROM ServiceDeployment d WHERE d.userId = :userId AND d.status = 'RUNNING'")
    Long countRunningDeploymentsByUserId(@Param("userId") Long userId);
}