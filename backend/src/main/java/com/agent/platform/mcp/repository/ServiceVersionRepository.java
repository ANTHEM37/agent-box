package com.agent.platform.mcp.repository;

import com.agent.platform.mcp.entity.ServiceVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 服务版本仓库接口
 */
@Repository
public interface ServiceVersionRepository extends JpaRepository<ServiceVersion, Long> {

    /**
     * 根据服务ID查找所有版本
     */
    List<ServiceVersion> findByServiceIdOrderByCreatedAtDesc(Long serviceId);

    /**
     * 根据服务ID和版本号查找
     */
    Optional<ServiceVersion> findByServiceIdAndVersion(Long serviceId, String version);

    /**
     * 查找服务的最新版本
     */
    @Query("SELECT v FROM ServiceVersion v WHERE v.service.id = :serviceId AND v.isLatest = true")
    Optional<ServiceVersion> findLatestByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 查找服务的所有版本（按版本号排序）
     */
    @Query("SELECT v FROM ServiceVersion v WHERE v.service.id = :serviceId ORDER BY v.version DESC")
    List<ServiceVersion> findVersionsByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 统计服务的版本数量
     */
    Long countByServiceId(Long serviceId);
}