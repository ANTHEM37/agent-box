package com.agent.platform.mcp.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务版本实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "service_versions")
public class ServiceVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private McpService service;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @Column(name = "docker_image", length = 200)
    private String dockerImage;

    @Column(name = "config_schema", columnDefinition = "JSONB")
    private String configSchema;

    @Column(name = "api_spec", columnDefinition = "JSONB")
    private String apiSpec;

    @Column(name = "is_latest")
    private Boolean isLatest = false;

    @Column(name = "download_count")
    private Long downloadCount = 0L;

    @Column(name = "size_mb", precision = 10, scale = 2)
    private java.math.BigDecimal sizeMb;

    /**
     * 增加下载次数
     */
    public void incrementDownloads() {
        this.downloadCount++;
    }
}