package com.agent.platform.agent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import java.util.List;

/**
 * 智能体定义实体
 * 定义智能体的基本属性和能力
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent_definition")
public class AgentDefinition extends BaseEntity {

    /**
     * 智能体名称
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * 智能体描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 智能体类型
     * AGENT: 普通智能体, WORKFLOW: 工作流智能体, COORDINATOR: 协调器智能体
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AgentType type = AgentType.AGENT;

    /**
     * 智能体角色定义
     */
    @Column(columnDefinition = "TEXT")
    private String roleDefinition;

    /**
     * 系统提示词
     */
    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    /**
     * 能力列表
     */
    @ElementCollection
    @CollectionTable(name = "agent_definition_capabilities", joinColumns = @JoinColumn(name = "agent_definition_id"))
    @Column(name = "capability")
    private List<String> capabilities;

    /**
     * 模型配置
     */
    @Column(name = "model_config", columnDefinition = "TEXT")
    private String modelConfig;

    /**
     * 配置信息（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String config;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    private Integer maxTokens = 2000;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 版本号
     */
    @Column(nullable = false)
    private String version = "1.0.0";

    /**
     * 智能体类型枚举
     */
    public enum AgentType {
        AGENT,          // 普通智能体
        WORKFLOW,       // 工作流智能体
        COORDINATOR,    // 协调器智能体
        SPECIALIST      // 专业智能体
    }
}