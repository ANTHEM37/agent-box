package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.AgentMemory;
import com.agent.platform.agent.entity.MemoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体记忆仓库接口
 */
@Repository
public interface AgentMemoryRepository extends JpaRepository<AgentMemory, Long> {
    
    /**
     * 根据智能体ID查找记忆
     */
    List<AgentMemory> findByAgentIdOrderByCreatedAtDesc(Long agentId);
    
    /**
     * 根据智能体ID和类型查找记忆
     */
    List<AgentMemory> findByAgentIdAndTypeOrderByImportanceDescCreatedAtDesc(Long agentId, MemoryType type);
    
    /**
     * 根据智能体ID、类型和重要性查找记忆
     */
    @Query("SELECT m FROM AgentMemory m WHERE m.agent.id = :agentId " +
           "AND (:type IS NULL OR m.type = :type) " +
           "AND m.importance >= :minImportance " +
           "ORDER BY m.importance DESC, m.createdAt DESC")
    List<AgentMemory> findByAgentIdAndTypeAndImportanceGreaterThanEqualOrderByImportanceDescCreatedAtDesc(
        @Param("agentId") Long agentId,
        @Param("type") MemoryType type,
        @Param("minImportance") Double minImportance,
        @Param("limit") Integer limit
    );
    
    /**
     * 文本搜索记忆
     */
    @Query("SELECT m FROM AgentMemory m WHERE m.agent.id = :agentId " +
           "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY m.importance DESC, m.createdAt DESC")
    List<AgentMemory> findByAgentIdAndContentContainingIgnoreCaseOrderByImportanceDescCreatedAtDesc(
        @Param("agentId") Long agentId,
        @Param("query") String query,
        @Param("limit") Integer limit
    );
    
    /**
     * 向量相似性搜索记忆
     */
    @Query(value = "SELECT * FROM agent_memories m WHERE m.agent_id = :agentId " +
                   "AND m.embedding IS NOT NULL " +
                   "ORDER BY m.embedding <-> CAST(:queryVector AS vector) " +
                   "LIMIT :limit", nativeQuery = true)
    List<AgentMemory> findSimilarMemories(
        @Param("agentId") Long agentId,
        @Param("queryVector") String queryVector,
        @Param("limit") Integer limit
    );
    
    /**
     * 查找过期记忆
     */
    List<AgentMemory> findByForgetAtBefore(LocalDateTime dateTime);
    
    /**
     * 查找最旧的记忆
     */
    List<AgentMemory> findByAgentIdOrderByCreatedAtAsc(Long agentId);
    
    /**
     * 统计智能体记忆数量
     */
    Long countByAgentId(Long agentId);
    
    /**
     * 根据类型统计记忆数量
     */
    Long countByAgentIdAndType(Long agentId, MemoryType type);
    
    /**
     * 根据重要性统计记忆数量
     */
    Long countByAgentIdAndImportanceGreaterThanEqual(Long agentId, Double importance);
    
    Long countByAgentIdAndImportanceBetween(Long agentId, Double minImportance, Double maxImportance);
    
    Long countByAgentIdAndImportanceLessThan(Long agentId, Double importance);
    
    /**
     * 根据任务ID查找记忆
     */
    List<AgentMemory> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    /**
     * 根据对话ID查找记忆
     */
    List<AgentMemory> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
}