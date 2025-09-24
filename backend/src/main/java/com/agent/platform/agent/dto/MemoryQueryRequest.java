package com.agent.platform.agent.dto;

import lombok.Data;

/**
 * 记忆查询请求DTO
 */
@Data
public class MemoryQueryRequest {
    private String query;
    private String type;
    private Double minImportance;
    private Integer limit = 10;
}