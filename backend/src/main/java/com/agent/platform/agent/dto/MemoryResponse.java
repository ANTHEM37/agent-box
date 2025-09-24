package com.agent.platform.agent.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 记忆响应DTO
 */
@Data
public class MemoryResponse {
    private Long id;
    private Long agentId;
    private String type;
    private String content;
    private String summary;
    private Double importance;
    private Integer accessCount;
    private LocalDateTime lastAccessAt;
    private LocalDateTime forgetAt;
    private Long taskId;
    private Long conversationId;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}