package com.agent.platform.agent.repository;

import com.agent.platform.agent.entity.AgentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 智能体定义数据访问接口
 */
@Repository
public interface AgentDefinitionRepository extends JpaRepository<AgentDefinition, Long>, JpaSpecificationExecutor<AgentDefinition> {

    /**
     * 根据名称查找智能体定义
     */
    Optional<AgentDefinition> findByName(String name);

    /**
     * 根据名称和版本查找智能体定义
     */
    Optional<AgentDefinition> findByNameAndVersion(String name, String version);

    /**
     * 查找启用的智能体定义
     */
    List<AgentDefinition> findByEnabledTrue();

    /**
     * 根据类型查找智能体定义
     */
    List<AgentDefinition> findByType(AgentDefinition.AgentType type);

    /**
     * 根据名称模糊搜索
     */
    @Query("SELECT a FROM AgentDefinition a WHERE a.name LIKE %:name%")
    List<AgentDefinition> findByNameContaining(@Param("name") String name);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查名称和版本是否存在
     */
    boolean existsByNameAndVersion(String name, String version);
}