package com.agent.platform.agent.dto;

import lombok.Data;

/**
 * 智能体搜索请求DTO
 */
@Data
public class AgentSearchRequest {
    private String keyword;
    private String type;
    private String status;
    private String creatorId;
    private Boolean isPublic;
    private String tag;
}