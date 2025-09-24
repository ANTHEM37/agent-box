package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 结束节点执行器
 */
@Slf4j
@Component
public class EndNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行结束节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 获取输出配置
        String outputMessage = context.getStringConfig("message", "工作流执行完成");
        Map<String, Object> outputData = (Map<String, Object>) context.getConfig("outputData");
        
        // 设置输出消息
        output.put("message", context.resolveVariables(outputMessage));
        output.put("endTime", System.currentTimeMillis());
        output.put("executionId", context.getExecutionId());
        
        // 添加自定义输出数据
        if (outputData != null) {
            for (Map.Entry<String, Object> entry : outputData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 解析变量
                if (value instanceof String) {
                    value = context.resolveVariables((String) value);
                }
                
                output.put(key, value);
            }
        }
        
        // 更新工作流执行的输出数据
        context.getWorkflowExecution().setOutputData(output);
        
        log.info("结束节点执行完成: nodeId={}, message={}", context.getNodeId(), outputMessage);
        return output;
    }
    
    @Override
    public String getNodeType() {
        return "end";
    }
    
    @Override
    public String getDisplayName() {
        return "结束";
    }
    
    @Override
    public String getDescription() {
        return "工作流结束节点，输出最终结果并结束工作流执行";
    }
    
    @Override
    public boolean supportsRetry() {
        return false; // 结束节点不支持重试
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 输出消息配置
        Map<String, Object> messageConfig = new HashMap<>();
        messageConfig.put("type", "string");
        messageConfig.put("title", "输出消息");
        messageConfig.put("description", "工作流完成时的输出消息，支持变量替换");
        messageConfig.put("default", "工作流执行完成");
        properties.put("message", messageConfig);
        
        // 输出数据配置
        Map<String, Object> outputDataConfig = new HashMap<>();
        outputDataConfig.put("type", "object");
        outputDataConfig.put("title", "输出数据");
        outputDataConfig.put("description", "自定义输出数据，支持变量替换");
        properties.put("outputData", outputDataConfig);
        
        schema.put("type", "object");
        schema.put("properties", properties);
        return schema;
    }
}