package com.agent.platform.workflow.engine;

import com.agent.platform.workflow.entity.NodeExecution;
import com.agent.platform.workflow.entity.Workflow;
import com.agent.platform.workflow.entity.WorkflowExecution;
import com.agent.platform.workflow.repository.NodeExecutionRepository;
import com.agent.platform.workflow.repository.WorkflowExecutionRepository;
import com.agent.platform.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {
    
    private final WorkflowService workflowService;
    private final WorkflowExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final NodeRegistry nodeRegistry;
    private final ExecutionContext executionContext;
    
    /**
     * 执行工作流
     */
    @Transactional
    public WorkflowExecution executeWorkflow(Long workflowId, Map<String, Object> inputData, Long userId) {
        try {
            // 1. 加载工作流定义
            Workflow workflow = workflowService.getById(workflowId);
            if (workflow == null) {
                throw new RuntimeException("工作流不存在: " + workflowId);
            }
            
            // 2. 创建执行记录
            WorkflowExecution execution = createExecution(workflow, inputData, userId);
            
            // 3. 异步执行工作流
            executeWorkflowAsync(execution);
            
            return execution;
            
        } catch (Exception e) {
            log.error("启动工作流执行失败: workflowId={}, error={}", workflowId, e.getMessage(), e);
            throw new RuntimeException("启动工作流执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建执行记录
     */
    private WorkflowExecution createExecution(Workflow workflow, Map<String, Object> inputData, Long userId) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflow(workflow);
        execution.setWorkflowVersion(workflow.getVersion());
        execution.setInputData(inputData);
        execution.setStatus(WorkflowExecution.ExecutionStatus.RUNNING);
        execution.setUser(workflow.getUser()); // 使用工作流所有者
        
        // 初始化执行上下文
        Map<String, Object> context = new HashMap<>();
        context.put("variables", new HashMap<String, Object>());
        context.put("inputData", inputData);
        execution.setContext(context);
        
        return executionRepository.save(execution);
    }
    
    /**
     * 异步执行工作流
     */
    @Async
    public CompletableFuture<Void> executeWorkflowAsync(WorkflowExecution execution) {
        try {
            log.info("开始执行工作流: executionId={}, workflowId={}", 
                    execution.getId(), execution.getWorkflow().getId());
            
            // 执行工作流
            executeNodes(execution);
            
            // 标记完成
            execution.setStatus(WorkflowExecution.ExecutionStatus.COMPLETED);
            execution.complete();
            executionRepository.save(execution);
            
            log.info("工作流执行完成: executionId={}, duration={}ms", 
                    execution.getId(), execution.getDurationMs());
            
        } catch (Exception e) {
            log.error("工作流执行失败: executionId={}, error={}", 
                    execution.getId(), e.getMessage(), e);
            
            // 标记失败
            execution.setStatus(WorkflowExecution.ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.complete();
            executionRepository.save(execution);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 执行工作流节点
     */
    private void executeNodes(WorkflowExecution execution) {
        Workflow.WorkflowDefinition definition = execution.getWorkflow().getDefinition();
        List<Workflow.WorkflowNode> nodes = definition.getNodes();
        List<Workflow.WorkflowEdge> edges = definition.getEdges();
        
        // 构建节点依赖图
        Map<String, List<String>> dependencies = buildDependencyGraph(nodes, edges);
        
        // 找到开始节点
        Workflow.WorkflowNode startNode = findStartNode(nodes);
        if (startNode == null) {
            throw new RuntimeException("未找到开始节点");
        }
        
        // 执行节点
        Set<String> executed = new HashSet<>();
        Queue<String> toExecute = new LinkedList<>();
        toExecute.offer(startNode.getId());
        
        while (!toExecute.isEmpty()) {
            String nodeId = toExecute.poll();
            
            if (executed.contains(nodeId)) {
                continue;
            }
            
            // 检查依赖是否都已执行
            List<String> deps = dependencies.get(nodeId);
            if (deps != null && !executed.containsAll(deps)) {
                // 重新加入队列等待依赖完成
                toExecute.offer(nodeId);
                continue;
            }
            
            // 执行节点
            Workflow.WorkflowNode node = findNodeById(nodes, nodeId);
            if (node != null) {
                NodeExecution nodeExecution = executeNode(node, execution);
                
                // 如果节点执行失败且不允许继续，则停止执行
                if (nodeExecution.getStatus() == NodeExecution.NodeExecutionStatus.FAILED) {
                    Boolean continueOnError = (Boolean) node.getConfig().get("continueOnError");
                    if (continueOnError == null || !continueOnError) {
                        throw new RuntimeException("节点执行失败: " + node.getName());
                    }
                }
                
                executed.add(nodeId);
                
                // 添加下游节点到执行队列
                List<String> nextNodes = getNextNodes(nodeId, edges);
                
                // 对于条件节点，需要根据条件结果决定执行路径
                if ("condition".equals(node.getType())) {
                    nextNodes = filterNextNodesByCondition(nodeExecution, nextNodes, edges);
                }
                
                toExecute.addAll(nextNodes);
            }
        }
    }
    
    /**
     * 执行单个节点
     */
    private NodeExecution executeNode(Workflow.WorkflowNode node, WorkflowExecution execution) {
        log.info("执行节点: nodeId={}, nodeType={}, nodeName={}", 
                node.getId(), node.getType(), node.getName());
        
        // 创建节点执行记录
        NodeExecution nodeExecution = new NodeExecution();
        nodeExecution.setExecution(execution);
        nodeExecution.setNodeId(node.getId());
        nodeExecution.setNodeType(node.getType());
        nodeExecution.setNodeName(node.getName());
        nodeExecution.setConfig(node.getConfig());
        nodeExecution.setInputData(node.getData());
        nodeExecution = nodeExecutionRepository.save(nodeExecution);
        
        try {
            // 开始执行
            nodeExecution.start();
            nodeExecutionRepository.save(nodeExecution);
            
            // 获取节点执行器
            NodeExecutor executor = nodeRegistry.getExecutor(node.getType());
            if (executor == null) {
                throw new RuntimeException("未找到节点执行器: " + node.getType());
            }
            
            // 准备执行上下文
            ExecutionContext context = executionContext.createContext(execution, nodeExecution);
            
            // 执行节点
            Map<String, Object> output = executor.execute(context);
            
            // 完成执行
            nodeExecution.complete(output);
            nodeExecutionRepository.save(nodeExecution);
            
            // 更新执行上下文
            updateExecutionContext(execution, nodeExecution, output);
            
            log.info("节点执行完成: nodeId={}, duration={}ms", 
                    node.getId(), nodeExecution.getDurationMs());
            
        } catch (Exception e) {
            log.error("节点执行失败: nodeId={}, error={}", node.getId(), e.getMessage(), e);
            
            // 标记失败
            nodeExecution.fail(e.getMessage());
            nodeExecutionRepository.save(nodeExecution);
        }
        
        return nodeExecution;
    }
    
    /**
     * 构建节点依赖图
     */
    private Map<String, List<String>> buildDependencyGraph(List<Workflow.WorkflowNode> nodes, 
                                                          List<Workflow.WorkflowEdge> edges) {
        Map<String, List<String>> dependencies = new HashMap<>();
        
        // 初始化所有节点
        for (Workflow.WorkflowNode node : nodes) {
            dependencies.put(node.getId(), new ArrayList<>());
        }
        
        // 构建依赖关系
        for (Workflow.WorkflowEdge edge : edges) {
            dependencies.get(edge.getTarget()).add(edge.getSource());
        }
        
        return dependencies;
    }
    
    /**
     * 查找开始节点
     */
    private Workflow.WorkflowNode findStartNode(List<Workflow.WorkflowNode> nodes) {
        return nodes.stream()
                .filter(node -> "start".equals(node.getType()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据ID查找节点
     */
    private Workflow.WorkflowNode findNodeById(List<Workflow.WorkflowNode> nodes, String nodeId) {
        return nodes.stream()
                .filter(node -> nodeId.equals(node.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取下游节点
     */
    private List<String> getNextNodes(String nodeId, List<Workflow.WorkflowEdge> edges) {
        return edges.stream()
                .filter(edge -> nodeId.equals(edge.getSource()))
                .map(Workflow.WorkflowEdge::getTarget)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据条件过滤下游节点
     */
    private List<String> filterNextNodesByCondition(NodeExecution nodeExecution, 
                                                   List<String> nextNodes, 
                                                   List<Workflow.WorkflowEdge> edges) {
        Map<String, Object> output = nodeExecution.getOutputData();
        Boolean conditionResult = (Boolean) output.get("result");
        
        if (conditionResult == null) {
            return Collections.emptyList();
        }
        
        // 根据条件结果过滤边
        return edges.stream()
                .filter(edge -> nodeExecution.getNodeId().equals(edge.getSource()))
                .filter(edge -> {
                    String edgeType = (String) edge.getData().get("condition");
                    return (conditionResult && "true".equals(edgeType)) || 
                           (!conditionResult && "false".equals(edgeType));
                })
                .map(Workflow.WorkflowEdge::getTarget)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新执行上下文
     */
    private void updateExecutionContext(WorkflowExecution execution, 
                                      NodeExecution nodeExecution, 
                                      Map<String, Object> output) {
        Map<String, Object> context = execution.getContext();
        Map<String, Object> variables = (Map<String, Object>) context.get("variables");
        
        // 将节点输出添加到变量中
        if (output != null) {
            String variableKey = nodeExecution.getNodeId() + "_output";
            variables.put(variableKey, output);
        }
        
        // 保存上下文
        execution.setContext(context);
        executionRepository.save(execution);
    }
    
    /**
     * 取消工作流执行
     */
    @Transactional
    public void cancelExecution(Long executionId) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("执行记录不存在: " + executionId));
        
        if (execution.getStatus() == WorkflowExecution.ExecutionStatus.RUNNING) {
            execution.setStatus(WorkflowExecution.ExecutionStatus.CANCELLED);
            execution.complete();
            executionRepository.save(execution);
            
            log.info("工作流执行已取消: executionId={}", executionId);
        }
    }
}