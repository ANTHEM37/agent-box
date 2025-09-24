package com.agent.platform.knowledge.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.knowledge.dto.SearchRequest;
import com.agent.platform.knowledge.dto.SearchResult;
import com.agent.platform.knowledge.service.SearchService;
import com.agent.platform.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@Tag(name = "知识库搜索", description = "语义搜索和RAG相关功能")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "语义搜索")
    @PostMapping("/semantic")
    public ResponseEntity<ApiResponse<List<SearchResult>>> semanticSearch(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SearchRequest request) {
        
        List<SearchResult> results = searchService.search(userDetails.getUser(), request);
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "混合搜索")
    @PostMapping("/hybrid")
    public ResponseEntity<ApiResponse<List<SearchResult>>> hybridSearch(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SearchRequest request) {
        
        List<SearchResult> results = searchService.hybridSearch(userDetails.getUser(), request);
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "获取RAG上下文")
    @GetMapping("/context")
    public ResponseEntity<ApiResponse<String>> getContextForRAG(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long knowledgeBaseId,
            @RequestParam String query,
            @RequestParam(defaultValue = "2000") int maxTokens) {
        
        String context = searchService.getContextForRAG(
            userDetails.getUser(), knowledgeBaseId, query, maxTokens);
        
        return ResponseEntity.ok(ApiResponse.success(context));
    }
}