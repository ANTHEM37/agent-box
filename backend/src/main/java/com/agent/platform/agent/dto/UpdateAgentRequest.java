package com.agent.platform.agent.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 更新智能体请求DTO
 */
@Data
public class UpdateAgentRequest {
    private String displayName;
    private String description;
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
    
    // JSON配置字段
    private Map<String, Object> personalityConfig;
    private List<String> capabilities;
    private List<Long> knowledgeBaseIds;
    private List<String> tools;
    private Map<String, Object> modelConfig;
    private List<String> tags;
}