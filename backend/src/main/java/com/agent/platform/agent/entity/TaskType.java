package com.agent.platform.agent.entity;

/**
 * 任务类型枚举
 */
public enum TaskType {
    CONVERSATION("对话"),
    ANALYSIS("分析"),
    GENERATION("生成"),
    TRANSLATION("翻译"),
    SUMMARIZATION("摘要"),
    QUESTION_ANSWERING("问答"),
    CODE_GENERATION("代码生成"),
    DATA_ANALYSIS("数据分析"),
    DOCUMENT_PROCESSING("文档处理"),
    RESEARCH("研究"),
    COLLABORATION("协作"),
    CUSTOM("自定义");

    private final String description;

    TaskType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}