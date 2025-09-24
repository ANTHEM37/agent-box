package com.agent.platform.mcp.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务评论实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "service_reviews", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "user_id"}))
public class ServiceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private McpService service;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    /**
     * 增加有用计数
     */
    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    /**
     * 验证评分范围
     */
    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
    }
}