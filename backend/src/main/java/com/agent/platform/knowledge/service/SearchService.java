package com.agent.platform.knowledge.service;

import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.knowledge.dto.SearchRequest;
import com.agent.platform.knowledge.dto.SearchResult;
import com.agent.platform.knowledge.entity.DocumentChunk;
import com.agent.platform.knowledge.entity.KnowledgeBase;
import com.agent.platform.knowledge.repository.DocumentChunkRepository;
import com.agent.platform.user.entity.User;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final VectorStoreService vectorStoreService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentChunkRepository documentChunkRepository;

    /**
     * 语义搜索
     */
    public List<SearchResult> search(User user, SearchRequest request) {
        // 验证知识库权限
        KnowledgeBase knowledgeBase = knowledgeBaseService.findKnowledgeBaseByUserAndId(
            user, request.getKnowledgeBaseId());

        try {
            // 执行向量搜索
            List<EmbeddingMatch<TextSegment>> matches = vectorStoreService.search(
                request.getQuery(), request.getTopK(), request.getThreshold());

            // 转换搜索结果
            List<SearchResult> results = new ArrayList<>();
            
            for (EmbeddingMatch<TextSegment> match : matches) {
                TextSegment segment = match.embedded();
                
                // 根据向量ID查找文档分块信息
                DocumentChunk chunk = documentChunkRepository.findByVectorId(match.embeddingId())
                    .orElse(null);
                
                SearchResult result = new SearchResult();
                result.setContent(segment.text());
                result.setScore(match.score());
                
                if (chunk != null) {
                    result.setDocumentName(chunk.getDocument().getOriginalFilename());
                    result.setChunkIndex(chunk.getChunkIndex());
                    
                    if (request.getIncludeMetadata()) {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("documentId", chunk.getDocument().getId());
                        metadata.put("chunkId", chunk.getId());
                        metadata.put("tokenCount", chunk.getTokenCount());
                        metadata.put("fileType", chunk.getDocument().getFileType());
                        result.setMetadata(metadata);
                    }
                }
                
                results.add(result);
            }

            log.info("用户 {} 在知识库 {} 中搜索: {}, 返回 {} 个结果", 
                user.getUsername(), knowledgeBase.getName(), request.getQuery(), results.size());

            return results;

        } catch (Exception e) {
            log.error("搜索失败，用户: {}, 知识库: {}, 查询: {}", 
                user.getUsername(), knowledgeBase.getName(), request.getQuery(), e);
            throw new BusinessException("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 混合搜索（向量搜索 + 关键词搜索）
     */
    public List<SearchResult> hybridSearch(User user, SearchRequest request) {
        // 先执行语义搜索
        List<SearchResult> semanticResults = search(user, request);

        // TODO: 实现关键词搜索并合并结果
        // 这里可以添加基于Elasticsearch的关键词搜索
        
        return semanticResults;
    }

    /**
     * 获取相关文档片段用于RAG
     */
    public String getContextForRAG(User user, Long knowledgeBaseId, String query, int maxTokens) {
        SearchRequest request = new SearchRequest();
        request.setKnowledgeBaseId(knowledgeBaseId);
        request.setQuery(query);
        request.setTopK(10);
        request.setThreshold(0.5);
        request.setIncludeMetadata(false);

        List<SearchResult> results = search(user, request);

        StringBuilder context = new StringBuilder();
        int currentTokens = 0;

        for (SearchResult result : results) {
            String content = result.getContent();
            int contentTokens = estimateTokenCount(content);
            
            if (currentTokens + contentTokens > maxTokens) {
                break;
            }
            
            context.append(content).append("\n\n");
            currentTokens += contentTokens;
        }

        return context.toString().trim();
    }

    /**
     * 估算token数量
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 简单估算：平均每个token约4个字符
        return text.length() / 4;
    }
}