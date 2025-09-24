package com.agent.platform.agent.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 创建智能体请求DTO
 */
@Data
public class CreateAgentRequest {
    @NotBlank(message = "Agent name is required")
    private String name;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String description;
    
    @NotBlank(message = "Agent type is required")
    private String type;
    
    @NotBlank(message = "Creator ID is required")
    private String creatorId;
    
    private String creatorName;
    private String avatarUrl;
    private String systemPrompt;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private Boolean memoryEnabled = true;
    private Integer memoryCapacity = 1000;
    private Boolean learningEnabled = false;
    private Boolean collaborationEnabled = false;
    private Integer priority = 5;
    private Boolean isPublic = false;
    
    // JSON配置字段
    private Map<String, Object> personalityConfig;
    private List<String> capabilities;
    private List<Long> knowledgeBaseIds;
    private List<String> tools;
    private Map<String, Object> modelConfig;
    private List<String> tags;
}