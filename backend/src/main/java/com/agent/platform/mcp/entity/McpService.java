package com.agent.platform.mcp.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * MCP 服务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mcp_services")
public class McpService extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @ElementCollection
    @CollectionTable(name = "mcp_service_tags", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_model")
    private PriceModel priceModel = PriceModel.FREE;

    @Column(name = "price_per_request", precision = 10, scale = 4)
    private BigDecimal pricePerRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status = ServiceStatus.DRAFT;

    @Column(name = "featured")
    private Boolean featured = false;

    @Column(name = "downloads_count")
    private Long downloadsCount = 0L;

    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    // 关联关系
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceVersion> versions;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceReview> reviews;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceDeployment> deployments;

    /**
     * 价格模式枚举
     */
    public enum PriceModel {
        FREE("免费"),
        PAID("付费"),
        FREEMIUM("免费增值");

        private final String displayName;

        PriceModel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 服务状态枚举
     */
    public enum ServiceStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        DEPRECATED("已弃用");

        private final String displayName;

        ServiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 更新评分
     */
    public void updateRating(int newRating) {
        if (this.ratingCount == 0) {
            this.ratingAverage = BigDecimal.valueOf(newRating);
            this.ratingCount = 1;
        } else {
            BigDecimal totalRating = this.ratingAverage.multiply(BigDecimal.valueOf(this.ratingCount));
            totalRating = totalRating.add(BigDecimal.valueOf(newRating));
            this.ratingCount++;
            this.ratingAverage = totalRating.divide(BigDecimal.valueOf(this.ratingCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * 增加下载次数
     */
    public void incrementDownloads() {
        this.downloadsCount++;
    }
}