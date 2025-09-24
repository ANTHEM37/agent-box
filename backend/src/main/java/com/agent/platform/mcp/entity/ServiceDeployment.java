package com.agent.platform.mcp.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 服务部署实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "service_deployments")
public class ServiceDeployment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private McpService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ServiceVersion version;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "deployment_name", nullable = false, length = 100)
    private String deploymentName;

    @Column(name = "config", columnDefinition = "JSONB")
    private String config;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeploymentStatus status = DeploymentStatus.DEPLOYING;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Column(name = "replicas")
    private Integer replicas = 1;

    @Column(name = "cpu_limit")
    private String cpuLimit;

    @Column(name = "memory_limit")
    private String memoryLimit;

    @Column(name = "auto_scaling")
    private Boolean autoScaling = false;

    @Column(name = "min_replicas")
    private Integer minReplicas = 1;

    @Column(name = "max_replicas")
    private Integer maxReplicas = 10;

    // 关联关系
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceInstance> instances;

    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceUsage> usageRecords;

    /**
     * 部署状态枚举
     */
    public enum DeploymentStatus {
        DEPLOYING("部署中"),
        RUNNING("运行中"),
        STOPPED("已停止"),
        FAILED("部署失败"),
        UPDATING("更新中");

        private final String displayName;

        DeploymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}