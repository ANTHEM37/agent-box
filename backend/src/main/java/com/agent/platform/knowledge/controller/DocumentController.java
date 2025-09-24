package com.agent.platform.knowledge.controller;

import com.agent.platform.common.base.ApiResponse;
import com.agent.platform.knowledge.dto.DocumentUploadResponse;
import com.agent.platform.knowledge.service.DocumentService;
import com.agent.platform.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档控制器
 */
@Tag(name = "文档管理", description = "文档的上传、处理、查询和删除")
@RestController
@RequestMapping("/api/knowledge-bases/{knowledgeBaseId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long knowledgeBaseId,
            @RequestParam("file") MultipartFile file) {
        
        DocumentUploadResponse response = documentService.uploadDocument(
            userDetails.getUser(), knowledgeBaseId, file);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "处理文档")
    @PostMapping("/{documentId}/process")
    public ResponseEntity<ApiResponse<Void>> processDocument(
            @PathVariable Long knowledgeBaseId,
            @PathVariable Long documentId) {
        
        documentService.processDocument(documentId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "获取文档列表")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DocumentUploadResponse>>> getDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long knowledgeBaseId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<DocumentUploadResponse> response = documentService.getDocuments(
            userDetails.getUser(), knowledgeBaseId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long knowledgeBaseId,
            @PathVariable Long documentId) {
        
        documentService.deleteDocument(userDetails.getUser(), documentId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}