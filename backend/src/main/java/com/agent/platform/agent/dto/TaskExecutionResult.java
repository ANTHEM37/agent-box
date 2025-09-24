package com.agent.platform.agent.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务执行结果DTO
 */
@Data
public class TaskExecutionResult {
    private Long taskId;
    private Long agentId;
    private boolean success;
    private String output;
    private String rawResponse;
    private String errorMessage;
    private Long responseTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long collaborationId;
}