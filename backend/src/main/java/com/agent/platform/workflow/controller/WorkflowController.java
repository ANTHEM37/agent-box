package com.agent.platform.workflow.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.security.UserDetailsImpl;
import com.agent.platform.workflow.dto.*;
import com.agent.platform.workflow.entity.Workflow;
import com.agent.platform.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    /**
     * 创建工作流
     */
    @PostMapping
    public ApiResponse<WorkflowResponse> createWorkflow(
            @Valid @RequestBody WorkflowCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Workflow workflow = new Workflow();
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setDefinition(request.getDefinition());
        workflow.setCategory(request.getCategory());
        workflow.setTags(request.getTags());
        workflow.setIsTemplate(request.getIsTemplate());
        workflow.setUser(userDetails.getUser());
        
        Workflow created = workflowService.create(workflow);
        return ApiResponse.success(WorkflowResponse.from(created));
    }
    
    /**
     * 更新工作流
     */
    @PutMapping("/{id}")
    public ApiResponse<WorkflowResponse> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Workflow workflow = new Workflow();
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setDefinition(request.getDefinition());
        workflow.setCategory(request.getCategory());
        workflow.setTags(request.getTags());
        workflow.setStatus(request.getStatus());
        workflow.setUser(userDetails.getUser());
        
        Workflow updated = workflowService.update(id, workflow);
        return ApiResponse.success(WorkflowResponse.from(updated));
    }
    
    /**
     * 获取工作流详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WorkflowResponse> getWorkflow(@PathVariable Long id) {
        Workflow workflow = workflowService.getById(id);
        return ApiResponse.success(WorkflowResponse.from(workflow));
    }
    
    /**
     * 删除工作流
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkflow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        workflowService.delete(id, userDetails.getUser().getId());
        return ApiResponse.success();
    }
    
    /**
     * 获取用户工作流列表
     */
    @GetMapping
    public ApiResponse<Page<WorkflowResponse>> getUserWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Long userId = userDetails.getUser().getId();
        
        Page<Workflow> workflows;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            workflows = workflowService.searchWorkflows(userId, keyword, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            Workflow.WorkflowStatus workflowStatus = Workflow.WorkflowStatus.valueOf(status.toUpperCase());
            workflows = workflowService.getUserWorkflowsByStatus(userId, workflowStatus, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            workflows = workflowService.getUserWorkflowsByCategory(userId, category, pageable);
        } else if (tag != null && !tag.trim().isEmpty()) {
            workflows = workflowService.getUserWorkflowsByTag(userId, tag, pageable);
        } else {
            workflows = workflowService.getUserWorkflows(userId, pageable);
        }
        
        Page<WorkflowResponse> response = workflows.map(WorkflowResponse::from);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取工作流模板
     */
    @GetMapping("/templates")
    public ApiResponse<Page<WorkflowResponse>> getTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Workflow> templates;
        if (category != null && !category.trim().isEmpty()) {
            templates = workflowService.getTemplatesByCategory(category, pageable);
        } else {
            templates = workflowService.getTemplates(pageable);
        }
        
        Page<WorkflowResponse> response = templates.map(WorkflowResponse::from);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        List<String> categories = workflowService.getAllCategories();
        return ApiResponse.success(categories);
    }
    
    /**
     * 获取用户的分类
     */
    @GetMapping("/categories/mine")
    public ApiResponse<List<String>> getUserCategories(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<String> categories = workflowService.getUserCategories(userDetails.getUser().getId());
        return ApiResponse.success(categories);
    }
    
    /**
     * 获取用户工作流统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getUserWorkflowStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Map<String, Long> stats = workflowService.getUserWorkflowStats(userDetails.getUser().getId());
        return ApiResponse.success(stats);
    }
    
    /**
     * 发布工作流
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<WorkflowResponse> publishWorkflow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Workflow workflow = workflowService.publish(id, userDetails.getUser().getId());
        return ApiResponse.success(WorkflowResponse.from(workflow));
    }
    
    /**
     * 归档工作流
     */
    @PostMapping("/{id}/archive")
    public ApiResponse<WorkflowResponse> archiveWorkflow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Workflow workflow = workflowService.archive(id, userDetails.getUser().getId());
        return ApiResponse.success(WorkflowResponse.from(workflow));
    }
    
    /**
     * 复制工作流
     */
    @PostMapping("/{id}/copy")
    public ApiResponse<WorkflowResponse> copyWorkflow(
            @PathVariable Long id,
            @RequestParam String newName,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Workflow workflow = workflowService.copy(id, userDetails.getUser().getId(), newName);
        return ApiResponse.success(WorkflowResponse.from(workflow));
    }
}