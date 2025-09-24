package com.agent.platform.agent.dto;

import lombok.Data;

/**
 * 记忆统计响应DTO
 */
@Data
public class MemoryStatsResponse {
    private Long agentId;
    private Long totalMemories;
    private Long workingMemories;
    private Long shortTermMemories;
    private Long longTermMemories;
    private Double avgImportance;
    private Long totalAccess;
    private java.util.Map<String, Long> memoriesByType;
    private java.util.Map<String, Long> memoriesByImportance;
}