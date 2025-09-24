package com.agent.platform.knowledge.repository;

import com.agent.platform.knowledge.entity.Document;
import com.agent.platform.knowledge.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    /**
     * 根据文档查找所有分块
     */
    List<DocumentChunk> findByDocumentOrderByChunkIndex(Document document);

    /**
     * 根据文档和分块索引查找分块
     */
    Optional<DocumentChunk> findByDocumentAndChunkIndex(Document document, Integer chunkIndex);

    /**
     * 根据向量ID查找分块
     */
    Optional<DocumentChunk> findByVectorId(String vectorId);

    /**
     * 统计文档的分块数量
     */
    long countByDocument(Document document);

    /**
     * 删除文档的所有分块
     */
    void deleteByDocument(Document document);

    /**
     * 查找文档的分块内容
     */
    @Query("SELECT dc.content FROM DocumentChunk dc WHERE dc.document = :document ORDER BY dc.chunkIndex")
    List<String> findContentByDocument(@Param("document") Document document);

    /**
     * 根据向量ID批量查找分块
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.vectorId IN :vectorIds")
    List<DocumentChunk> findByVectorIdIn(@Param("vectorIds") List<String> vectorIds);

    /**
     * 计算文档的总token数
     */
    @Query("SELECT COALESCE(SUM(dc.tokenCount), 0) FROM DocumentChunk dc WHERE dc.document = :document")
    Integer sumTokenCountByDocument(@Param("document") Document document);
}