package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码执行节点执行器
 */
@Slf4j
@Component
public class CodeExecutionNodeExecutor implements NodeExecutor {
    
    private final ScriptEngineManager scriptEngineManager;
    
    public CodeExecutionNodeExecutor() {
        this.scriptEngineManager = new ScriptEngineManager();
    }
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行代码执行节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 获取配置
        String code = context.getStringConfig("code", "");
        String language = context.getStringConfig("language", "javascript");
        Integer timeout = context.getIntegerConfig("timeout", 10000);
        
        // 解析变量
        String resolvedCode = context.resolveVariables(code);
        
        if (resolvedCode.isEmpty()) {
            throw new RuntimeException("执行代码不能为空");
        }
        
        try {
            // 获取脚本引擎
            ScriptEngine engine = getScriptEngine(language);
            if (engine == null) {
                throw new RuntimeException("不支持的脚本语言: " + language);
            }
            
            // 设置变量到脚本上下文
            if (context.getVariables() != null) {
                for (Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                    engine.put(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置输入数据
            if (context.getInputData() != null) {
                engine.put("inputData", context.getInputData());
            }
            
            // 执行代码
            long startTime = System.currentTimeMillis();
            Object result = engine.eval(resolvedCode);
            long duration = System.currentTimeMillis() - startTime;
            
            // 设置输出
            output.put("result", result);
            output.put("duration", duration);
            output.put("language", language);
            output.put("codeLength", resolvedCode.length());
            
            // 获取脚本中设置的变量
            Map<String, Object> scriptVariables = new HashMap<>();
            for (String key : engine.getBindings(javax.script.ScriptContext.ENGINE_SCOPE).keySet()) {
                Object value = engine.get(key);
                if (value != null && !isBuiltinVariable(key)) {
                    scriptVariables.put(key, value);
                }
            }
            output.put("variables", scriptVariables);
            
            log.info("代码执行节点执行完成: nodeId={}, duration={}ms, language={}", 
                    context.getNodeId(), duration, language);
            
        } catch (ScriptException e) {
            log.error("代码执行失败: nodeId={}, error={}", context.getNodeId(), e.getMessage(), e);
            throw new RuntimeException("代码执行失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("代码执行节点异常: nodeId={}, error={}", context.getNodeId(), e.getMessage(), e);
            throw new RuntimeException("代码执行异常: " + e.getMessage(), e);
        }
        
        return output;
    }
    
    /**
     * 获取脚本引擎
     */
    private ScriptEngine getScriptEngine(String language) {
        switch (language.toLowerCase()) {
            case "javascript":
            case "js":
                return scriptEngineManager.getEngineByName("javascript");
            case "groovy":
                return scriptEngineManager.getEngineByName("groovy");
            case "python":
                return scriptEngineManager.getEngineByName("python");
            default:
                return null;
        }
    }
    
    /**
     * 检查是否为内置变量
     */
    private boolean isBuiltinVariable(String key) {
        return "inputData".equals(key) || 
               "context".equals(key) || 
               key.startsWith("javax.script") ||
               key.startsWith("java.lang");
    }
    
    @Override
    public String getNodeType() {
        return "code_execution";
    }
    
    @Override
    public String getDisplayName() {
        return "代码执行";
    }
    
    @Override
    public String getDescription() {
        return "执行自定义代码逻辑，支持JavaScript、Groovy等脚本语言";
    }
    
    @Override
    public String validateConfig(Map<String, Object> config) {
        String code = (String) config.get("code");
        if (code == null || code.trim().isEmpty()) {
            return "执行代码不能为空";
        }
        
        String language = (String) config.get("language");
        if (language != null) {
            ScriptEngine engine = getScriptEngine(language);
            if (engine == null) {
                return "不支持的脚本语言: " + language;
            }
        }
        
        Object timeout = config.get("timeout");
        if (timeout != null) {
            int t = ((Number) timeout).intValue();
            if (t < 1000 || t > 60000) {
                return "超时时间必须在1000-60000毫秒之间";
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 代码配置
        Map<String, Object> codeConfig = new HashMap<>();
        codeConfig.put("type", "string");
        codeConfig.put("title", "执行代码");
        codeConfig.put("description", "要执行的代码，支持变量替换");
        codeConfig.put("format", "textarea");
        properties.put("code", codeConfig);
        
        // 语言配置
        Map<String, Object> languageConfig = new HashMap<>();
        languageConfig.put("type", "string");
        languageConfig.put("title", "脚本语言");
        languageConfig.put("enum", new String[]{"javascript", "groovy"});
        languageConfig.put("default", "javascript");
        properties.put("language", languageConfig);
        
        // 超时配置
        Map<String, Object> timeoutConfig = new HashMap<>();
        timeoutConfig.put("type", "integer");
        timeoutConfig.put("title", "超时时间(毫秒)");
        timeoutConfig.put("minimum", 1000);
        timeoutConfig.put("maximum", 60000);
        timeoutConfig.put("default", 10000);
        properties.put("timeout", timeoutConfig);
        
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[]{"code"});
        return schema;
    }
}