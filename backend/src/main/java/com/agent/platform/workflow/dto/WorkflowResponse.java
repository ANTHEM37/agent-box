package com.agent.platform.workflow.dto;

import com.agent.platform.workflow.entity.Workflow;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkflowResponse {
    
    private Long id;
    private String name;
    private String description;
    private Workflow.WorkflowDefinition definition;
    private Integer version;
    private Workflow.WorkflowStatus status;
    private String category;
    private List<String> tags;
    private Boolean isTemplate;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private Long executionCount;
    private Long successCount;
    private Double successRate;
    private Long avgExecutionTime;
    
    public static WorkflowResponse from(Workflow workflow) {
        WorkflowResponse response = new WorkflowResponse();
        response.setId(workflow.getId());
        response.setName(workflow.getName());
        response.setDescription(workflow.getDescription());
        response.setDefinition(workflow.getDefinition());
        response.setVersion(workflow.getVersion());
        response.setStatus(workflow.getStatus());
        response.setCategory(workflow.getCategory());
        response.setTags(workflow.getTags());
        response.setIsTemplate(workflow.getIsTemplate());
        response.setCreatedAt(workflow.getCreatedAt());
        response.setUpdatedAt(workflow.getUpdatedAt());
        
        if (workflow.getUser() != null) {
            response.setUserId(workflow.getUser().getId());
            response.setUserName(workflow.getUser().getUsername());
        }
        
        return response;
    }
}