package com.agent.platform.workflow.dto;

import com.agent.platform.workflow.entity.Workflow;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class WorkflowCreateRequest {
    
    @NotBlank(message = "工作流名称不能为空")
    private String name;
    
    private String description;
    
    @NotNull(message = "工作流定义不能为空")
    private Workflow.WorkflowDefinition definition;
    
    private String category;
    
    private List<String> tags;
    
    private Boolean isTemplate = false;
}