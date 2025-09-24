package com.agent.platform.mcp.repository;

import com.agent.platform.mcp.entity.ServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 服务使用统计仓库接口
 */
@Repository
public interface ServiceUsageRepository extends JpaRepository<ServiceUsage, Long> {

    /**
     * 根据部署ID和日期查找使用记录
     */
    Optional<ServiceUsage> findByDeploymentIdAndUserIdAndDate(Long deploymentId, Long userId, LocalDate date);

    /**
     * 根据用户ID查找使用记录
     */
    List<ServiceUsage> findByUserId(Long userId);

    /**
     * 根据部署ID查找使用记录
     */
    List<ServiceUsage> findByDeploymentId(Long deploymentId);

    /**
     * 查找用户在指定日期范围内的使用记录
     */
    @Query("SELECT u FROM ServiceUsage u WHERE u.userId = :userId AND u.date BETWEEN :startDate AND :endDate")
    List<ServiceUsage> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查找部署在指定日期范围内的使用记录
     */
    @Query("SELECT u FROM ServiceUsage u WHERE u.deployment.id = :deploymentId AND u.date BETWEEN :startDate AND :endDate")
    List<ServiceUsage> findByDeploymentIdAndDateRange(@Param("deploymentId") Long deploymentId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 统计用户的总请求数
     */
    @Query("SELECT SUM(u.requestCount) FROM ServiceUsage u WHERE u.userId = :userId")
    Long sumRequestCountByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的总费用
     */
    @Query("SELECT SUM(u.cost) FROM ServiceUsage u WHERE u.userId = :userId")
    BigDecimal sumCostByUserId(@Param("userId") Long userId);

    /**
     * 统计部署的总请求数
     */
    @Query("SELECT SUM(u.requestCount) FROM ServiceUsage u WHERE u.deployment.id = :deploymentId")
    Long sumRequestCountByDeploymentId(@Param("deploymentId") Long deploymentId);

    /**
     * 统计服务的总使用量
     */
    @Query("SELECT SUM(u.requestCount) FROM ServiceUsage u WHERE u.deployment.service.id = :serviceId")
    Long sumRequestCountByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 查找今日使用记录
     */
    @Query("SELECT u FROM ServiceUsage u WHERE u.date = CURRENT_DATE")
    List<ServiceUsage> findTodayUsage();

    /**
     * 统计用户今日费用
     */
    @Query("SELECT SUM(u.cost) FROM ServiceUsage u WHERE u.userId = :userId AND u.date = CURRENT_DATE")
    BigDecimal sumTodayCostByUserId(@Param("userId") Long userId);
}