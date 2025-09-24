package com.agent.platform.workflow.engine;

import com.agent.platform.workflow.entity.NodeExecution;
import com.agent.platform.workflow.entity.WorkflowExecution;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class ExecutionContext {
    
    private WorkflowExecution workflowExecution;
    private NodeExecution nodeExecution;
    private Map<String, Object> variables;
    private Map<String, Object> inputData;
    private Map<String, Object> nodeConfig;
    
    /**
     * 创建执行上下文
     */
    public ExecutionContext createContext(WorkflowExecution workflowExecution, NodeExecution nodeExecution) {
        ExecutionContext context = new ExecutionContext();
        context.setWorkflowExecution(workflowExecution);
        context.setNodeExecution(nodeExecution);
        
        // 设置变量
        Map<String, Object> executionContext = workflowExecution.getContext();
        context.setVariables((Map<String, Object>) executionContext.get("variables"));
        context.setInputData(workflowExecution.getInputData());
        
        // 设置节点配置
        context.setNodeConfig(nodeExecution.getConfig());
        
        return context;
    }
    
    /**
     * 获取变量值
     */
    public Object getVariable(String key) {
        return variables != null ? variables.get(key) : null;
    }
    
    /**
     * 设置变量值
     */
    public void setVariable(String key, Object value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(key, value);
    }
    
    /**
     * 获取输入数据
     */
    public Object getInputValue(String key) {
        return inputData != null ? inputData.get(key) : null;
    }
    
    /**
     * 获取节点配置
     */
    public Object getConfig(String key) {
        return nodeConfig != null ? nodeConfig.get(key) : null;
    }
    
    /**
     * 获取节点配置（带默认值）
     */
    public Object getConfig(String key, Object defaultValue) {
        Object value = getConfig(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取字符串配置
     */
    public String getStringConfig(String key) {
        Object value = getConfig(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取字符串配置（带默认值）
     */
    public String getStringConfig(String key, String defaultValue) {
        String value = getStringConfig(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取布尔配置
     */
    public Boolean getBooleanConfig(String key) {
        Object value = getConfig(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }
    
    /**
     * 获取布尔配置（带默认值）
     */
    public Boolean getBooleanConfig(String key, Boolean defaultValue) {
        Boolean value = getBooleanConfig(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取整数配置
     */
    public Integer getIntegerConfig(String key) {
        Object value = getConfig(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取整数配置（带默认值）
     */
    public Integer getIntegerConfig(String key, Integer defaultValue) {
        Integer value = getIntegerConfig(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 解析变量表达式
     * 支持格式: ${variableName} 或 ${nodeId_output.fieldName}
     */
    public String resolveVariables(String expression) {
        if (expression == null || !expression.contains("${")) {
            return expression;
        }
        
        String result = expression;
        
        // 简单的变量替换实现
        while (result.contains("${") && result.contains("}")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);
            
            if (end > start) {
                String varExpression = result.substring(start + 2, end);
                Object value = resolveVariableExpression(varExpression);
                String replacement = value != null ? value.toString() : "";
                result = result.substring(0, start) + replacement + result.substring(end + 1);
            } else {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 解析变量表达式
     */
    private Object resolveVariableExpression(String expression) {
        if (expression.contains(".")) {
            // 处理嵌套属性，如 nodeId_output.fieldName
            String[] parts = expression.split("\\.", 2);
            Object obj = getVariable(parts[0]);
            
            if (obj instanceof Map) {
                return ((Map<?, ?>) obj).get(parts[1]);
            }
        } else {
            // 直接变量引用
            return getVariable(expression);
        }
        
        return null;
    }
    
    /**
     * 获取工作流ID
     */
    public Long getWorkflowId() {
        return workflowExecution != null ? workflowExecution.getWorkflow().getId() : null;
    }
    
    /**
     * 获取执行ID
     */
    public Long getExecutionId() {
        return workflowExecution != null ? workflowExecution.getId() : null;
    }
    
    /**
     * 获取节点ID
     */
    public String getNodeId() {
        return nodeExecution != null ? nodeExecution.getNodeId() : null;
    }
    
    /**
     * 获取节点类型
     */
    public String getNodeType() {
        return nodeExecution != null ? nodeExecution.getNodeType() : null;
    }
}