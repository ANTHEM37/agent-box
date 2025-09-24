package com.agent.platform.agent.dto;

import lombok.Data;

/**
 * 智能体统计响应DTO
 */
@Data
public class AgentStatsResponse {
    private Long agentId;
    private Long usageCount;
    private Double successRate;
    private Long avgResponseTime;
    private Long totalTasks;
    private Long completedTasks;
    private Long failedTasks;
    private Long totalMemories;
    private Long totalConversations;
}