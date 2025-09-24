package com.agent.platform.workflow.dto;

import com.agent.platform.workflow.entity.WorkflowExecution;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class WorkflowExecutionResponse {
    
    private Long id;
    private Long workflowId;
    private String workflowName;
    private Integer workflowVersion;
    private WorkflowExecution.ExecutionStatus status;
    private Map<String, Object> inputData;
    private Map<String, Object> outputData;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMs;
    private String errorMessage;
    private Long userId;
    
    // 节点执行统计
    private Integer totalNodes;
    private Integer completedNodes;
    private Integer failedNodes;
    private Double progress;
    
    public static WorkflowExecutionResponse from(WorkflowExecution execution) {
        WorkflowExecutionResponse response = new WorkflowExecutionResponse();
        response.setId(execution.getId());
        response.setWorkflowVersion(execution.getWorkflowVersion());
        response.setStatus(execution.getStatus());
        response.setInputData(execution.getInputData());
        response.setOutputData(execution.getOutputData());
        response.setStartedAt(execution.getStartedAt());
        response.setCompletedAt(execution.getCompletedAt());
        response.setDurationMs(execution.getDurationMs());
        response.setErrorMessage(execution.getErrorMessage());
        
        if (execution.getWorkflow() != null) {
            response.setWorkflowId(execution.getWorkflow().getId());
            response.setWorkflowName(execution.getWorkflow().getName());
        }
        
        if (execution.getUser() != null) {
            response.setUserId(execution.getUser().getId());
        }
        
        return response;
    }
}