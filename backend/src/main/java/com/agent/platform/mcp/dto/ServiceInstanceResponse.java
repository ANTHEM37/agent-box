package com.agent.platform.mcp.dto;

import com.agent.platform.mcp.entity.ServiceInstance;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务实例响应
 */
@Data
public class ServiceInstanceResponse {

    private Long id;
    private Long deploymentId;
    private String containerId;
    private String host;
    private Integer port;
    private ServiceInstance.InstanceStatus status;
    private String healthCheckUrl;
    private LocalDateTime lastHealthCheck;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private Long requestCount;
    private Long errorCount;
    private Long avgResponseTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应对象
     */
    public static ServiceInstanceResponse fromEntity(ServiceInstance instance) {
        ServiceInstanceResponse response = new ServiceInstanceResponse();
        response.setId(instance.getId());
        response.setDeploymentId(instance.getDeployment().getId());
        response.setContainerId(instance.getContainerId());
        response.setHost(instance.getHost());
        response.setPort(instance.getPort());
        response.setStatus(instance.getStatus());
        response.setHealthCheckUrl(instance.getHealthCheckUrl());
        response.setLastHealthCheck(instance.getLastHealthCheck());
        response.setCpuUsage(instance.getCpuUsage());
        response.setMemoryUsage(instance.getMemoryUsage());
        response.setRequestCount(instance.getRequestCount());
        response.setErrorCount(instance.getErrorCount());
        response.setAvgResponseTime(instance.getAvgResponseTime());
        response.setCreatedAt(instance.getCreatedAt());
        response.setUpdatedAt(instance.getUpdatedAt());
        return response;
    }
}