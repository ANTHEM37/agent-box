package com.agent.platform.mcp.dto;

import com.agent.platform.mcp.entity.McpService;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP 服务响应
 */
@Data
public class McpServiceResponse {

    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String category;
    private List<String> tags;
    private Long authorId;
    private String authorName;
    private String repositoryUrl;
    private String documentationUrl;
    private String iconUrl;
    private McpService.PriceModel priceModel;
    private BigDecimal pricePerRequest;
    private McpService.ServiceStatus status;
    private Boolean featured;
    private Long downloadsCount;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 最新版本信息
    private String latestVersion;
    private String latestVersionChangelog;
    private LocalDateTime latestVersionCreatedAt;

    // 统计信息
    private Integer versionsCount;
    private Integer reviewsCount;
    private Integer deploymentsCount;

    /**
     * 从实体转换为响应对象
     */
    public static McpServiceResponse fromEntity(McpService service) {
        McpServiceResponse response = new McpServiceResponse();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setDisplayName(service.getDisplayName());
        response.setDescription(service.getDescription());
        response.setCategory(service.getCategory());
        response.setTags(service.getTags());
        response.setAuthorId(service.getAuthorId());
        response.setRepositoryUrl(service.getRepositoryUrl());
        response.setDocumentationUrl(service.getDocumentationUrl());
        response.setIconUrl(service.getIconUrl());
        response.setPriceModel(service.getPriceModel());
        response.setPricePerRequest(service.getPricePerRequest());
        response.setStatus(service.getStatus());
        response.setFeatured(service.getFeatured());
        response.setDownloadsCount(service.getDownloadsCount());
        response.setRatingAverage(service.getRatingAverage());
        response.setRatingCount(service.getRatingCount());
        response.setCreatedAt(service.getCreatedAt());
        response.setUpdatedAt(service.getUpdatedAt());
        return response;
    }
}