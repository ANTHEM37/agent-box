package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 对话节点执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LLMChatNodeExecutor implements NodeExecutor {
    
    private final ChatLanguageModel chatLanguageModel;
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行LLM对话节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 获取配置
        String prompt = context.getStringConfig("prompt", "");
        String systemMessage = context.getStringConfig("systemMessage", "");
        Double temperature = ((Number) context.getConfig("temperature", 0.7)).doubleValue();
        Integer maxTokens = context.getIntegerConfig("maxTokens", 1000);
        
        // 解析变量
        String resolvedPrompt = context.resolveVariables(prompt);
        String resolvedSystemMessage = context.resolveVariables(systemMessage);
        
        if (resolvedPrompt.isEmpty()) {
            throw new RuntimeException("提示词不能为空");
        }
        
        try {
            // 构建消息
            UserMessage userMessage = UserMessage.from(resolvedPrompt);
            
            // 调用LLM
            AiMessage response = chatLanguageModel.generate(userMessage).content();
            String responseText = response.text();
            
            // 设置输出
            output.put("response", responseText);
            output.put("prompt", resolvedPrompt);
            output.put("systemMessage", resolvedSystemMessage);
            output.put("temperature", temperature);
            output.put("maxTokens", maxTokens);
            output.put("tokenCount", responseText.length() / 4); // 粗略估算
            
            log.info("LLM对话节点执行完成: nodeId={}, responseLength={}", 
                    context.getNodeId(), responseText.length());
            
        } catch (Exception e) {
            log.error("LLM调用失败: nodeId={}, error={}", context.getNodeId(), e.getMessage(), e);
            throw new RuntimeException("LLM调用失败: " + e.getMessage(), e);
        }
        
        return output;
    }
    
    @Override
    public String getNodeType() {
        return "llm_chat";
    }
    
    @Override
    public String getDisplayName() {
        return "LLM对话";
    }
    
    @Override
    public String getDescription() {
        return "调用大语言模型进行对话，支持自定义提示词和参数";
    }
    
    @Override
    public String validateConfig(Map<String, Object> config) {
        String prompt = (String) config.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return "提示词不能为空";
        }
        
        Object temperature = config.get("temperature");
        if (temperature != null) {
            double temp = ((Number) temperature).doubleValue();
            if (temp < 0 || temp > 2) {
                return "温度值必须在0-2之间";
            }
        }
        
        Object maxTokens = config.get("maxTokens");
        if (maxTokens != null) {
            int tokens = ((Number) maxTokens).intValue();
            if (tokens < 1 || tokens > 4000) {
                return "最大Token数必须在1-4000之间";
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 提示词配置
        Map<String, Object> promptConfig = new HashMap<>();
        promptConfig.put("type", "string");
        promptConfig.put("title", "提示词");
        promptConfig.put("description", "发送给LLM的提示词，支持变量替换 ${variableName}");
        promptConfig.put("format", "textarea");
        properties.put("prompt", promptConfig);
        
        // 系统消息配置
        Map<String, Object> systemConfig = new HashMap<>();
        systemConfig.put("type", "string");
        systemConfig.put("title", "系统消息");
        systemConfig.put("description", "系统角色设定，可选");
        systemConfig.put("format", "textarea");
        properties.put("systemMessage", systemConfig);
        
        // 温度配置
        Map<String, Object> temperatureConfig = new HashMap<>();
        temperatureConfig.put("type", "number");
        temperatureConfig.put("title", "温度");
        temperatureConfig.put("description", "控制输出的随机性，0-2之间");
        temperatureConfig.put("minimum", 0);
        temperatureConfig.put("maximum", 2);
        temperatureConfig.put("default", 0.7);
        properties.put("temperature", temperatureConfig);
        
        // 最大Token数配置
        Map<String, Object> maxTokensConfig = new HashMap<>();
        maxTokensConfig.put("type", "integer");
        maxTokensConfig.put("title", "最大Token数");
        maxTokensConfig.put("description", "生成文本的最大长度");
        maxTokensConfig.put("minimum", 1);
        maxTokensConfig.put("maximum", 4000);
        maxTokensConfig.put("default", 1000);
        properties.put("maxTokens", maxTokensConfig);
        
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[]{"prompt"});
        return schema;
    }
}