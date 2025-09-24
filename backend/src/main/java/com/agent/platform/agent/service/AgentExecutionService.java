package com.agent.platform.agent.service;

import com.agent.platform.agent.entity.*;
import com.agent.platform.agent.repository.*;
import com.agent.platform.agent.dto.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体执行服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentExecutionService {
    
    private final ChatLanguageModel chatModel;
    private final AgentTaskRepository taskRepository;
    private final TaskExecutionLogRepository logRepository;
    private final MemoryService memoryService;
    private final CollaborationService collaborationService;
    
    /**
     * 执行任务
     */
    @Transactional
    public TaskExecutionResult executeTask(AIAgent agent, AgentTask task, TaskExecutionRequest request) {
        log.info("Executing task {} for agent {}", task.getId(), agent.getId());
        
        long startTime = System.currentTimeMillis();
        TaskExecutionResult result = new TaskExecutionResult();
        result.setTaskId(task.getId());
        result.setAgentId(agent.getId());
        result.setStartTime(LocalDateTime.now());
        
        try {
            // 更新任务状态
            task.setStatus(TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());
            taskRepository.save(task);
            
            // 记录开始执行日志
            logExecution(task, LogLevel.INFO, "Task execution started", ExecutionPhase.INITIALIZATION);
            
            // 检查是否需要协作
            if (task.getRequiresCollaboration()) {
                result = executeCollaborativeTask(agent, task, request);
            } else {
                result = executeSingleAgentTask(agent, task, request);
            }
            
            // 更新任务状态
            task.setStatus(result.isSuccess() ? TaskStatus.COMPLETED : TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());
            task.setExecutionDuration(System.currentTimeMillis() - startTime);
            task.setResult(result.getOutput());
            
            if (!result.isSuccess()) {
                task.setErrorMessage(result.getErrorMessage());
            }
            
            taskRepository.save(task);
            
            // 存储执行记忆
            storeExecutionMemory(agent, task, result);
            
            // 记录完成日志
            logExecution(task, result.isSuccess() ? LogLevel.INFO : LogLevel.ERROR, 
                "Task execution " + (result.isSuccess() ? "completed" : "failed"), 
                ExecutionPhase.COMPLETION);
            
        } catch (Exception e) {
            log.error("Task execution failed", e);
            
            task.setStatus(TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());
            task.setExecutionDuration(System.currentTimeMillis() - startTime);
            task.setErrorMessage(e.getMessage());
            taskRepository.save(task);
            
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            
            // 记录错误日志
            logExecution(task, LogLevel.ERROR, "Task execution failed: " + e.getMessage(), 
                ExecutionPhase.ERROR_HANDLING, e.getStackTrace().toString());
        }
        
        result.setEndTime(LocalDateTime.now());
        result.setResponseTime(System.currentTimeMillis() - startTime);
        
        return result;
    }
    
    /**
     * 执行单智能体任务
     */
    private TaskExecutionResult executeSingleAgentTask(AIAgent agent, AgentTask task, TaskExecutionRequest request) {
        log.debug("Executing single agent task: {}", task.getId());
        
        TaskExecutionResult result = new TaskExecutionResult();
        result.setTaskId(task.getId());
        result.setAgentId(agent.getId());
        
        try {
            // 构建上下文
            String context = buildTaskContext(agent, task, request);
            
            // 记录预处理日志
            logExecution(task, LogLevel.INFO, "Built task context", ExecutionPhase.PREPROCESSING);
            
            // 调用语言模型
            String response = callLanguageModel(agent, context, task);
            
            // 记录执行日志
            logExecution(task, LogLevel.INFO, "Language model response received", ExecutionPhase.EXECUTION);
            
            // 后处理结果
            String processedOutput = postProcessOutput(response, task.getType());
            
            result.setSuccess(true);
            result.setOutput(processedOutput);
            result.setRawResponse(response);
            
            // 记录后处理日志
            logExecution(task, LogLevel.INFO, "Output post-processed", ExecutionPhase.POSTPROCESSING);
            
        } catch (Exception e) {
            log.error("Single agent task execution failed", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 执行协作任务
     */
    private TaskExecutionResult executeCollaborativeTask(AIAgent agent, AgentTask task, TaskExecutionRequest request) {
        log.debug("Executing collaborative task: {}", task.getId());
        
        TaskExecutionResult result = new TaskExecutionResult();
        result.setTaskId(task.getId());
        result.setAgentId(agent.getId());
        
        try {
            // 发起协作
            CollaborationRequest collabRequest = new CollaborationRequest();
            collabRequest.setPrimaryAgentId(agent.getId());
            collabRequest.setTaskId(task.getId());
            collabRequest.setTitle("Collaborative task: " + task.getTitle());
            collabRequest.setObjective(task.getDescription());
            collabRequest.setType(CollaborationType.TASK_DECOMPOSITION.name());
            collabRequest.setStrategy(CollaborationStrategy.CONSENSUS.name());
            
            // 解析协作智能体IDs
            if (task.getCollaboratorAgentIds() != null) {
                // 这里应该解析JSON格式的智能体ID列表
                collabRequest.setParticipantAgentIds(Arrays.asList(task.getCollaboratorAgentIds().split(",")));
            }
            
            CollaborationResult collabResult = collaborationService.startCollaboration(collabRequest);
            
            result.setSuccess(collabResult.isSuccess());
            result.setOutput(collabResult.getResult());
            result.setCollaborationId(collabResult.getCollaborationId());
            
            if (!collabResult.isSuccess()) {
                result.setErrorMessage(collabResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Collaborative task execution failed", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 构建任务上下文
     */
    private String buildTaskContext(AIAgent agent, AgentTask task, TaskExecutionRequest request) {
        StringBuilder context = new StringBuilder();
        
        // 添加智能体系统提示
        if (agent.getSystemPrompt() != null) {
            context.append("System: ").append(agent.getSystemPrompt()).append("\n\n");
        }
        
        // 添加任务描述
        context.append("Task: ").append(task.getTitle()).append("\n");
        if (task.getDescription() != null) {
            context.append("Description: ").append(task.getDescription()).append("\n");
        }
        
        // 添加任务输入
        if (task.getInput() != null) {
            context.append("Input: ").append(task.getInput()).append("\n");
        }
        
        // 添加相关记忆
        if (agent.getMemoryEnabled()) {
            List<MemoryResponse> relevantMemories = getRelevantMemories(agent.getId(), task);
            if (!relevantMemories.isEmpty()) {
                context.append("\nRelevant memories:\n");
                for (MemoryResponse memory : relevantMemories) {
                    context.append("- ").append(memory.getContent()).append("\n");
                }
            }
        }
        
        // 添加用户请求
        if (request.getUserMessage() != null) {
            context.append("\nUser request: ").append(request.getUserMessage());
        }
        
        return context.toString();
    }
    
    /**
     * 调用语言模型
     */
    private String callLanguageModel(AIAgent agent, String context, AgentTask task) {
        try {
            // 构建消息列表
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            
            // 添加系统消息
            if (agent.getSystemPrompt() != null) {
                messages.add(new SystemMessage(agent.getSystemPrompt()));
            }
            
            // 添加用户消息
            messages.add(new UserMessage(context));
            
            // 调用模型
            AiMessage response = chatModel.generate(messages).content();
            
            return response.text();
            
        } catch (Exception e) {
            log.error("Failed to call language model", e);
            throw new RuntimeException("Language model call failed: " + e.getMessage());
        }
    }
    
    /**
     * 后处理输出
     */
    private String postProcessOutput(String rawOutput, TaskType taskType) {
        // 根据任务类型进行特定的后处理
        switch (taskType) {
            case CODE_GENERATION:
                return processCodeOutput(rawOutput);
            case DATA_ANALYSIS:
                return processDataAnalysisOutput(rawOutput);
            case DOCUMENT_PROCESSING:
                return processDocumentOutput(rawOutput);
            default:
                return rawOutput.trim();
        }
    }
    
    private String processCodeOutput(String output) {
        // 提取代码块
        if (output.contains("```")) {
            int start = output.indexOf("```");
            int end = output.lastIndexOf("```");
            if (start < end) {
                return output.substring(start + 3, end).trim();
            }
        }
        return output;
    }
    
    private String processDataAnalysisOutput(String output) {
        // 格式化数据分析结果
        return output.trim();
    }
    
    private String processDocumentOutput(String output) {
        // 格式化文档处理结果
        return output.trim();
    }
    
    /**
     * 获取相关记忆
     */
    private List<MemoryResponse> getRelevantMemories(Long agentId, AgentTask task) {
        try {
            MemoryQueryRequest queryRequest = new MemoryQueryRequest();
            queryRequest.setQuery(task.getTitle() + " " + task.getDescription());
            queryRequest.setLimit(5);
            queryRequest.setMinImportance(0.5);
            
            return memoryService.retrieveMemory(agentId, queryRequest);
        } catch (Exception e) {
            log.error("Failed to retrieve relevant memories", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 存储执行记忆
     */
    private void storeExecutionMemory(AIAgent agent, AgentTask task, TaskExecutionResult result) {
        if (!agent.getMemoryEnabled()) {
            return;
        }
        
        try {
            StoreMemoryRequest memoryRequest = new StoreMemoryRequest();
            memoryRequest.setType(MemoryType.EPISODIC.name());
            memoryRequest.setContent("Executed task: " + task.getTitle() + 
                ". Result: " + (result.isSuccess() ? "Success" : "Failed") + 
                ". Output: " + result.getOutput());
            memoryRequest.setSummary("Task execution: " + task.getTitle());
            memoryRequest.setImportance(result.isSuccess() ? 0.7 : 0.5);
            memoryRequest.setTaskId(task.getId());
            memoryRequest.setSource(MemorySource.TASK_EXECUTION.name());
            
            memoryService.storeMemory(agent.getId(), memoryRequest);
        } catch (Exception e) {
            log.error("Failed to store execution memory", e);
        }
    }
    
    /**
     * 记录执行日志
     */
    private void logExecution(AgentTask task, LogLevel level, String message, ExecutionPhase phase) {
        logExecution(task, level, message, phase, null);
    }
    
    private void logExecution(AgentTask task, LogLevel level, String message, ExecutionPhase phase, String details) {
        try {
            TaskExecutionLog log = new TaskExecutionLog();
            log.setTask(task);
            log.setLevel(level);
            log.setMessage(message);
            log.setDetails(details);
            log.setPhase(phase);
            log.setTimestamp(LocalDateTime.now());
            
            logRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save execution log", e);
        }
    }
}