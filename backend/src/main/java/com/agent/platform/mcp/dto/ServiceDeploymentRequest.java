package com.agent.platform.mcp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 服务部署请求
 */
@Data
public class ServiceDeploymentRequest {

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    @NotNull(message = "版本ID不能为空")
    private Long versionId;

    @NotBlank(message = "部署名称不能为空")
    private String deploymentName;

    private String config;

    @Min(value = 1, message = "副本数不能小于1")
    private Integer replicas = 1;

    private String cpuLimit;

    private String memoryLimit;

    private Boolean autoScaling = false;

    @Min(value = 1, message = "最小副本数不能小于1")
    private Integer minReplicas = 1;

    @Min(value = 1, message = "最大副本数不能小于1")
    private Integer maxReplicas = 10;
}