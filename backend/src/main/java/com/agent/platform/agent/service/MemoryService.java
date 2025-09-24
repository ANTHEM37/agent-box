package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.*;
import com.agent.platform.agent.repository.*;
import com.agent.platform.agent.dto.*;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能体记忆管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryService {
    
    private final AgentMemoryRepository memoryRepository;
    private final AIAgentRepository agentRepository;
    private final EmbeddingModel embeddingModel;
    
    /**
     * 存储记忆
     */
    @Transactional
    public MemoryResponse storeMemory(Long agentId, StoreMemoryRequest request) {
        log.info("Storing memory for agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        if (!agent.getMemoryEnabled()) {
            throw new IllegalStateException("Memory is disabled for agent: " + agentId);
        }
        
        // 检查记忆容量
        Long currentMemoryCount = memoryRepository.countByAgentId(agentId);
        if (currentMemoryCount >= agent.getMemoryCapacity()) {
            // 删除最旧的记忆
            removeOldestMemories(agentId, 1);
        }
        
        AgentMemory memory = new AgentMemory();
        memory.setAgent(agent);
        memory.setType(MemoryType.valueOf(request.getType()));
        memory.setContent(request.getContent());
        memory.setSummary(request.getSummary());
        memory.setImportance(request.getImportance());
        memory.setTaskId(request.getTaskId());
        memory.setConversationId(request.getConversationId());
        memory.setSource(MemorySource.valueOf(request.getSource()));
        
        // 生成向量嵌入
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            try {
                Embedding embedding = embeddingModel.embed(request.getContent()).content();
                memory.setEmbedding(embedding.vectorAsList().toString());
            } catch (Exception e) {
                log.error("Failed to generate embedding for memory", e);
            }
        }
        
        // 设置遗忘时间（基于重要性和记忆类型）
        memory.setForgetAt(calculateForgetTime(memory.getType(), memory.getImportance()));
        
        memory = memoryRepository.save(memory);
        
        log.info("Memory stored successfully: {}", memory.getId());
        return convertToResponse(memory);
    }
    
    /**
     * 检索记忆
     */
    public List<MemoryResponse> retrieveMemory(Long agentId, MemoryQueryRequest request) {
        log.info("Retrieving memory for agent: {}", agentId);
        
        AIAgent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        List<AgentMemory> memories;
        
        if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            // 语义搜索
            memories = semanticSearch(agentId, request.getQuery(), request.getLimit());
        } else {
            // 按类型和重要性检索
            memories = memoryRepository.findByAgentIdAndTypeAndImportanceGreaterThanEqualOrderByImportanceDescCreatedAtDesc(
                agentId, 
                request.getType() != null ? MemoryType.valueOf(request.getType()) : null,
                request.getMinImportance() != null ? request.getMinImportance() : 0.0,
                request.getLimit() != null ? request.getLimit() : 10
            );
        }
        
        // 更新访问统计
        updateAccessStats(memories);
        
        return memories.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 语义搜索记忆
     */
    private List<AgentMemory> semanticSearch(Long agentId, String query, Integer limit) {
        try {
            // 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            String queryVector = queryEmbedding.vectorAsList().toString();
            
            // 使用向量相似性搜索
            return memoryRepository.findSimilarMemories(agentId, queryVector, limit != null ? limit : 10);
        } catch (Exception e) {
            log.error("Failed to perform semantic search", e);
            // 降级到文本搜索
            return memoryRepository.findByAgentIdAndContentContainingIgnoreCaseOrderByImportanceDescCreatedAtDesc(
                agentId, query, limit != null ? limit : 10
            );
        }
    }
    
    /**
     * 更新记忆
     */
    @Transactional
    public MemoryResponse updateMemory(Long memoryId, UpdateMemoryRequest request) {
        log.info("Updating memory: {}", memoryId);
        
        AgentMemory memory = memoryRepository.findById(memoryId)
            .orElseThrow(() -> new IllegalArgumentException("Memory not found: " + memoryId));
        
        if (request.getContent() != null) {
            memory.setContent(request.getContent());
            
            // 重新生成向量嵌入
            try {
                Embedding embedding = embeddingModel.embed(request.getContent()).content();
                memory.setEmbedding(embedding.vectorAsList().toString());
            } catch (Exception e) {
                log.error("Failed to generate embedding for updated memory", e);
            }
        }
        
        if (request.getSummary() != null) {
            memory.setSummary(request.getSummary());
        }
        
        if (request.getImportance() != null) {
            memory.setImportance(request.getImportance());
            // 重新计算遗忘时间
            memory.setForgetAt(calculateForgetTime(memory.getType(), memory.getImportance()));
        }
        
        memory = memoryRepository.save(memory);
        
        log.info("Memory updated successfully: {}", memory.getId());
        return convertToResponse(memory);
    }
    
    /**
     * 删除记忆
     */
    @Transactional
    public void deleteMemory(Long memoryId) {
        log.info("Deleting memory: {}", memoryId);
        
        AgentMemory memory = memoryRepository.findById(memoryId)
            .orElseThrow(() -> new IllegalArgumentException("Memory not found: " + memoryId));
        
        memoryRepository.delete(memory);
        
        log.info("Memory deleted successfully: {}", memoryId);
    }
    
    /**
     * 清理过期记忆
     */
    @Transactional
    public void cleanupExpiredMemories() {
        log.info("Cleaning up expired memories");
        
        LocalDateTime now = LocalDateTime.now();
        List<AgentMemory> expiredMemories = memoryRepository.findByForgetAtBefore(now);
        
        for (AgentMemory memory : expiredMemories) {
            // 根据重要性决定是否真正删除
            if (memory.getImportance() < 0.8) {
                memoryRepository.delete(memory);
                log.debug("Deleted expired memory: {}", memory.getId());
            } else {
                // 重要记忆延长遗忘时间
                memory.setForgetAt(now.plusDays(30));
                memoryRepository.save(memory);
                log.debug("Extended forget time for important memory: {}", memory.getId());
            }
        }
        
        log.info("Cleanup completed, processed {} expired memories", expiredMemories.size());
    }
    
    /**
     * 记忆整合
     */
    @Transactional
    public void consolidateMemories(Long agentId) {
        log.info("Consolidating memories for agent: {}", agentId);
        
        // 获取相似的记忆
        List<AgentMemory> memories = memoryRepository.findByAgentIdOrderByCreatedAtDesc(agentId);
        
        Map<String, List<AgentMemory>> similarGroups = groupSimilarMemories(memories);
        
        for (Map.Entry<String, List<AgentMemory>> entry : similarGroups.entrySet()) {
            List<AgentMemory> group = entry.getValue();
            if (group.size() > 1) {
                // 合并相似记忆
                AgentMemory consolidatedMemory = mergeMemories(group);
                memoryRepository.save(consolidatedMemory);
                
                // 删除原始记忆
                for (AgentMemory memory : group) {
                    if (!memory.getId().equals(consolidatedMemory.getId())) {
                        memoryRepository.delete(memory);
                    }
                }
                
                log.debug("Consolidated {} memories into one", group.size());
            }
        }
        
        log.info("Memory consolidation completed for agent: {}", agentId);
    }
    
    /**
     * 获取记忆统计
     */
    public MemoryStatsResponse getMemoryStats(Long agentId) {
        MemoryStatsResponse stats = new MemoryStatsResponse();
        stats.setAgentId(agentId);
        
        Long totalMemories = memoryRepository.countByAgentId(agentId);
        stats.setTotalMemories(totalMemories);
        
        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        for (MemoryType type : MemoryType.values()) {
            Long count = memoryRepository.countByAgentIdAndType(agentId, type);
            typeStats.put(type.name(), count);
        }
        stats.setMemoriesByType(typeStats);
        
        // 按重要性统计
        Long highImportance = memoryRepository.countByAgentIdAndImportanceGreaterThanEqual(agentId, 0.8);
        Long mediumImportance = memoryRepository.countByAgentIdAndImportanceBetween(agentId, 0.5, 0.8);
        Long lowImportance = memoryRepository.countByAgentIdAndImportanceLessThan(agentId, 0.5);
        
        Map<String, Long> importanceStats = new HashMap<>();
        importanceStats.put("HIGH", highImportance);
        importanceStats.put("MEDIUM", mediumImportance);
        importanceStats.put("LOW", lowImportance);
        stats.setMemoriesByImportance(importanceStats);
        
        return stats;
    }
    
    /**
     * 计算遗忘时间
     */
    private LocalDateTime calculateForgetTime(MemoryType type, Double importance) {
        LocalDateTime now = LocalDateTime.now();
        
        // 基础遗忘时间（天）
        int baseDays = switch (type) {
            case WORKING -> 1;
            case SHORT_TERM -> 7;
            case LONG_TERM -> 365;
            case EPISODIC -> 30;
            case SEMANTIC -> 180;
            case PROCEDURAL -> 90;
            case META -> 30;
        };
        
        // 根据重要性调整
        double importanceMultiplier = Math.max(0.1, importance * 2);
        int adjustedDays = (int) (baseDays * importanceMultiplier);
        
        return now.plusDays(adjustedDays);
    }
    
    /**
     * 删除最旧的记忆
     */
    private void removeOldestMemories(Long agentId, int count) {
        List<AgentMemory> oldestMemories = memoryRepository.findByAgentIdOrderByCreatedAtAsc(agentId);
        
        for (int i = 0; i < Math.min(count, oldestMemories.size()); i++) {
            AgentMemory memory = oldestMemories.get(i);
            if (memory.getImportance() < 0.7) { // 只删除不重要的记忆
                memoryRepository.delete(memory);
                log.debug("Removed oldest memory: {}", memory.getId());
            }
        }
    }
    
    /**
     * 更新访问统计
     */
    private void updateAccessStats(List<AgentMemory> memories) {
        LocalDateTime now = LocalDateTime.now();
        for (AgentMemory memory : memories) {
            memory.setAccessCount(memory.getAccessCount() + 1);
            memory.setLastAccessAt(now);
        }
        memoryRepository.saveAll(memories);
    }
    
    /**
     * 分组相似记忆
     */
    private Map<String, List<AgentMemory>> groupSimilarMemories(List<AgentMemory> memories) {
        // 简化实现，实际应该使用向量相似性
        Map<String, List<AgentMemory>> groups = new HashMap<>();
        
        for (AgentMemory memory : memories) {
            String key = memory.getType().name() + "_" + (memory.getTaskId() != null ? memory.getTaskId() : "general");
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(memory);
        }
        
        return groups;
    }
    
    /**
     * 合并记忆
     */
    private AgentMemory mergeMemories(List<AgentMemory> memories) {
        AgentMemory primary = memories.get(0);
        
        // 合并内容
        StringBuilder mergedContent = new StringBuilder();
        double totalImportance = 0;
        
        for (AgentMemory memory : memories) {
            mergedContent.append(memory.getContent()).append("\n");
            totalImportance += memory.getImportance();
        }
        
        primary.setContent(mergedContent.toString().trim());
        primary.setImportance(totalImportance / memories.size());
        primary.setSummary("Consolidated from " + memories.size() + " memories");
        
        return primary;
    }
    
    /**
     * 转换为响应对象
     */
    private MemoryResponse convertToResponse(AgentMemory memory) {
        MemoryResponse response = new MemoryResponse();
        response.setId(memory.getId());
        response.setAgentId(memory.getAgent().getId());
        response.setType(memory.getType().name());
        response.setContent(memory.getContent());
        response.setSummary(memory.getSummary());
        response.setImportance(memory.getImportance());
        response.setAccessCount(memory.getAccessCount());
        response.setLastAccessAt(memory.getLastAccessAt());
        response.setForgetAt(memory.getForgetAt());
        response.setTaskId(memory.getTaskId());
        response.setConversationId(memory.getConversationId());
        response.setSource(memory.getSource() != null ? memory.getSource().name() : null);
        response.setCreatedAt(memory.getCreatedAt());
        response.setUpdatedAt(memory.getUpdatedAt());
        
        return response;
    }
}