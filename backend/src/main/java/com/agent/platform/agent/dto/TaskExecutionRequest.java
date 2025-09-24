package com.agent.platform.agent.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 任务执行请求DTO
 */
@Data
public class TaskExecutionRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Task type is required")
    private String type;
    
    private Integer priority = 5;
    private String creatorId;
    private Boolean requiresCollaboration = false;
    private String userMessage;
    
    // 任务输入数据
    private Map<String, Object> input;
}