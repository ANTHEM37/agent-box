package com.agent.platform.agent.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能体响应DTO
 */
@Data
public class AgentResponse {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String type;
    private String status;
    private Long creatorId;
    private String creatorName;
    private String avatarUrl;
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private Boolean memoryEnabled;
    private Integer memoryCapacity;
    private Boolean learningEnabled;
    private Boolean collaborationEnabled;
    private Integer priority;
    private Boolean isPublic;
    private Long usageCount;
    private Double successRate;
    private Long avgResponseTime;
    private LocalDateTime lastActiveAt;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // JSON配置字段
    private Map<String, Object> personalityConfig;
    private List<String> capabilities;
    private List<Long> knowledgeBaseIds;
    private List<String> tools;
    private Map<String, Object> modelConfig;
    private List<String> tags;
}