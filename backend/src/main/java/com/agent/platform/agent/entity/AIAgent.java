package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;
import java.util.List;
import java.util.Map;

/**
 * AI智能体实体
 */
@Entity
@Table(name = "ai_agents")
@Data
@EqualsAndHashCode(callSuper = true)
public class AIAgent extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 智能体名称（唯一标识）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * 显示名称
     */
    @Column(nullable = false, length = 200)
    private String displayName;
    
    /**
     * 智能体描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 智能体类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentType type;
    
    /**
     * 智能体状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status;
    
    /**
     * 创建者ID
     */
    @Column(nullable = false)
    private Long creatorId;
    
    /**
     * 创建者名称
     */
    @Column(length = 100)
    private String creatorName;
    
    /**
     * 智能体头像URL
     */
    @Column(length = 500)
    private String avatarUrl;
    
    /**
     * 个性配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String personalityConfig;
    
    /**
     * 能力配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String capabilitiesConfig;
    
    /**
     * 知识库配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String knowledgeBaseConfig;
    
    /**
     * 工具配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String toolsConfig;
    
    /**
     * 模型配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String modelConfig;
    
    /**
     * 系统提示词
     */
    @Column(columnDefinition = "TEXT")
    private String systemPrompt;
    
    /**
     * 温度参数（0.0-2.0）
     */
    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0.7")
    private Double temperature;
    
    /**
     * 最大令牌数
     */
    @Column(columnDefinition = "INTEGER DEFAULT 4000")
    private Integer maxTokens;
    
    /**
     * 是否启用记忆
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean memoryEnabled;
    
    /**
     * 记忆容量（条数）
     */
    @Column(columnDefinition = "INTEGER DEFAULT 1000")
    private Integer memoryCapacity;
    
    /**
     * 是否启用学习
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean learningEnabled;
    
    /**
     * 是否支持协作
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean collaborationEnabled;
    
    /**
     * 优先级（1-10，数字越大优先级越高）
     */
    @Column(columnDefinition = "INTEGER DEFAULT 5")
    private Integer priority;
    
    /**
     * 标签（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 是否公开
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isPublic;
    
    /**
     * 使用次数
     */
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long usageCount;
    
    /**
     * 成功率
     */
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    private Double successRate;
    
    /**
     * 平均响应时间（毫秒）
     */
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long avgResponseTime;
    
    /**
     * 最后活跃时间
     */
    @Column
    private java.time.LocalDateTime lastActiveAt;
    
    /**
     * 版本号
     */
    @Column(columnDefinition = "INTEGER DEFAULT 1")
    private Integer version;
    
    /**
     * 扩展配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String extensionConfig;
    
    // 关联关系
    
    /**
     * 智能体记忆
     */
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgentMemory> memories;
    
    /**
     * 智能体任务
     */
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgentTask> tasks;
    
    /**
     * 智能体会话
     */
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgentConversation> conversations;
    
    /**
     * 智能体协作
     */
    @OneToMany(mappedBy = "primaryAgent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgentCollaboration> collaborations;
}

