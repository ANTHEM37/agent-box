package com.agent.platform.mcp.repository;

import com.agent.platform.mcp.entity.McpService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MCP 服务仓库接口
 */
@Repository
public interface McpServiceRepository extends JpaRepository<McpService, Long> {

    /**
     * 根据名称查找服务
     */
    Optional<McpService> findByName(String name);

    /**
     * 根据作者ID查找服务
     */
    List<McpService> findByAuthorId(Long authorId);

    /**
     * 根据状态查找服务
     */
    List<McpService> findByStatus(McpService.ServiceStatus status);

    /**
     * 查找已发布的服务
     */
    @Query("SELECT s FROM McpService s WHERE s.status = 'PUBLISHED'")
    Page<McpService> findPublishedServices(Pageable pageable);

    /**
     * 根据分类查找服务
     */
    Page<McpService> findByCategory(String category, Pageable pageable);

    /**
     * 根据标签查找服务
     */
    @Query("SELECT s FROM McpService s WHERE :tag MEMBER OF s.tags AND s.status = 'PUBLISHED'")
    Page<McpService> findByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * 搜索服务（按名称、显示名称、描述）
     */
    @Query("SELECT s FROM McpService s WHERE " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "s.status = 'PUBLISHED'")
    Page<McpService> searchServices(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找精选服务
     */
    @Query("SELECT s FROM McpService s WHERE s.featured = true AND s.status = 'PUBLISHED'")
    List<McpService> findFeaturedServices();

    /**
     * 查找热门服务（按下载量排序）
     */
    @Query("SELECT s FROM McpService s WHERE s.status = 'PUBLISHED' ORDER BY s.downloadsCount DESC")
    Page<McpService> findPopularServices(Pageable pageable);

    /**
     * 查找高评分服务
     */
    @Query("SELECT s FROM McpService s WHERE s.status = 'PUBLISHED' AND s.ratingAverage >= :minRating ORDER BY s.ratingAverage DESC")
    Page<McpService> findHighRatedServices(@Param("minRating") Double minRating, Pageable pageable);

    /**
     * 获取所有分类
     */
    @Query("SELECT DISTINCT s.category FROM McpService s WHERE s.category IS NOT NULL AND s.status = 'PUBLISHED'")
    List<String> findAllCategories();

    /**
     * 获取所有标签
     */
    @Query("SELECT DISTINCT tag FROM McpService s JOIN s.tags tag WHERE s.status = 'PUBLISHED'")
    List<String> findAllTags();

    /**
     * 统计服务数量
     */
    @Query("SELECT COUNT(s) FROM McpService s WHERE s.status = 'PUBLISHED'")
    Long countPublishedServices();

    /**
     * 统计作者的服务数量
     */
    Long countByAuthorId(Long authorId);
}