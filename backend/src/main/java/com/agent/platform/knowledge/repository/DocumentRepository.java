package com.agent.platform.knowledge.repository;

import com.agent.platform.knowledge.entity.Document;
import com.agent.platform.knowledge.entity.KnowledgeBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * 根据知识库查找文档
     */
    Page<Document> findByKnowledgeBaseOrderByCreatedAtDesc(KnowledgeBase knowledgeBase, Pageable pageable);

    /**
     * 根据知识库和状态查找文档
     */
    List<Document> findByKnowledgeBaseAndStatus(KnowledgeBase knowledgeBase, Document.DocumentStatus status);

    /**
     * 根据知识库和文件名查找文档
     */
    Optional<Document> findByKnowledgeBaseAndFilename(KnowledgeBase knowledgeBase, String filename);

    /**
     * 统计知识库的文档数量
     */
    long countByKnowledgeBase(KnowledgeBase knowledgeBase);

    /**
     * 统计知识库中已处理的文档数量
     */
    long countByKnowledgeBaseAndStatus(KnowledgeBase knowledgeBase, Document.DocumentStatus status);

    /**
     * 查找需要处理的文档
     */
    @Query("SELECT d FROM Document d WHERE d.status = 'UPLOADED' ORDER BY d.createdAt ASC")
    List<Document> findPendingDocuments();

    /**
     * 根据文件类型统计文档数量
     */
    @Query("SELECT d.fileType, COUNT(d) FROM Document d WHERE d.knowledgeBase = :kb GROUP BY d.fileType")
    List<Object[]> countDocumentsByFileType(@Param("kb") KnowledgeBase knowledgeBase);

    /**
     * 计算知识库的总token数
     */
    @Query("SELECT COALESCE(SUM(d.tokenCount), 0) FROM Document d WHERE d.knowledgeBase = :kb AND d.status = 'PROCESSED'")
    Long sumTokenCountByKnowledgeBase(@Param("kb") KnowledgeBase knowledgeBase);
}