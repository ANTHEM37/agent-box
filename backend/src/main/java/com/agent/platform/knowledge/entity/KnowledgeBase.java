package com.agent.platform.knowledge.entity;

import com.agent.platform.common.base.BaseEntity;
import com.agent.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "knowledge_bases")
public class KnowledgeBase extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "embedding_model", length = 50)
    private String embeddingModel = "text-embedding-ada-002";

    @Column(name = "chunk_size")
    private Integer chunkSize = 500;

    @Column(name = "chunk_overlap")
    private Integer chunkOverlap = 50;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private KnowledgeBaseStatus status = KnowledgeBaseStatus.ACTIVE;

    @Column(name = "document_count")
    private Integer documentCount = 0;

    @Column(name = "total_tokens")
    private Long totalTokens = 0L;

    public enum KnowledgeBaseStatus {
        ACTIVE, INACTIVE, PROCESSING, ERROR
    }
}