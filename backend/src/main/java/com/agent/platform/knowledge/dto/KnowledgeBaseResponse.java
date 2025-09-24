package com.agent.platform.knowledge.dto;

import com.agent.platform.knowledge.entity.KnowledgeBase;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库响应
 */
@Data
public class KnowledgeBaseResponse {

    private Long id;
    private String name;
    private String description;
    private String embeddingModel;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private KnowledgeBase.KnowledgeBaseStatus status;
    private Integer documentCount;
    private Long totalTokens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeBaseResponse from(KnowledgeBase knowledgeBase) {
        KnowledgeBaseResponse response = new KnowledgeBaseResponse();
        response.setId(knowledgeBase.getId());
        response.setName(knowledgeBase.getName());
        response.setDescription(knowledgeBase.getDescription());
        response.setEmbeddingModel(knowledgeBase.getEmbeddingModel());
        response.setChunkSize(knowledgeBase.getChunkSize());
        response.setChunkOverlap(knowledgeBase.getChunkOverlap());
        response.setStatus(knowledgeBase.getStatus());
        response.setDocumentCount(knowledgeBase.getDocumentCount());
        response.setTotalTokens(knowledgeBase.getTotalTokens());
        response.setCreatedAt(knowledgeBase.getCreatedAt());
        response.setUpdatedAt(knowledgeBase.getUpdatedAt());
        return response;
    }
}