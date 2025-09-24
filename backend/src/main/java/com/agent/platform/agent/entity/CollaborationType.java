package com.agent.platform.agent.entity;

/**
 * 协作类型枚举
 */
public enum CollaborationType {
    SEQUENTIAL("顺序协作"),
    PARALLEL("并行协作"),
    HIERARCHICAL("层次协作"),
    PEER_TO_PEER("点对点协作"),
    MASTER_SLAVE("主从协作"),
    CONSENSUS("共识协作"),
    COMPETITIVE("竞争协作"),
    COOPERATIVE("合作协作");

    private final String description;

    CollaborationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}