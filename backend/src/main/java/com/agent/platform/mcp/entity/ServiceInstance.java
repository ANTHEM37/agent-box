package com.agent.platform.mcp.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 服务实例实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "service_instances")
public class ServiceInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id", nullable = false)
    private ServiceDeployment deployment;

    @Column(name = "container_id", length = 100)
    private String containerId;

    @Column(name = "host", length = 100)
    private String host;

    @Column(name = "port")
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InstanceStatus status = InstanceStatus.STARTING;

    @Column(name = "health_check_url", length = 500)
    private String healthCheckUrl;

    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    @Column(name = "cpu_usage", precision = 5, scale = 2)
    private java.math.BigDecimal cpuUsage;

    @Column(name = "memory_usage", precision = 5, scale = 2)
    private java.math.BigDecimal memoryUsage;

    @Column(name = "request_count")
    private Long requestCount = 0L;

    @Column(name = "error_count")
    private Long errorCount = 0L;

    @Column(name = "avg_response_time")
    private Long avgResponseTime;

    /**
     * 实例状态枚举
     */
    public enum InstanceStatus {
        STARTING("启动中"),
        RUNNING("运行中"),
        STOPPING("停止中"),
        STOPPED("已停止"),
        FAILED("失败"),
        UNHEALTHY("不健康");

        private final String displayName;

        InstanceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 更新健康检查状态
     */
    public void updateHealthCheck(boolean healthy) {
        this.lastHealthCheck = LocalDateTime.now();
        if (healthy && this.status == InstanceStatus.UNHEALTHY) {
            this.status = InstanceStatus.RUNNING;
        } else if (!healthy && this.status == InstanceStatus.RUNNING) {
            this.status = InstanceStatus.UNHEALTHY;
        }
    }

    /**
     * 增加请求计数
     */
    public void incrementRequestCount() {
        this.requestCount++;
    }

    /**
     * 增加错误计数
     */
    public void incrementErrorCount() {
        this.errorCount++;
    }
}