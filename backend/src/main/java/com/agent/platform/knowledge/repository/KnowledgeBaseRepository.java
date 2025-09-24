package com.agent.platform.knowledge.repository;

import com.agent.platform.knowledge.entity.KnowledgeBase;
import com.agent.platform.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    /**
     * 根据用户查找知识库
     */
    Page<KnowledgeBase> findByUserAndStatus(User user, KnowledgeBase.KnowledgeBaseStatus status, Pageable pageable);

    /**
     * 根据用户查找所有知识库
     */
    List<KnowledgeBase> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 根据用户和名称查找知识库
     */
    Optional<KnowledgeBase> findByUserAndName(User user, String name);

    /**
     * 统计用户的知识库数量
     */
    long countByUser(User user);

    /**
     * 查找用户的活跃知识库
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.user = :user AND kb.status = 'ACTIVE'")
    List<KnowledgeBase> findActiveKnowledgeBasesByUser(@Param("user") User user);

    /**
     * 根据名称模糊查询用户的知识库
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.user = :user AND kb.name LIKE %:name%")
    List<KnowledgeBase> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
}