package com.agent.platform.knowledge.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 向量存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 向量化并存储文本段
     */
    public String addTextSegment(TextSegment textSegment) {
        try {
            // 生成向量
            Embedding embedding = embeddingModel.embed(textSegment).content();
            
            // 生成唯一ID
            String vectorId = UUID.randomUUID().toString();
            
            // 存储到向量数据库
            embeddingStore.add(vectorId, embedding);
            
            log.debug("成功存储文本段到向量数据库，ID: {}", vectorId);
            return vectorId;
            
        } catch (Exception e) {
            log.error("存储文本段到向量数据库失败", e);
            throw new RuntimeException("向量化存储失败", e);
        }
    }

    /**
     * 批量向量化并存储文本段
     */
    public List<String> addTextSegments(List<TextSegment> textSegments) {
        try {
            // 批量生成向量
            List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content();
            
            // 生成唯一ID列表
            List<String> vectorIds = textSegments.stream()
                .map(segment -> UUID.randomUUID().toString())
                .toList();
            
            // 批量存储到向量数据库
            for (int i = 0; i < textSegments.size(); i++) {
                embeddingStore.add(vectorIds.get(i), embeddings.get(i));
            }
            
            log.info("成功批量存储{}个文本段到向量数据库", textSegments.size());
            return vectorIds;
            
        } catch (Exception e) {
            log.error("批量存储文本段到向量数据库失败", e);
            throw new RuntimeException("批量向量化存储失败", e);
        }
    }

    /**
     * 语义搜索
     */
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore) {
        try {
            // 向量化查询
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            
            // 执行相似度搜索
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding, maxResults, minScore);
            
            log.debug("语义搜索完成，查询: {}, 结果数量: {}", query, matches.size());
            return matches;
            
        } catch (Exception e) {
            log.error("语义搜索失败，查询: {}", query, e);
            throw new RuntimeException("语义搜索失败", e);
        }
    }

    /**
     * 根据向量ID删除 - 暂时不支持删除功能
     */
    public void removeByVectorId(String vectorId) {
        log.warn("向量删除功能暂不支持，ID: {}", vectorId);
        // embeddingStore.remove(vectorId); // LangChain4j EmbeddingStore接口不支持删除操作
    }

    /**
     * 批量删除向量 - 暂时不支持删除功能
     */
    public void removeByVectorIds(List<String> vectorIds) {
        log.warn("批量向量删除功能暂不支持，数量: {}", vectorIds.size());
        // embeddingStore.removeAll(vectorIds); // LangChain4j EmbeddingStore接口不支持删除操作
    }
}