package com.agent.platform.knowledge.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档分块实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "document_chunks")
public class DocumentChunk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "start_position")
    private Integer startPosition;

    @Column(name = "end_position")
    private Integer endPosition;

    @Column(name = "vector_id", length = 100)
    private String vectorId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    // 索引
    @Table(indexes = {
        @Index(name = "idx_document_chunks_document_id", columnList = "document_id"),
        @Index(name = "idx_document_chunks_vector_id", columnList = "vector_id")
    })
    public static class Indexes {}
}