package com.agent.platform.knowledge.dto;

import com.agent.platform.knowledge.entity.Document;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档上传响应
 */
@Data
public class DocumentUploadResponse {

    private Long id;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    private Document.DocumentStatus status;
    private LocalDateTime createdAt;

    public static DocumentUploadResponse from(Document document) {
        DocumentUploadResponse response = new DocumentUploadResponse();
        response.setId(document.getId());
        response.setFilename(document.getFilename());
        response.setOriginalFilename(document.getOriginalFilename());
        response.setFileSize(document.getFileSize());
        response.setFileType(document.getFileType());
        response.setMimeType(document.getMimeType());
        response.setStatus(document.getStatus());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }
}