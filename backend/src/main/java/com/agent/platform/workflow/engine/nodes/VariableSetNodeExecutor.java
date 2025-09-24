package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 变量设置节点执行器
 */
@Slf4j
@Component
public class VariableSetNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行变量设置节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 获取变量设置配置
        List<Map<String, Object>> variables = (List<Map<String, Object>>) context.getConfig("variables");
        
        if (variables != null) {
            for (Map<String, Object> variable : variables) {
                String name = (String) variable.get("name");
                Object value = variable.get("value");
                String type = (String) variable.get("type");
                
                if (name != null && value != null) {
                    // 解析变量值
                    Object resolvedValue = resolveValue(value, type, context);
                    
                    // 设置变量
                    context.setVariable(name, resolvedValue);
                    output.put(name, resolvedValue);
                    
                    log.debug("设置变量: {}={} (type={})", name, resolvedValue, type);
                }
            }
        }
        
        output.put("variableCount", variables != null ? variables.size() : 0);
        
        log.info("变量设置节点执行完成: nodeId={}, 设置了 {} 个变量", 
                context.getNodeId(), output.get("variableCount"));
        return output;
    }
    
    /**
     * 解析变量值
     */
    private Object resolveValue(Object value, String type, ExecutionContext context) {
        // 如果是字符串，先解析变量
        if (value instanceof String) {
            String stringValue = context.resolveVariables((String) value);
            
            // 根据类型转换
            if (type != null) {
                switch (type.toLowerCase()) {
                    case "number":
                    case "integer":
                        try {
                            return Integer.parseInt(stringValue);
                        } catch (NumberFormatException e) {
                            try {
                                return Double.parseDouble(stringValue);
                            } catch (NumberFormatException e2) {
                                log.warn("无法将 '{}' 转换为数字，保持字符串类型", stringValue);
                                return stringValue;
                            }
                        }
                    case "boolean":
                        return Boolean.parseBoolean(stringValue);
                    case "string":
                    default:
                        return stringValue;
                }
            }
            
            return stringValue;
        }
        
        return value;
    }
    
    @Override
    public String getNodeType() {
        return "variable_set";
    }
    
    @Override
    public String getDisplayName() {
        return "变量设置";
    }
    
    @Override
    public String getDescription() {
        return "设置工作流变量，支持多种数据类型和变量引用";
    }
    
    @Override
    public String validateConfig(Map<String, Object> config) {
        List<Map<String, Object>> variables = (List<Map<String, Object>>) config.get("variables");
        
        if (variables == null || variables.isEmpty()) {
            return "至少需要设置一个变量";
        }
        
        for (Map<String, Object> variable : variables) {
            String name = (String) variable.get("name");
            if (name == null || name.trim().isEmpty()) {
                return "变量名不能为空";
            }
            
            if (variable.get("value") == null) {
                return "变量值不能为空";
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 变量列表配置
        Map<String, Object> variablesConfig = new HashMap<>();
        variablesConfig.put("type", "array");
        variablesConfig.put("title", "变量列表");
        variablesConfig.put("description", "要设置的变量列表");
        
        Map<String, Object> itemSchema = new HashMap<>();
        Map<String, Object> itemProperties = new HashMap<>();
        
        // 变量名
        Map<String, Object> nameConfig = new HashMap<>();
        nameConfig.put("type", "string");
        nameConfig.put("title", "变量名");
        nameConfig.put("description", "变量的名称");
        itemProperties.put("name", nameConfig);
        
        // 变量值
        Map<String, Object> valueConfig = new HashMap<>();
        valueConfig.put("type", "string");
        valueConfig.put("title", "变量值");
        valueConfig.put("description", "变量的值，支持变量引用 ${variableName}");
        itemProperties.put("value", valueConfig);
        
        // 变量类型
        Map<String, Object> typeConfig = new HashMap<>();
        typeConfig.put("type", "string");
        typeConfig.put("title", "数据类型");
        typeConfig.put("enum", new String[]{"string", "number", "boolean"});
        typeConfig.put("default", "string");
        itemProperties.put("type", typeConfig);
        
        itemSchema.put("type", "object");
        itemSchema.put("properties", itemProperties);
        itemSchema.put("required", new String[]{"name", "value"});
        
        variablesConfig.put("items", itemSchema);
        properties.put("variables", variablesConfig);
        
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[]{"variables"});
        return schema;
    }
}