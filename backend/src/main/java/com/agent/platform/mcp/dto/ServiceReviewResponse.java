package com.agent.platform.mcp.dto;

import com.agent.platform.mcp.entity.ServiceReview;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务评论响应
 */
@Data
public class ServiceReviewResponse {

    private Long id;
    private Long serviceId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String comment;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应对象
     */
    public static ServiceReviewResponse fromEntity(ServiceReview review) {
        ServiceReviewResponse response = new ServiceReviewResponse();
        response.setId(review.getId());
        response.setServiceId(review.getService().getId());
        response.setUserId(review.getUserId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setHelpfulCount(review.getHelpfulCount());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}