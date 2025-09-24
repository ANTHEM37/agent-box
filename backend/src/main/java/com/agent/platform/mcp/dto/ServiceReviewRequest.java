package com.agent.platform.mcp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 服务评论请求
 */
@Data
public class ServiceReviewRequest {

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;

    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String comment;
}