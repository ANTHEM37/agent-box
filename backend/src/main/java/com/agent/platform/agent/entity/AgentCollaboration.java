package com.agent.platform.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.agent.platform.common.entity.BaseEntity;
import java.util.List;

/**
 * 智能体协作实体
 */
@Entity
@Table(name = "agent_collaborations")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentCollaboration extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 协作会话标题
     */
    @Column(nullable = false, length = 500)
    private String title;
    
    /**
     * 协作描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 协作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollaborationType type;
    
    /**
     * 协作状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollaborationStatus status;
    
    /**
     * 主要智能体（发起者）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_agent_id", nullable = false)
    private AIAgent primaryAgent;
    
    /**
     * 协作智能体IDs（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String participantAgentIds;
    
    /**
     * 关联任务ID
     */
    @Column
    private Long taskId;
    
    /**
     * 协作目标
     */
    @Column(columnDefinition = "TEXT")
    private String objective;
    
    /**
     * 协作策略
     */
    @Enumerated(EnumType.STRING)
    @Column
    private CollaborationStrategy strategy;
    
    /**
     * 协作规则（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String rules;
    
    /**
     * 协作结果
     */
    @Column(columnDefinition = "TEXT")
    private String result;
    
    /**
     * 开始时间
     */
    @Column
    private java.time.LocalDateTime startedAt;
    
    /**
     * 结束时间
     */
    @Column
    private java.time.LocalDateTime endedAt;
    
    /**
     * 协作时长（毫秒）
     */
    @Column
    private Long duration;
    
    /**
     * 消息数量
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer messageCount;
    
    /**
     * 参与者数量
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer participantCount;
    
    /**
     * 协作评分（1-10）
     */
    @Column(columnDefinition = "INTEGER")
    private Integer rating;
    
    /**
     * 协作反馈
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    /**
     * 扩展数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    // 关联关系
    
    /**
     * 协作消息
     */
    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CollaborationMessage> messages;
}

/**
 * 协作类型枚举
 */
enum CollaborationType {
    TASK_DECOMPOSITION, // 任务分解
    PARALLEL_EXECUTION, // 并行执行
    SEQUENTIAL_EXECUTION, // 顺序执行
    PEER_REVIEW,       // 同行评议
    BRAINSTORMING,     // 头脑风暴
    DEBATE,            // 辩论
    CONSULTATION,      // 咨询
    TEACHING,          // 教学
    RESEARCH,          // 研究
    PROBLEM_SOLVING    // 问题解决
}

/**
 * 协作状态枚举
 */
enum CollaborationStatus {
    INITIATED,         // 已发起
    IN_PROGRESS,       // 进行中
    WAITING_RESPONSE,  // 等待响应
    COMPLETED,         // 已完成
    FAILED,            // 失败
    CANCELLED,         // 已取消
    TIMEOUT            // 超时
}

/**
 * 协作策略枚举
 */
enum CollaborationStrategy {
    CONSENSUS,         // 共识
    MAJORITY_VOTE,     // 多数投票
    EXPERT_DECISION,   // 专家决策
    HIERARCHICAL,      // 层级决策
    NEGOTIATION,       // 协商
    COMPETITION,       // 竞争
    COOPERATION        // 合作
}