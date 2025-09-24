package com.agent.platform.knowledge.service;

import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.knowledge.dto.KnowledgeBaseCreateRequest;
import com.agent.platform.knowledge.dto.KnowledgeBaseResponse;
import com.agent.platform.knowledge.entity.KnowledgeBase;
import com.agent.platform.knowledge.repository.KnowledgeBaseRepository;
import com.agent.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    /**
     * 创建知识库
     */
    @Transactional
    public KnowledgeBaseResponse createKnowledgeBase(User user, KnowledgeBaseCreateRequest request) {
        // 检查名称是否已存在
        if (knowledgeBaseRepository.findByUserAndName(user, request.getName()).isPresent()) {
            throw new BusinessException("知识库名称已存在");
        }

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(request.getName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setUser(user);
        knowledgeBase.setEmbeddingModel(request.getEmbeddingModel());
        knowledgeBase.setChunkSize(request.getChunkSize());
        knowledgeBase.setChunkOverlap(request.getChunkOverlap());
        knowledgeBase.setStatus(KnowledgeBase.KnowledgeBaseStatus.ACTIVE);

        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        
        log.info("用户 {} 创建知识库: {}", user.getUsername(), knowledgeBase.getName());
        return KnowledgeBaseResponse.from(knowledgeBase);
    }

    /**
     * 获取用户的知识库列表
     */
    public Page<KnowledgeBaseResponse> getUserKnowledgeBases(User user, Pageable pageable) {
        Page<KnowledgeBase> knowledgeBases = knowledgeBaseRepository.findByUserAndStatus(
            user, KnowledgeBase.KnowledgeBaseStatus.ACTIVE, pageable);
        
        return knowledgeBases.map(KnowledgeBaseResponse::from);
    }

    /**
     * 获取知识库详情
     */
    public KnowledgeBaseResponse getKnowledgeBase(User user, Long knowledgeBaseId) {
        KnowledgeBase knowledgeBase = findKnowledgeBaseByUserAndId(user, knowledgeBaseId);
        return KnowledgeBaseResponse.from(knowledgeBase);
    }

    /**
     * 更新知识库
     */
    @Transactional
    public KnowledgeBaseResponse updateKnowledgeBase(User user, Long knowledgeBaseId, 
                                                   KnowledgeBaseCreateRequest request) {
        KnowledgeBase knowledgeBase = findKnowledgeBaseByUserAndId(user, knowledgeBaseId);

        // 检查名称是否与其他知识库冲突
        knowledgeBaseRepository.findByUserAndName(user, request.getName())
            .ifPresent(existing -> {
                if (!existing.getId().equals(knowledgeBaseId)) {
                    throw new BusinessException("知识库名称已存在");
                }
            });

        knowledgeBase.setName(request.getName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setEmbeddingModel(request.getEmbeddingModel());
        knowledgeBase.setChunkSize(request.getChunkSize());
        knowledgeBase.setChunkOverlap(request.getChunkOverlap());

        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        
        log.info("用户 {} 更新知识库: {}", user.getUsername(), knowledgeBase.getName());
        return KnowledgeBaseResponse.from(knowledgeBase);
    }

    /**
     * 删除知识库
     */
    @Transactional
    public void deleteKnowledgeBase(User user, Long knowledgeBaseId) {
        KnowledgeBase knowledgeBase = findKnowledgeBaseByUserAndId(user, knowledgeBaseId);
        
        // 软删除：设置状态为INACTIVE
        knowledgeBase.setStatus(KnowledgeBase.KnowledgeBaseStatus.INACTIVE);
        knowledgeBaseRepository.save(knowledgeBase);
        
        log.info("用户 {} 删除知识库: {}", user.getUsername(), knowledgeBase.getName());
    }

    /**
     * 更新知识库统计信息
     */
    @Transactional
    public void updateKnowledgeBaseStats(Long knowledgeBaseId, int documentCount, long totalTokens) {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            .orElseThrow(() -> new BusinessException("知识库不存在"));
        
        knowledgeBase.setDocumentCount(documentCount);
        knowledgeBase.setTotalTokens(totalTokens);
        knowledgeBaseRepository.save(knowledgeBase);
    }

    /**
     * 根据用户和ID查找知识库
     */
    public KnowledgeBase findKnowledgeBaseByUserAndId(User user, Long knowledgeBaseId) {
        return knowledgeBaseRepository.findById(knowledgeBaseId)
            .filter(kb -> kb.getUser().getId().equals(user.getId()) && 
                         kb.getStatus() == KnowledgeBase.KnowledgeBaseStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException("知识库不存在或无权限访问"));
    }

    /**
     * 搜索用户的知识库
     */
    public List<KnowledgeBaseResponse> searchKnowledgeBases(User user, String keyword) {
        List<KnowledgeBase> knowledgeBases = knowledgeBaseRepository
            .findByUserAndNameContaining(user, keyword);
        
        return knowledgeBases.stream()
            .map(KnowledgeBaseResponse::from)
            .toList();
    }

    /**
     * 获取用户的活跃知识库
     */
    public List<KnowledgeBaseResponse> getActiveKnowledgeBases(User user) {
        List<KnowledgeBase> knowledgeBases = knowledgeBaseRepository
            .findActiveKnowledgeBasesByUser(user);
        
        return knowledgeBases.stream()
            .map(KnowledgeBaseResponse::from)
            .toList();
    }
}