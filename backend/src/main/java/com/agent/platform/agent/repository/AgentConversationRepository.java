package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.AgentConversation;
import com.agent.platform.agent.entity.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体对话仓库接口
 */
@Repository
public interface AgentConversationRepository extends JpaRepository<AgentConversation, Long> {
    
    /**
     * 根据智能体ID查找对话
     */
    List<AgentConversation> findByAgentIdOrderByCreatedAtDesc(Long agentId);
    
    /**
     * 根据用户ID查找对话
     */
    List<AgentConversation> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * 根据智能体ID和用户ID查找对话
     */
    List<AgentConversation> findByAgentIdAndUserIdOrderByCreatedAtDesc(Long agentId, String userId);
    
    /**
     * 根据状态查找对话
     */
    List<AgentConversation> findByStatusOrderByCreatedAtDesc(ConversationStatus status);
    
    /**
     * 查找活跃对话
     */
    @Query("SELECT c FROM AgentConversation c WHERE c.status = 'ACTIVE' " +
           "AND c.lastMessageAt > :threshold ORDER BY c.lastMessageAt DESC")
    List<AgentConversation> findActiveConversations(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 分页查询对话
     */
    @Query("SELECT c FROM AgentConversation c WHERE " +
           "(:agentId IS NULL OR c.agent.id = :agentId) AND " +
           "(:userId IS NULL OR c.userId = :userId) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.lastMessageAt DESC")
    Page<AgentConversation> findConversationsWithFilters(
        @Param("agentId") Long agentId,
        @Param("userId") String userId,
        @Param("status") ConversationStatus status,
        Pageable pageable
    );
    
    /**
     * 统计对话数量
     */
    Long countByAgentId(Long agentId);
    
    Long countByUserId(String userId);
    
    Long countByAgentIdAndUserId(Long agentId, String userId);
    
    Long countByStatus(ConversationStatus status);
    
    /**
     * 统计时间范围内的对话
     */
    @Query("SELECT COUNT(c) FROM AgentConversation c WHERE c.agent.id = :agentId " +
           "AND c.createdAt BETWEEN :startTime AND :endTime")
    Long countByAgentIdAndCreatedAtBetween(
        @Param("agentId") Long agentId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}