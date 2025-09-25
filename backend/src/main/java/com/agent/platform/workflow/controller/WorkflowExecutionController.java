package com.agent.platform.workflow.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.security.UserDetailsImpl;
import com.agent.platform.workflow.dto.WorkflowExecutionRequest;
import com.agent.platform.workflow.dto.WorkflowExecutionResponse;
import com.agent.platform.workflow.engine.WorkflowEngine;
import com.agent.platform.workflow.entity.WorkflowExecution;
import com.agent.platform.workflow.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/workflow-executions")
@RequiredArgsConstructor
public class WorkflowExecutionController {
    
    private final WorkflowEngine workflowEngine;
    private final WorkflowExecutionRepository executionRepository;
    
    /**
     * 执行工作流
     */
    @PostMapping("/execute")
    public ApiResponse<WorkflowExecutionResponse> executeWorkflow(
            @Valid @RequestBody WorkflowExecutionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        WorkflowExecution execution = workflowEngine.executeWorkflow(
                request.getWorkflowId(), 
                request.getInputData(), 
                userDetails.getUser().getId()
        );
        
        return ApiResponse.success(WorkflowExecutionResponse.from(execution));
    }
    
    /**
     * 获取执行详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WorkflowExecutionResponse> getExecution(@PathVariable Long id) {
        WorkflowExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("执行记录不存在"));
        
        return ApiResponse.success(WorkflowExecutionResponse.from(execution));
    }
    
    /**
     * 取消执行
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelExecution(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        workflowEngine.cancelExecution(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取用户的执行记录
     */
    @GetMapping
    public ApiResponse<Page<WorkflowExecutionResponse>> getUserExecutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long workflowId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Long userId = userDetails.getUser().getId();
        
        Page<WorkflowExecution> executions;
        
        if (workflowId != null) {
            executions = executionRepository.findByWorkflowIdAndUserIdOrderByStartedAtDesc(
                    workflowId, userId, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            WorkflowExecution.ExecutionStatus executionStatus = 
                    WorkflowExecution.ExecutionStatus.valueOf(status.toUpperCase());
            executions = executionRepository.findByUserIdAndStatusOrderByStartedAtDesc(
                    userId, executionStatus, pageable);
        } else {
            executions = executionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
        }
        
        Page<WorkflowExecutionResponse> response = executions.map(WorkflowExecutionResponse::from);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取工作流的执行记录
     */
    @GetMapping("/workflow/{workflowId}")
    public ApiResponse<Page<WorkflowExecutionResponse>> getWorkflowExecutions(
            @PathVariable Long workflowId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkflowExecution> executions = executionRepository
                .findByWorkflowIdOrderByStartedAtDesc(workflowId, pageable);
        
        Page<WorkflowExecutionResponse> response = executions.map(WorkflowExecutionResponse::from);
        return ApiResponse.success(response);
    }
}