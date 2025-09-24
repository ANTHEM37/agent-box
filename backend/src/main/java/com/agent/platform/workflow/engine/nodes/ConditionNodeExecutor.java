package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件判断节点执行器
 */
@Slf4j
@Component
public class ConditionNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行条件判断节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 获取配置
        String leftValue = context.getStringConfig("leftValue", "");
        String operator = context.getStringConfig("operator", "equals");
        String rightValue = context.getStringConfig("rightValue", "");
        String valueType = context.getStringConfig("valueType", "string");
        
        // 解析变量
        String resolvedLeft = context.resolveVariables(leftValue);
        String resolvedRight = context.resolveVariables(rightValue);
        
        try {
            // 执行条件判断
            boolean result = evaluateCondition(resolvedLeft, operator, resolvedRight, valueType);
            
            // 设置输出
            output.put("result", result);
            output.put("leftValue", resolvedLeft);
            output.put("rightValue", resolvedRight);
            output.put("operator", operator);
            output.put("valueType", valueType);
            
            log.info("条件判断节点执行完成: nodeId={}, result={}, condition='{} {} {}'", 
                    context.getNodeId(), result, resolvedLeft, operator, resolvedRight);
            
        } catch (Exception e) {
            log.error("条件判断失败: nodeId={}, error={}", context.getNodeId(), e.getMessage(), e);
            throw new RuntimeException("条件判断失败: " + e.getMessage(), e);
        }
        
        return output;
    }
    
    /**
     * 执行条件判断
     */
    private boolean evaluateCondition(String leftValue, String operator, String rightValue, String valueType) {
        // 根据数据类型转换值
        Object left = convertValue(leftValue, valueType);
        Object right = convertValue(rightValue, valueType);
        
        switch (operator.toLowerCase()) {
            case "equals":
            case "==":
                return objectEquals(left, right);
                
            case "not_equals":
            case "!=":
                return !objectEquals(left, right);
                
            case "greater_than":
            case ">":
                return compareNumbers(left, right) > 0;
                
            case "greater_than_or_equal":
            case ">=":
                return compareNumbers(left, right) >= 0;
                
            case "less_than":
            case "<":
                return compareNumbers(left, right) < 0;
                
            case "less_than_or_equal":
            case "<=":
                return compareNumbers(left, right) <= 0;
                
            case "contains":
                return left.toString().contains(right.toString());
                
            case "not_contains":
                return !left.toString().contains(right.toString());
                
            case "starts_with":
                return left.toString().startsWith(right.toString());
                
            case "ends_with":
                return left.toString().endsWith(right.toString());
                
            case "is_empty":
                return left == null || left.toString().trim().isEmpty();
                
            case "is_not_empty":
                return left != null && !left.toString().trim().isEmpty();
                
            default:
                throw new RuntimeException("不支持的操作符: " + operator);
        }
    }
    
    /**
     * 转换值类型
     */
    private Object convertValue(String value, String valueType) {
        if (value == null) {
            return null;
        }
        
        switch (valueType.toLowerCase()) {
            case "number":
                try {
                    if (value.contains(".")) {
                        return Double.parseDouble(value);
                    } else {
                        return Long.parseLong(value);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("无法将 '" + value + "' 转换为数字");
                }
                
            case "boolean":
                return Boolean.parseBoolean(value);
                
            case "string":
            default:
                return value;
        }
    }
    
    /**
     * 对象相等比较
     */
    private boolean objectEquals(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }
    
    /**
     * 数字比较
     */
    private int compareNumbers(Object left, Object right) {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new RuntimeException("比较操作需要数字类型的值");
        }
        
        double leftNum = ((Number) left).doubleValue();
        double rightNum = ((Number) right).doubleValue();
        
        return Double.compare(leftNum, rightNum);
    }
    
    @Override
    public String getNodeType() {
        return "condition";
    }
    
    @Override
    public String getDisplayName() {
        return "条件判断";
    }
    
    @Override
    public String getDescription() {
        return "根据条件表达式进行判断，控制工作流的执行路径";
    }
    
    @Override
    public String validateConfig(Map<String, Object> config) {
        String leftValue = (String) config.get("leftValue");
        if (leftValue == null || leftValue.trim().isEmpty()) {
            return "左值不能为空";
        }
        
        String operator = (String) config.get("operator");
        if (operator == null || operator.trim().isEmpty()) {
            return "操作符不能为空";
        }
        
        // 对于某些操作符，右值可以为空
        String[] noRightValueOps = {"is_empty", "is_not_empty"};
        boolean needsRightValue = true;
        for (String op : noRightValueOps) {
            if (op.equals(operator)) {
                needsRightValue = false;
                break;
            }
        }
        
        if (needsRightValue) {
            String rightValue = (String) config.get("rightValue");
            if (rightValue == null || rightValue.trim().isEmpty()) {
                return "右值不能为空";
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 左值配置
        Map<String, Object> leftConfig = new HashMap<>();
        leftConfig.put("type", "string");
        leftConfig.put("title", "左值");
        leftConfig.put("description", "条件表达式的左值，支持变量替换");
        properties.put("leftValue", leftConfig);
        
        // 操作符配置
        Map<String, Object> operatorConfig = new HashMap<>();
        operatorConfig.put("type", "string");
        operatorConfig.put("title", "操作符");
        operatorConfig.put("enum", new String[]{
            "equals", "not_equals", "greater_than", "greater_than_or_equal",
            "less_than", "less_than_or_equal", "contains", "not_contains",
            "starts_with", "ends_with", "is_empty", "is_not_empty"
        });
        operatorConfig.put("default", "equals");
        properties.put("operator", operatorConfig);
        
        // 右值配置
        Map<String, Object> rightConfig = new HashMap<>();
        rightConfig.put("type", "string");
        rightConfig.put("title", "右值");
        rightConfig.put("description", "条件表达式的右值，支持变量替换");
        properties.put("rightValue", rightConfig);
        
        // 值类型配置
        Map<String, Object> typeConfig = new HashMap<>();
        typeConfig.put("type", "string");
        typeConfig.put("title", "值类型");
        typeConfig.put("enum", new String[]{"string", "number", "boolean"});
        typeConfig.put("default", "string");
        properties.put("valueType", typeConfig);
        
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[]{"leftValue", "operator"});
        return schema;
    }
}