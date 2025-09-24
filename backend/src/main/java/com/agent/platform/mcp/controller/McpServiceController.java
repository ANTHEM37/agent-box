package com.agent.platform.mcp.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.mcp.dto.McpServiceCreateRequest;
import com.agent.platform.mcp.dto.McpServiceResponse;
import com.agent.platform.mcp.service.McpServiceService;
import com.agent.platform.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MCP 服务控制器
 */
@RestController
@RequestMapping("/api/mcp/services")
@RequiredArgsConstructor
public class McpServiceController {

    private final McpServiceService mcpServiceService;

    /**
     * 创建 MCP 服务
     */
    @PostMapping
    public ApiResponse<McpServiceResponse> createService(
            @Valid @RequestBody McpServiceCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        McpServiceResponse response = mcpServiceService.createService(request, userDetails.getUser());
        return ApiResponse.success(response);
    }

    /**
     * 获取服务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<McpServiceResponse> getService(@PathVariable Long id) {
        McpServiceResponse response = mcpServiceService.getService(id);
        return ApiResponse.success(response);
    }

    /**
     * 更新服务
     */
    @PutMapping("/{id}")
    public ApiResponse<McpServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody McpServiceCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        McpServiceResponse response = mcpServiceService.updateService(id, request, userDetails.getUser());
        return ApiResponse.success(response);
    }

    /**
     * 发布服务
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishService(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        mcpServiceService.publishService(id, userDetails.getUser());
        return ApiResponse.success();
    }

    /**
     * 删除服务
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteService(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        mcpServiceService.deleteService(id, userDetails.getUser());
        return ApiResponse.success();
    }

    /**
     * 获取已发布的服务列表
     */
    @GetMapping("/published")
    public ApiResponse<Page<McpServiceResponse>> getPublishedServices(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<McpServiceResponse> response = mcpServiceService.getPublishedServices(pageable);
        return ApiResponse.success(response);
    }

    /**
     * 搜索服务
     */
    @GetMapping("/search")
    public ApiResponse<Page<McpServiceResponse>> searchServices(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<McpServiceResponse> response = mcpServiceService.searchServices(keyword, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 根据分类获取服务
     */
    @GetMapping("/category/{category}")
    public ApiResponse<Page<McpServiceResponse>> getServicesByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<McpServiceResponse> response = mcpServiceService.getServicesByCategory(category, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 根据标签获取服务
     */
    @GetMapping("/tag/{tag}")
    public ApiResponse<Page<McpServiceResponse>> getServicesByTag(
            @PathVariable String tag,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<McpServiceResponse> response = mcpServiceService.getServicesByTag(tag, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 获取精选服务
     */
    @GetMapping("/featured")
    public ApiResponse<List<McpServiceResponse>> getFeaturedServices() {
        List<McpServiceResponse> response = mcpServiceService.getFeaturedServices();
        return ApiResponse.success(response);
    }

    /**
     * 获取热门服务
     */
    @GetMapping("/popular")
    public ApiResponse<Page<McpServiceResponse>> getPopularServices(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<McpServiceResponse> response = mcpServiceService.getPopularServices(pageable);
        return ApiResponse.success(response);
    }

    /**
     * 获取高评分服务
     */
    @GetMapping("/high-rated")
    public ApiResponse<Page<McpServiceResponse>> getHighRatedServices(
            @RequestParam(defaultValue = "4.0") Double minRating,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<McpServiceResponse> response = mcpServiceService.getHighRatedServices(minRating, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 获取用户的服务
     */
    @GetMapping("/my")
    public ApiResponse<List<McpServiceResponse>> getUserServices(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<McpServiceResponse> response = mcpServiceService.getUserServices(userDetails.getUser().getId());
        return ApiResponse.success(response);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        List<String> response = mcpServiceService.getAllCategories();
        return ApiResponse.success(response);
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ApiResponse<List<String>> getAllTags() {
        List<String> response = mcpServiceService.getAllTags();
        return ApiResponse.success(response);
    }

    /**
     * 增加下载次数
     */
    @PostMapping("/{id}/download")
    public ApiResponse<Void> incrementDownloads(@PathVariable Long id) {
        mcpServiceService.incrementDownloads(id);
        return ApiResponse.success();
    }
}