package com.agent.platform.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 搜索结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private String content;
    private Double score;
    private String documentName;
    private Integer chunkIndex;
    private Map<String, Object> metadata;

    public SearchResult(String content, Double score) {
        this.content = content;
        this.score = score;
    }
}