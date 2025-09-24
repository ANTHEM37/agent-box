package com.agent.platform.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置
 */
@Configuration
public class LangChain4jConfig {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.base.url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${chroma.host:localhost}")
    private String chromaHost;

    @Value("${chroma.port:8000}")
    private int chromaPort;

    /**
     * OpenAI Embedding 模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
            .apiKey(openaiApiKey)
            .baseUrl(openaiBaseUrl)
            .modelName("text-embedding-ada-002")
            .timeout(Duration.ofSeconds(60))
            .maxRetries(3)
            .build();
    }

    /**
     * Chroma 向量存储
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
            .baseUrl(String.format("http://%s:%d", chromaHost, chromaPort))
            .collectionName("agent-platform")
            .build();
    }
}