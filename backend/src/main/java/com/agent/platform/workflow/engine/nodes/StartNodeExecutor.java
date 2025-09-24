package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 开始节点执行器
 */
@Slf4j
@Component
public class StartNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行开始节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 将输入数据传递给输出
        if (context.getInputData() != null) {
            output.putAll(context.getInputData());
        }
        
        // 添加执行信息
        output.put("executionId", context.getExecutionId());
        output.put("workflowId", context.getWorkflowId());
        output.put("startTime", System.currentTimeMillis());
        
        log.info("开始节点执行完成: nodeId={}", context.getNodeId());
        return output;
    }
    
    @Override
    public String getNodeType() {
        return "start";
    }
    
    @Override
    public String getDisplayName() {
        return "开始";
    }
    
    @Override
    public String getDescription() {
        return "工作流开始节点，接收输入数据并启动工作流执行";
    }
    
    @Override
    public boolean supportsRetry() {
        return false; // 开始节点不支持重试
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", new HashMap<String, Object>());
        return schema;
    }
}