package com.agent.platform.mcp.dto;

import com.agent.platform.mcp.entity.McpService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * MCP 服务创建请求
 */
@Data
public class McpServiceCreateRequest {

    @NotBlank(message = "服务名称不能为空")
    @Size(max = 100, message = "服务名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(max = 200, message = "显示名称长度不能超过200个字符")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000个字符")
    private String description;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    private List<String> tags;

    private String repositoryUrl;

    private String documentationUrl;

    private String iconUrl;

    @NotNull(message = "价格模式不能为空")
    private McpService.PriceModel priceModel;

    private BigDecimal pricePerRequest;

    // 初始版本信息
    @NotBlank(message = "版本号不能为空")
    private String version;

    private String changelog;

    @NotBlank(message = "Docker镜像不能为空")
    private String dockerImage;

    private String configSchema;

    private String apiSpec;
}