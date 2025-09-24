package com.agent.platform.agent.dto;

import lombok.Data;

/**
 * 更新记忆请求DTO
 */
@Data
public class UpdateMemoryRequest {
    private String content;
    private String summary;
    private Double importance;
}