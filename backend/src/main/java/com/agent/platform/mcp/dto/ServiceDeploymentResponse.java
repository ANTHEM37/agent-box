package com.agent.platform.mcp.dto;

import com.agent.platform.mcp.entity.ServiceDeployment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务部署响应
 */
@Data
public class ServiceDeploymentResponse {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private String serviceDisplayName;
    private Long versionId;
    private String version;
    private Long userId;
    private String deploymentName;
    private String config;
    private ServiceDeployment.DeploymentStatus status;
    private String endpointUrl;
    private Integer replicas;
    private String cpuLimit;
    private String memoryLimit;
    private Boolean autoScaling;
    private Integer minReplicas;
    private Integer maxReplicas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 实例信息
    private List<ServiceInstanceResponse> instances;
    private Integer runningInstancesCount;
    private Integer totalInstancesCount;

    // 统计信息
    private Long totalRequests;
    private Long totalErrors;
    private Double errorRate;
    private Long avgResponseTime;

    /**
     * 从实体转换为响应对象
     */
    public static ServiceDeploymentResponse fromEntity(ServiceDeployment deployment) {
        ServiceDeploymentResponse response = new ServiceDeploymentResponse();
        response.setId(deployment.getId());
        response.setServiceId(deployment.getService().getId());
        response.setServiceName(deployment.getService().getName());
        response.setServiceDisplayName(deployment.getService().getDisplayName());
        response.setVersionId(deployment.getVersion().getId());
        response.setVersion(deployment.getVersion().getVersion());
        response.setUserId(deployment.getUserId());
        response.setDeploymentName(deployment.getDeploymentName());
        response.setConfig(deployment.getConfig());
        response.setStatus(deployment.getStatus());
        response.setEndpointUrl(deployment.getEndpointUrl());
        response.setReplicas(deployment.getReplicas());
        response.setCpuLimit(deployment.getCpuLimit());
        response.setMemoryLimit(deployment.getMemoryLimit());
        response.setAutoScaling(deployment.getAutoScaling());
        response.setMinReplicas(deployment.getMinReplicas());
        response.setMaxReplicas(deployment.getMaxReplicas());
        response.setCreatedAt(deployment.getCreatedAt());
        response.setUpdatedAt(deployment.getUpdatedAt());
        return response;
    }
}