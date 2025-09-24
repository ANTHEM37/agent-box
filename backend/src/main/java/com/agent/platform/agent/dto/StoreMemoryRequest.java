package com.agent.platform.agent.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 存储记忆请求DTO
 */
@Data
public class StoreMemoryRequest {
    @NotBlank(message = "Memory type is required")
    private String type;
    
    @NotBlank(message = "Memory content is required")
    private String content;
    
    private String summary;
    
    @NotNull(message = "Importance is required")
    private Double importance;
    
    private Long taskId;
    private Long conversationId;
    
    @NotBlank(message = "Memory source is required")
    private String source;
}