package com.agent.platform.knowledge.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_base_id", nullable = false)
    private KnowledgeBase knowledgeBase;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(name = "token_count")
    private Integer tokenCount = 0;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @Column(columnDefinition = "TEXT")
    private String summary;

    public enum DocumentStatus {
        UPLOADED, PROCESSING, PROCESSED, ERROR, DELETED
    }
}