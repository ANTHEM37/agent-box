package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.knowledge.dto.SearchRequest;
import com.agent.platform.knowledge.dto.SearchResult;
import com.agent.platform.knowledge.service.SearchService;
import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库检索节点执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeRetrievalNodeExecutor implements NodeExecutor {

    private final SearchService searchService;

    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行知识库检索节点: nodeId={}", context.getNodeId());

        Map<String, Object> output = new HashMap<>();

        // 获取配置
        Long knowledgeBaseId = ((Number) context.getConfig("knowledgeBaseId")).longValue();
        String query = context.getStringConfig("query", "");
        Integer topK = context.getIntegerConfig("topK", 5);
        Double threshold = ((Number) context.getConfig("threshold", 0.7)).doubleValue();
        Boolean includeMetadata = context.getBooleanConfig("includeMetadata", true);
        String searchType = context.getStringConfig("searchType", "semantic");

        // 解析变量
        String resolvedQuery = context.resolveVariables(query);

        if (resolvedQuery.isEmpty()) {
            throw new RuntimeException("检索查询不能为空");
        }

        if (knowledgeBaseId == null) {
            throw new RuntimeException("知识库ID不能为空");
        }

        try {
            // 构建搜索请求
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setKnowledgeBaseId(knowledgeBaseId);
            searchRequest.setQuery(resolvedQuery);
            searchRequest.setTopK(topK);
            searchRequest.setThreshold(threshold);
            searchRequest.setIncludeMetadata(includeMetadata);

            // 执行搜索
            List<SearchResult> results;
            if ("hybrid".equals(searchType)) {
                results = searchService.hybridSearch(null, searchRequest);
            } else {
                results = searchService.search(null, searchRequest);
            }

            // 设置输出
            output.put("results", results);
            output.put("query", resolvedQuery);
            output.put("resultCount", results.size());
            output.put("knowledgeBaseId", knowledgeBaseId);
            output.put("searchType", searchType);

            // 提取文本内容用于后续处理
            StringBuilder contextText = new StringBuilder();
            for (SearchResult result : results) {
                if (contextText.length() > 0) {
                    contextText.append("\n\n");
                }
                contextText.append(result.getContent());
            }
            output.put("contextText", contextText.toString());

            log.info("知识库检索节点执行完成: nodeId={}, resultCount={}", context.getNodeId(), results.size());

        } catch (Exception e) {
            log.error("知识库检索失败: nodeId={}, error={}", context.getNodeId(), e.getMessage(), e);
            throw new RuntimeException("知识库检索失败: " + e.getMessage(), e);
        }

        return output;
    }

    @Override
    public String getNodeType() {
        return "knowledge_retrieval";
    }

    @Override
    public String getDisplayName() {
        return "知识库检索";
    }

    @Override
    public String getDescription() {
        return "从知识库中检索相关文档片段，支持语义搜索和混合搜索";
    }

    @Override
    public String validateConfig(Map<String, Object> config) {
        Object knowledgeBaseId = config.get("knowledgeBaseId");
        if (knowledgeBaseId == null) {
            return "知识库ID不能为空";
        }

        String query = (String) config.get("query");
        if (query == null || query.trim().isEmpty()) {
            return "检索查询不能为空";
        }

        Object topK = config.get("topK");
        if (topK != null) {
            int k = ((Number) topK).intValue();
            if (k < 1 || k > 20) {
                return "返回结果数量必须在1-20之间";
            }
        }

        Object threshold = config.get("threshold");
        if (threshold != null) {
            double t = ((Number) threshold).doubleValue();
            if (t < 0 || t > 1) {
                return "相似度阈值必须在0-1之间";
            }
        }

        return null;
    }

    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        // 知识库ID配置
        Map<String, Object> kbIdConfig = new HashMap<>();
        kbIdConfig.put("type", "integer");
        kbIdConfig.put("title", "知识库ID");
        kbIdConfig.put("description", "要检索的知识库ID");
        properties.put("knowledgeBaseId", kbIdConfig);

        // 查询配置
        Map<String, Object> queryConfig = new HashMap<>();
        queryConfig.put("type", "string");
        queryConfig.put("title", "检索查询");
        queryConfig.put("description", "检索的查询文本，支持变量替换 ${variableName}");
        properties.put("query", queryConfig);

        // 返回结果数量配置
        Map<String, Object> topKConfig = new HashMap<>();
        topKConfig.put("type", "integer");
        topKConfig.put("title", "返回结果数量");
        topKConfig.put("description", "返回最相关的结果数量");
        topKConfig.put("minimum", 1);
        topKConfig.put("maximum", 20);
        topKConfig.put("default", 5);
        properties.put("topK", topKConfig);

        // 相似度阈值配置
        Map<String, Object> thresholdConfig = new HashMap<>();
        thresholdConfig.put("type", "number");
        thresholdConfig.put("title", "相似度阈值");
        thresholdConfig.put("description", "过滤结果的最小相似度");
        thresholdConfig.put("minimum", 0);
        thresholdConfig.put("maximum", 1);
        thresholdConfig.put("default", 0.7);
        properties.put("threshold", thresholdConfig);

        // 搜索类型配置
        Map<String, Object> searchTypeConfig = new HashMap<>();
        searchTypeConfig.put("type", "string");
        searchTypeConfig.put("title", "搜索类型");
        searchTypeConfig.put("enum", new String[]{"semantic", "hybrid"});
        searchTypeConfig.put("default", "semantic");
        properties.put("searchType", searchTypeConfig);

        // 包含元数据配置
        Map<String, Object> metadataConfig = new HashMap<>();
        metadataConfig.put("type", "boolean");
        metadataConfig.put("title", "包含元数据");
        metadataConfig.put("description", "是否在结果中包含文档元数据");
        metadataConfig.put("default", true);
        properties.put("includeMetadata", metadataConfig);

        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[]{"knowledgeBaseId", "query"});
        return schema;
    }
}