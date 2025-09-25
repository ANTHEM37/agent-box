package com.agent.platform.knowledge.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.knowledge.dto.KnowledgeBaseCreateRequest;
import com.agent.platform.knowledge.dto.KnowledgeBaseResponse;
import com.agent.platform.knowledge.entity.KnowledgeBase;
import com.agent.platform.knowledge.service.KnowledgeBaseService;
import com.agent.platform.user.entity.User;
import com.agent.platform.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库管理控制器
 */
@Tag(name = "知识库管理", description = "知识库的创建、查询、更新和删除")
@RestController
@RequestMapping("/knowledge-bases")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "创建知识库")
    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> createKnowledgeBase(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody KnowledgeBaseCreateRequest request) {
        
        KnowledgeBaseResponse response = knowledgeBaseService.createKnowledgeBase(
            userDetails.getUser(), request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "获取知识库列表")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<KnowledgeBaseResponse>>> getKnowledgeBases(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<KnowledgeBaseResponse> response = knowledgeBaseService.getUserKnowledgeBases(
            userDetails.getUser(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "获取知识库详情")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> getKnowledgeBase(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        KnowledgeBaseResponse response = knowledgeBaseService.getKnowledgeBase(
            userDetails.getUser(), id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> updateKnowledgeBase(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeBaseCreateRequest request) {
        
        KnowledgeBaseResponse response = knowledgeBaseService.updateKnowledgeBase(
            userDetails.getUser(), id, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKnowledgeBase(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        knowledgeBaseService.deleteKnowledgeBase(userDetails.getUser(), id);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "搜索知识库")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseResponse>>> searchKnowledgeBases(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String keyword) {
        
        List<KnowledgeBaseResponse> response = knowledgeBaseService.searchKnowledgeBases(
            userDetails.getUser(), keyword);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "获取活跃知识库")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseResponse>>> getActiveKnowledgeBases(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<KnowledgeBaseResponse> response = knowledgeBaseService.getActiveKnowledgeBases(
            userDetails.getUser());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}