package com.agent.platform.workflow.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
public class WorkflowExecutionRequest {
    
    @NotNull(message = "工作流ID不能为空")
    private Long workflowId;
    
    private Map<String, Object> inputData;
    
    private Boolean async = true; // 是否异步执行
}