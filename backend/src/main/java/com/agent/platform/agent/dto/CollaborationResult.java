package com.agent.platform.agent.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 协作结果DTO
 */
@Data
public class CollaborationResult {
    private Long collaborationId;
    private boolean success;
    private String result;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
}