package com.agent.platform.mcp.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.mcp.dto.ServiceDeploymentRequest;
import com.agent.platform.mcp.dto.ServiceDeploymentResponse;
import com.agent.platform.mcp.service.DeploymentService;
import com.agent.platform.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 部署控制器
 */
@RestController
@RequestMapping("/mcp/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentService deploymentService;

    /**
     * 创建部署
     */
    @PostMapping
    public ApiResponse<ServiceDeploymentResponse> createDeployment(
            @Valid @RequestBody ServiceDeploymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ServiceDeploymentResponse response = deploymentService.createDeployment(request, userDetails.getUser());
        return ApiResponse.success(response);
    }

    /**
     * 获取部署详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ServiceDeploymentResponse> getDeployment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ServiceDeploymentResponse response = deploymentService.getDeployment(id, userDetails.getUser());
        return ApiResponse.success(response);
    }

    /**
     * 获取用户的部署列表
     */
    @GetMapping("/my")
    public ApiResponse<Page<ServiceDeploymentResponse>> getUserDeployments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<ServiceDeploymentResponse> response = deploymentService.getUserDeployments(userDetails.getUser(), pageable);
        return ApiResponse.success(response);
    }

    /**
     * 停止部署
     */
    @PostMapping("/{id}/stop")
    public ApiResponse<Void> stopDeployment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        deploymentService.stopDeployment(id, userDetails.getUser());
        return ApiResponse.success();
    }

    /**
     * 重启部署
     */
    @PostMapping("/{id}/restart")
    public ApiResponse<Void> restartDeployment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        deploymentService.restartDeployment(id, userDetails.getUser());
        return ApiResponse.success();
    }

    /**
     * 删除部署
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeployment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        deploymentService.deleteDeployment(id, userDetails.getUser());
        return ApiResponse.success();
    }

    /**
     * 扩缩容部署
     */
    @PostMapping("/{id}/scale")
    public ApiResponse<Void> scaleDeployment(
            @PathVariable Long id,
            @RequestParam Integer replicas,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        deploymentService.scaleDeployment(id, replicas, userDetails.getUser());
        return ApiResponse.success();
    }

    /**
     * 获取部署统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<DeploymentService.DeploymentStats> getDeploymentStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        DeploymentService.DeploymentStats stats = deploymentService.getDeploymentStats(userDetails.getUser());
        return ApiResponse.success(stats);
    }
}