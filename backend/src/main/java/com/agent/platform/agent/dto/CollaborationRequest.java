package com.agent.platform.agent.dto;

import lombok.Data;
import java.util.List;

/**
 * 协作请求DTO
 */
@Data
public class CollaborationRequest {
    private Long primaryAgentId;
    private Long taskId;
    private String title;
    private String objective;
    private String type;
    private String strategy;
    private List<String> participantAgentIds;
    private String config;
}