package com.agent.platform.workflow.service;

import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.workflow.entity.Workflow;
import com.agent.platform.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    
    /**
     * 创建工作流
     */
    @Transactional
    public Workflow create(Workflow workflow) {
        // 验证工作流名称唯一性
        if (workflowRepository.existsByUserIdAndName(workflow.getUser().getId(), workflow.getName())) {
            throw new BusinessException("工作流名称已存在");
        }
        
        // 验证工作流定义
        validateWorkflowDefinition(workflow.getDefinition());
        
        return workflowRepository.save(workflow);
    }
    
    /**
     * 更新工作流
     */
    @Transactional
    public Workflow update(Long id, Workflow workflow) {
        Workflow existing = getById(id);
        
        // 验证权限
        if (!existing.getUser().getId().equals(workflow.getUser().getId())) {
            throw new BusinessException("无权限修改此工作流");
        }
        
        // 验证名称唯一性（排除自己）
        if (workflowRepository.existsByUserIdAndNameAndIdNot(
                workflow.getUser().getId(), workflow.getName(), id)) {
            throw new BusinessException("工作流名称已存在");
        }
        
        // 验证工作流定义
        validateWorkflowDefinition(workflow.getDefinition());
        
        // 更新字段
        existing.setName(workflow.getName());
        existing.setDescription(workflow.getDescription());
        existing.setDefinition(workflow.getDefinition());
        existing.setCategory(workflow.getCategory());
        existing.setTags(workflow.getTags());
        existing.setStatus(workflow.getStatus());
        
        // 如果是发布状态，增加版本号
        if (workflow.getStatus() == Workflow.WorkflowStatus.PUBLISHED) {
            existing.setVersion(existing.getVersion() + 1);
        }
        
        return workflowRepository.save(existing);
    }
    
    /**
     * 根据ID获取工作流
     */
    public Workflow getById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工作流不存在"));
    }
    
    /**
     * 删除工作流
     */
    @Transactional
    public void delete(Long id, Long userId) {
        Workflow workflow = getById(id);
        
        // 验证权限
        if (!workflow.getUser().getId().equals(userId)) {
            throw new BusinessException("无权限删除此工作流");
        }
        
        workflowRepository.delete(workflow);
        log.info("删除工作流: id={}, name={}", id, workflow.getName());
    }
    
    /**
     * 获取用户的工作流列表
     */
    public Page<Workflow> getUserWorkflows(Long userId, Pageable pageable) {
        return workflowRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }
    
    /**
     * 根据状态获取工作流列表
     */
    public Page<Workflow> getUserWorkflowsByStatus(Long userId, Workflow.WorkflowStatus status, Pageable pageable) {
        return workflowRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, status, pageable);
    }
    
    /**
     * 根据分类获取工作流列表
     */
    public Page<Workflow> getUserWorkflowsByCategory(Long userId, String category, Pageable pageable) {
        return workflowRepository.findByUserIdAndCategoryOrderByUpdatedAtDesc(userId, category, pageable);
    }
    
    /**
     * 搜索工作流 - 暂时使用简单实现
     */
    public Page<Workflow> searchWorkflows(Long userId, String keyword, Pageable pageable) {
        // 暂时返回所有用户工作流，后续实现搜索功能
        return workflowRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }
    
    /**
     * 根据标签获取工作流 - 暂时使用简单实现
     */
    public Page<Workflow> getUserWorkflowsByTag(Long userId, String tag, Pageable pageable) {
        // 暂时返回所有用户工作流，后续实现标签搜索功能
        return workflowRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取模板工作流 - 暂时返回空结果
     */
    public Page<Workflow> getTemplates(Pageable pageable) {
        // 暂时返回空结果，后续实现模板功能
        return Page.empty(pageable);
    }
    
    /**
     * 根据分类获取模板 - 暂时返回空结果
     */
    public Page<Workflow> getTemplatesByCategory(String category, Pageable pageable) {
        // 暂时返回空结果，后续实现模板功能
        return Page.empty(pageable);
    }
    
    /**
     * 获取所有分类 - 暂时返回空列表
     */
    public List<String> getAllCategories() {
        // 暂时返回空列表，后续实现分类功能
        return List.of();
    }
    
    /**
     * 获取用户的分类 - 暂时返回空列表
     */
    public List<String> getUserCategories(Long userId) {
        // 暂时返回空列表，后续实现分类功能
        return List.of();
    }
    
    /**
     * 获取用户工作流统计 - 暂时返回简单统计
     */
    public Map<String, Long> getUserWorkflowStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        // 暂时使用简单实现
        List<Workflow> userWorkflows = workflowRepository.findByUserIdOrderByUpdatedAtDesc(userId, Pageable.unpaged()).getContent();
        stats.put("total", (long) userWorkflows.size());
        stats.put("draft", userWorkflows.stream().filter(w -> w.getStatus() == Workflow.WorkflowStatus.DRAFT).count());
        stats.put("published", userWorkflows.stream().filter(w -> w.getStatus() == Workflow.WorkflowStatus.PUBLISHED).count());
        stats.put("archived", userWorkflows.stream().filter(w -> w.getStatus() == Workflow.WorkflowStatus.ARCHIVED).count());
        return stats;
    }
    
    /**
     * 发布工作流
     */
    @Transactional
    public Workflow publish(Long id, Long userId) {
        Workflow workflow = getById(id);
        
        // 验证权限
        if (!workflow.getUser().getId().equals(userId)) {
            throw new BusinessException("无权限发布此工作流");
        }
        
        // 验证工作流定义
        validateWorkflowDefinition(workflow.getDefinition());
        
        // 更新状态和版本
        workflow.setStatus(Workflow.WorkflowStatus.PUBLISHED);
        workflow.setVersion(workflow.getVersion() + 1);
        
        return workflowRepository.save(workflow);
    }
    
    /**
     * 归档工作流
     */
    @Transactional
    public Workflow archive(Long id, Long userId) {
        Workflow workflow = getById(id);
        
        // 验证权限
        if (!workflow.getUser().getId().equals(userId)) {
            throw new BusinessException("无权限归档此工作流");
        }
        
        workflow.setStatus(Workflow.WorkflowStatus.ARCHIVED);
        return workflowRepository.save(workflow);
    }
    
    /**
     * 复制工作流
     */
    @Transactional
    public Workflow copy(Long id, Long userId, String newName) {
        Workflow original = getById(id);
        
        // 验证新名称唯一性
        if (workflowRepository.existsByUserIdAndName(userId, newName)) {
            throw new BusinessException("工作流名称已存在");
        }
        
        // 创建副本
        Workflow copy = new Workflow();
        copy.setName(newName);
        copy.setDescription(original.getDescription() + " (副本)");
        copy.setDefinition(original.getDefinition());
        copy.setCategory(original.getCategory());
        copy.setTags(original.getTags());
        copy.setStatus(Workflow.WorkflowStatus.DRAFT);
        copy.setVersion(1);
        copy.setUser(original.getUser()); // 设置用户
        
        return workflowRepository.save(copy);
    }
    
    /**
     * 验证工作流定义
     */
    private void validateWorkflowDefinition(Workflow.WorkflowDefinition definition) {
        if (definition == null) {
            throw new BusinessException("工作流定义不能为空");
        }
        
        if (definition.getNodes() == null || definition.getNodes().isEmpty()) {
            throw new BusinessException("工作流必须包含至少一个节点");
        }
        
        // 检查是否有开始节点
        boolean hasStartNode = definition.getNodes().stream()
                .anyMatch(node -> "start".equals(node.getType()));
        if (!hasStartNode) {
            throw new BusinessException("工作流必须包含一个开始节点");
        }
        
        // 检查是否有结束节点
        boolean hasEndNode = definition.getNodes().stream()
                .anyMatch(node -> "end".equals(node.getType()));
        if (!hasEndNode) {
            throw new BusinessException("工作流必须包含一个结束节点");
        }
        
        // 验证节点ID唯一性
        Set<String> nodeIds = new HashSet<>();
        for (Workflow.WorkflowNode node : definition.getNodes()) {
            if (nodeIds.contains(node.getId())) {
                throw new BusinessException("节点ID重复: " + node.getId());
            }
            nodeIds.add(node.getId());
        }
        
        // 验证连接的有效性
        if (definition.getEdges() != null) {
            for (Workflow.WorkflowEdge edge : definition.getEdges()) {
                if (!nodeIds.contains(edge.getSource())) {
                    throw new BusinessException("连接的源节点不存在: " + edge.getSource());
                }
                if (!nodeIds.contains(edge.getTarget())) {
                    throw new BusinessException("连接的目标节点不存在: " + edge.getTarget());
                }
            }
        }
    }
}