package com.agent.platform.mcp.repository;

import com.agent.platform.mcp.entity.ServiceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 服务评论仓库接口
 */
@Repository
public interface ServiceReviewRepository extends JpaRepository<ServiceReview, Long> {

    /**
     * 根据服务ID查找评论
     */
    Page<ServiceReview> findByServiceIdOrderByCreatedAtDesc(Long serviceId, Pageable pageable);

    /**
     * 根据用户ID查找评论
     */
    List<ServiceReview> findByUserId(Long userId);

    /**
     * 根据服务ID和用户ID查找评论
     */
    Optional<ServiceReview> findByServiceIdAndUserId(Long serviceId, Long userId);

    /**
     * 根据评分查找评论
     */
    List<ServiceReview> findByServiceIdAndRating(Long serviceId, Integer rating);

    /**
     * 统计服务的评论数量
     */
    Long countByServiceId(Long serviceId);

    /**
     * 计算服务的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM ServiceReview r WHERE r.service.id = :serviceId")
    Double calculateAverageRating(@Param("serviceId") Long serviceId);

    /**
     * 统计服务各评分的数量
     */
    @Query("SELECT r.rating, COUNT(r) FROM ServiceReview r WHERE r.service.id = :serviceId GROUP BY r.rating")
    List<Object[]> countRatingsByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 查找最有用的评论
     */
    @Query("SELECT r FROM ServiceReview r WHERE r.service.id = :serviceId ORDER BY r.helpfulCount DESC")
    Page<ServiceReview> findMostHelpfulReviews(@Param("serviceId") Long serviceId, Pageable pageable);
}