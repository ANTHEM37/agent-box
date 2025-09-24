package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.AIAgent;
import com.agent.platform.agent.entity.AgentStatus;
import com.agent.platform.agent.entity.AgentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI智能体仓库接口
 */
@Repository
public interface AIAgentRepository extends JpaRepository<AIAgent, Long> {
    
    /**
     * 根据名称查找智能体
     */
    Optional<AIAgent> findByName(String name);
    
    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据创建者ID查找智能体
     */
    List<AIAgent> findByCreatorId(String creatorId);
    
    /**
     * 根据状态查找智能体
     */
    List<AIAgent> findByStatus(AgentStatus status);
    
    /**
     * 根据类型查找智能体
     */
    List<AIAgent> findByType(AgentType type);
    
    /**
     * 查找公开的智能体
     */
    List<AIAgent> findByIsPublicTrueAndStatusOrderByUsageCountDesc(AgentStatus status);
    
    /**
     * 根据标签查找智能体
     */
    @Query("SELECT a FROM AIAgent a WHERE a.tags LIKE %:tag% AND a.status = :status")
    List<AIAgent> findByTagsContainingAndStatus(@Param("tag") String tag, @Param("status") AgentStatus status);
    
    /**
     * 复合条件查询智能体
     */
    @Query("SELECT a FROM AIAgent a WHERE " +
           "(:keyword IS NULL OR a.name LIKE %:keyword% OR a.displayName LIKE %:keyword% OR a.description LIKE %:keyword%) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:creatorId IS NULL OR a.creatorId = :creatorId) AND " +
           "(:isPublic IS NULL OR a.isPublic = :isPublic) AND " +
           "a.isDeleted = false " +
           "ORDER BY a.usageCount DESC, a.createdAt DESC")
    Page<AIAgent> findAgentsWithFilters(
        @Param("keyword") String keyword,
        @Param("type") AgentType type,
        @Param("status") AgentStatus status,
        @Param("creatorId") String creatorId,
        @Param("isPublic") Boolean isPublic,
        Pageable pageable
    );
    
    /**
     * 查找热门智能体
     */
    @Query("SELECT a FROM AIAgent a WHERE a.status = 'ACTIVE' AND a.isPublic = true AND a.isDeleted = false " +
           "ORDER BY a.usageCount DESC, a.successRate DESC")
    List<AIAgent> findPopularAgents(Pageable pageable);
    
    /**
     * 查找推荐智能体
     */
    @Query("SELECT a FROM AIAgent a WHERE a.status = 'ACTIVE' AND a.isPublic = true AND a.isDeleted = false " +
           "AND a.successRate >= :minSuccessRate " +
           "ORDER BY a.successRate DESC, a.usageCount DESC")
    List<AIAgent> findRecommendedAgents(@Param("minSuccessRate") Double minSuccessRate, Pageable pageable);
    
    /**
     * 统计智能体数量
     */
    @Query("SELECT COUNT(a) FROM AIAgent a WHERE a.status = :status AND a.isDeleted = false")
    Long countByStatus(@Param("status") AgentStatus status);
    
    /**
     * 统计创建者的智能体数量
     */
    Long countByCreatorIdAndIsDeletedFalse(String creatorId);
}