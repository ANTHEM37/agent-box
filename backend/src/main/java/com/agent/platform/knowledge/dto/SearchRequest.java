package com.agent.platform.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 搜索请求
 */
@Data
public class SearchRequest {

    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeBaseId;

    @NotBlank(message = "查询内容不能为空")
    private String query;

    private Integer topK = 5;

    private Double threshold = 0.7;

    private Boolean includeMetadata = true;
}