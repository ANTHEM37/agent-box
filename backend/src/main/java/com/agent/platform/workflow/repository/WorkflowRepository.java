package com.agent.platform.workflow.repository;

import com.agent.platform.workflow.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    // 根据用户ID查找工作流
    Page<Workflow> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    
    // 根据状态查找工作流
    Page<Workflow> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, Workflow.WorkflowStatus status, Pageable pageable);
    
    // 根据分类查找工作流
    Page<Workflow> findByUserIdAndCategoryOrderByUpdatedAtDesc(Long userId, String category, Pageable pageable);
    
    // 检查名称唯一性
    boolean existsByUserIdAndName(Long userId, String name);
    
    // 检查名称唯一性（排除指定ID）
    boolean existsByUserIdAndNameAndIdNot(Long userId, String name, Long id);
    
    // 搜索工作流（按名称和描述）
    @Query("SELECT w FROM Workflow w WHERE w.user.id = :userId AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Workflow> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
    

    
    // 查找模板工作流
    Page<Workflow> findByIsTemplateTrueOrderByUpdatedAtDesc(Pageable pageable);
    
    // 查找公共模板
    @Query("SELECT w FROM Workflow w WHERE w.isTemplate = true AND w.status = 'PUBLISHED'")
    Page<Workflow> findPublicTemplates(Pageable pageable);
    
    // 根据分类查找模板
    @Query("SELECT w FROM Workflow w WHERE w.isTemplate = true AND w.category = :category AND w.status = 'PUBLISHED'")
    Page<Workflow> findTemplatesByCategory(@Param("category") String category, Pageable pageable);
    
    // 获取用户的工作流统计
    @Query("SELECT COUNT(w) FROM Workflow w WHERE w.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(w) FROM Workflow w WHERE w.user.id = :userId AND w.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Workflow.WorkflowStatus status);
    
    // 获取所有分类
    @Query("SELECT DISTINCT w.category FROM Workflow w WHERE w.category IS NOT NULL ORDER BY w.category")
    List<String> findAllCategories();
    
    // 获取用户的所有分类
    @Query("SELECT DISTINCT w.category FROM Workflow w WHERE w.user.id = :userId AND w.category IS NOT NULL ORDER BY w.category")
    List<String> findCategoriesByUserId(@Param("userId") Long userId);
    
    // 检查工作流名称是否存在
    boolean existsByUser_IdAndNameAndIdNot(Long userId, String name, Long id);
    
    boolean existsByUser_IdAndName(Long userId, String name);
}