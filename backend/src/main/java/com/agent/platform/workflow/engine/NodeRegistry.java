package com.agent.platform.workflow.engine;

import com.agent.platform.workflow.engine.nodes.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NodeRegistry {
    
    private final Map<String, NodeExecutor> executors = new HashMap<>();
    
    @Autowired
    private StartNodeExecutor startNodeExecutor;
    
    @Autowired
    private LLMChatNodeExecutor llmChatNodeExecutor;
    
    @Autowired
    private KnowledgeRetrievalNodeExecutor knowledgeRetrievalNodeExecutor;
    
    @Autowired
    private HttpRequestNodeExecutor httpRequestNodeExecutor;
    
    @Autowired
    private ConditionNodeExecutor conditionNodeExecutor;
    
    @Autowired
    private VariableSetNodeExecutor variableSetNodeExecutor;
    
    @Autowired
    private CodeExecutionNodeExecutor codeExecutionNodeExecutor;
    
    @Autowired
    private EndNodeExecutor endNodeExecutor;
    
    @PostConstruct
    public void registerNodes() {
        log.info("注册工作流节点执行器...");
        
        register("start", startNodeExecutor);
        register("llm_chat", llmChatNodeExecutor);
        register("knowledge_retrieval", knowledgeRetrievalNodeExecutor);
        register("http_request", httpRequestNodeExecutor);
        register("condition", conditionNodeExecutor);
        register("variable_set", variableSetNodeExecutor);
        register("code_execution", codeExecutionNodeExecutor);
        register("end", endNodeExecutor);
        
        log.info("已注册 {} 个节点执行器", executors.size());
    }
    
    /**
     * 注册节点执行器
     */
    public void register(String nodeType, NodeExecutor executor) {
        executors.put(nodeType, executor);
        log.debug("注册节点执行器: {}", nodeType);
    }
    
    /**
     * 获取节点执行器
     */
    public NodeExecutor getExecutor(String nodeType) {
        NodeExecutor executor = executors.get(nodeType);
        if (executor == null) {
            log.warn("未找到节点执行器: {}", nodeType);
        }
        return executor;
    }
    
    /**
     * 获取所有注册的节点类型
     */
    public Map<String, NodeExecutor> getAllExecutors() {
        return new HashMap<>(executors);
    }
    
    /**
     * 检查节点类型是否支持
     */
    public boolean isSupported(String nodeType) {
        return executors.containsKey(nodeType);
    }
}