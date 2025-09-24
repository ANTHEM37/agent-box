package com.agent.platform.workflow.engine;

import java.util.Map;

/**
 * 节点执行器接口
 */
public interface NodeExecutor {
    
    /**
     * 执行节点
     * 
     * @param context 执行上下文
     * @return 节点输出数据
     * @throws Exception 执行异常
     */
    Map<String, Object> execute(ExecutionContext context) throws Exception;
    
    /**
     * 获取节点类型
     */
    String getNodeType();
    
    /**
     * 获取节点显示名称
     */
    String getDisplayName();
    
    /**
     * 获取节点描述
     */
    String getDescription();
    
    /**
     * 验证节点配置
     * 
     * @param config 节点配置
     * @return 验证结果，null表示验证通过
     */
    default String validateConfig(Map<String, Object> config) {
        return null; // 默认不验证
    }
    
    /**
     * 是否支持重试
     */
    default boolean supportsRetry() {
        return true;
    }
    
    /**
     * 获取默认重试次数
     */
    default int getDefaultRetryCount() {
        return 3;
    }
    
    /**
     * 获取节点配置模式（用于前端渲染配置表单）
     */
    default Map<String, Object> getConfigSchema() {
        return null;
    }
}