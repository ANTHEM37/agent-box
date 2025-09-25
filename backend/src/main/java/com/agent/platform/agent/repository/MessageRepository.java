package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息数据访问接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    /**
     * 根据发送方实例ID查找消息
     */
    List<Message> findBySenderId(Long senderId);

    /**
     * 根据接收方实例ID查找消息
     */
    List<Message> findByReceiverId(Long receiverId);

    /**
     * 根据任务ID查找消息
     */
    List<Message> findByTaskId(Long taskId);

    /**
     * 根据消息类型查找消息
     */
    List<Message> findByType(Message.MessageType type);

    /**
     * 根据消息状态查找消息
     */
    List<Message> findByStatus(Message.MessageStatus status);

    /**
     * 根据消息状态统计消息数量
     */
    long countByStatus(Message.MessageStatus status);

    /**
     * 查找两个智能体实例之间的消息
     */
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :agent1Id AND m.receiver.id = :agent2Id) OR (m.sender.id = :agent2Id AND m.receiver.id = :agent1Id) ORDER BY m.sendTime ASC")
    List<Message> findConversationBetweenAgents(@Param("agent1Id") Long agent1Id, 
                                              @Param("agent2Id") Long agent2Id);

    /**
     * 查找未送达的消息
     */
    List<Message> findByStatusIn(List<Message.MessageStatus> statuses);

    /**
     * 查找需要回复的消息
     */
    List<Message> findByRequiresReplyTrueAndReplyToIsNull();

    /**
     * 查找指定时间范围内发送的消息
     */
    List<Message> findBySendTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定智能体实例的未读消息数量
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.status = 'DELIVERED'")
    long countUnreadMessages(@Param("receiverId") Long receiverId);

    /**
     * 查找最新的消息（按发送时间倒序）
     */
    @Query("SELECT m FROM Message m ORDER BY m.sendTime DESC")
    List<Message> findLatestMessages(@Param("limit") int limit);

    /**
     * 根据优先级查找高优先级消息
     */
    List<Message> findByPriorityLessThanEqualOrderBySendTimeAsc(Integer priority);

    /**
     * 查找过期未处理的消息
     */
    @Query("SELECT m FROM Message m WHERE m.sendTime < :expireTime AND m.status IN ('SENT', 'DELIVERED')")
    List<Message> findExpiredMessages(@Param("expireTime") LocalDateTime expireTime);
}