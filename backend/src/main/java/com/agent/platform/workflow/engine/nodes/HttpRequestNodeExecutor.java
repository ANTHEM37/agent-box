package com.agent.platform.workflow.engine.nodes;

import com.agent.platform.workflow.engine.ExecutionContext;
import com.agent.platform.workflow.engine.NodeExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求节点执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRequestNodeExecutor implements NodeExecutor {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public Map<String, Object> execute(ExecutionContext context) throws Exception {
        log.info("执行HTTP请求节点: nodeId={}", context.getNodeId());
        
        Map<String, Object> output = new HashMap<>();
        
        // 获取配置
        String url = context.getStringConfig("url", "");
        String method = context.getStringConfig("method", "GET");
        Map<String, String> headers = (Map<String, String>) context.getConfig("headers");
        Object requestBody = context.getConfig("requestBody");
        Integer timeout = context.getIntegerConfig("timeout", 30000);
        
        // 解析变量
        String resolvedUrl = context.resolveVariables(url);
        
        if (resolvedUrl.isEmpty()) {
            throw new RuntimeException("请求URL不能为空");
        }
        
        try {
            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String headerValue = context.resolveVariables(entry.getValue());
                    httpHeaders.add(entry.getKey(), headerValue);
                }
            }
            
            // 设置Content-Type
            if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            }
            
            // 构建请求体
            String requestBodyStr = null;
            if (requestBody != null) {
                if (requestBody instanceof String) {
                    requestBodyStr = context.resolveVariables((String) requestBody);
                } else {
                    requestBodyStr = objectMapper.writeValueAsString(requestBody);
                }
            }
            
            // 创建请求实体
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyStr, httpHeaders);
            
            // 执行请求
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    resolvedUrl,
                    HttpMethod.valueOf(method.toUpperCase()),
                    requestEntity,
                    String.class
            );
            long duration = System.currentTimeMillis() - startTime;
            
            // 解析响应
            String responseBody = response.getBody();
            Object responseData = null;
            try {
                if (responseBody != null && !responseBody.isEmpty()) {
                    responseData = objectMapper.readValue(responseBody, Object.class);
                }
            } catch (Exception e) {
                // 如果不是JSON格式，保持原始字符串
                responseData = responseBody;
            }
            
            // 设置输出
            output.put("statusCode", response.getStatusCodeValue());
            output.put("headers", response.getHeaders().toSingleValueMap());
            output.put("body", responseData);
            output.put("rawBody", responseBody);
            output.put("duration", duration);
            output.put("url", resolvedUrl);
            output.put("method", method);
            output.put("success", response.getStatusCode().is2xxSuccessful());
            
            log.info("HTTP请求节点执行完成: nodeId={}, statusCode={}, duration={}ms", 
                    context.getNodeId(), response.getStatusCodeValue(), duration);
            
        } catch (Exception e) {
            log.error("HTTP请求失败: nodeId={}, url={}, error={}", 
                    context.getNodeId(), resolvedUrl, e.getMessage(), e);
            
            // 记录错误信息
            output.put("success", false);
            output.put("error", e.getMessage());
            output.put("url", resolvedUrl);
            output.put("method", method);
            
            // 根据配置决定是否抛出异常
            Boolean continueOnError = context.getBooleanConfig("continueOnError", false);
            if (!continueOnError) {
                throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
            }
        }
        
        return output;
    }
    
    @Override
    public String getNodeType() {
        return "http_request";
    }
    
    @Override
    public String getDisplayName() {
        return "HTTP请求";
    }
    
    @Override
    public String getDescription() {
        return "发送HTTP请求到外部API，支持GET、POST、PUT、DELETE等方法";
    }
    
    @Override
    public String validateConfig(Map<String, Object> config) {
        String url = (String) config.get("url");
        if (url == null || url.trim().isEmpty()) {
            return "请求URL不能为空";
        }
        
        String method = (String) config.get("method");
        if (method != null) {
            try {
                HttpMethod.valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "不支持的HTTP方法: " + method;
            }
        }
        
        Object timeout = config.get("timeout");
        if (timeout != null) {
            int t = ((Number) timeout).intValue();
            if (t < 1000 || t > 300000) {
                return "超时时间必须在1000-300000毫秒之间";
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // URL配置
        Map<String, Object> urlConfig = new HashMap<>();
        urlConfig.put("type", "string");
        urlConfig.put("title", "请求URL");
        urlConfig.put("description", "HTTP请求的目标URL，支持变量替换");
        urlConfig.put("format", "uri");
        properties.put("url", urlConfig);
        
        // 方法配置
        Map<String, Object> methodConfig = new HashMap<>();
        methodConfig.put("type", "string");
        methodConfig.put("title", "请求方法");
        methodConfig.put("enum", new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});
        methodConfig.put("default", "GET");
        properties.put("method", methodConfig);
        
        // 请求头配置
        Map<String, Object> headersConfig = new HashMap<>();
        headersConfig.put("type", "object");
        headersConfig.put("title", "请求头");
        headersConfig.put("description", "HTTP请求头，支持变量替换");
        properties.put("headers", headersConfig);
        
        // 请求体配置
        Map<String, Object> bodyConfig = new HashMap<>();
        bodyConfig.put("type", "string");
        bodyConfig.put("title", "请求体");
        bodyConfig.put("description", "HTTP请求体，支持JSON格式和变量替换");
        bodyConfig.put("format", "textarea");
        properties.put("requestBody", bodyConfig);
        
        // 超时配置
        Map<String, Object> timeoutConfig = new HashMap<>();
        timeoutConfig.put("type", "integer");
        timeoutConfig.put("title", "超时时间(毫秒)");
        timeoutConfig.put("minimum", 1000);
        timeoutConfig.put("maximum", 300000);
        timeoutConfig.put("default", 30000);
        properties.put("timeout", timeoutConfig);
        
        // 错误处理配置
        Map<String, Object> errorConfig = new HashMap<>();
        errorConfig.put("type", "boolean");
        errorConfig.put("title", "出错时继续执行");
        errorConfig.put("description", "请求失败时是否继续执行后续节点");
        errorConfig.put("default", false);
        properties.put("continueOnError", errorConfig);
        
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[]{"url"});
        return schema;
    }
}