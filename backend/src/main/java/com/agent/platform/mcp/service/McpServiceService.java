package com.agent.platform.mcp.service;

import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.mcp.dto.McpServiceCreateRequest;
import com.agent.platform.mcp.dto.McpServiceResponse;
import com.agent.platform.mcp.entity.McpService;
import com.agent.platform.mcp.entity.ServiceVersion;
import com.agent.platform.mcp.repository.McpServiceRepository;
import com.agent.platform.mcp.repository.ServiceVersionRepository;
import com.agent.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP 服务业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServiceService {

    private final McpServiceRepository serviceRepository;
    private final ServiceVersionRepository versionRepository;

    /**
     * 创建 MCP 服务
     */
    @Transactional
    public McpServiceResponse createService(McpServiceCreateRequest request, User user) {
        // 检查服务名称是否已存在
        if (serviceRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("服务名称已存在");
        }

        // 创建服务
        McpService service = new McpService();
        service.setName(request.getName());
        service.setDisplayName(request.getDisplayName());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setTags(request.getTags());
        service.setAuthorId(user.getId());
        service.setRepositoryUrl(request.getRepositoryUrl());
        service.setDocumentationUrl(request.getDocumentationUrl());
        service.setIconUrl(request.getIconUrl());
        service.setPriceModel(request.getPriceModel());
        service.setPricePerRequest(request.getPricePerRequest());
        service.setStatus(McpService.ServiceStatus.DRAFT);

        service = serviceRepository.save(service);

        // 创建初始版本
        ServiceVersion version = new ServiceVersion();
        version.setService(service);
        version.setVersion(request.getVersion());
        version.setChangelog(request.getChangelog());
        version.setDockerImage(request.getDockerImage());
        version.setConfigSchema(request.getConfigSchema());
        version.setApiSpec(request.getApiSpec());
        version.setIsLatest(true);

        versionRepository.save(version);

        log.info("用户 {} 创建了 MCP 服务: {}", user.getId(), service.getName());
        return McpServiceResponse.fromEntity(service);
    }

    /**
     * 获取服务详情
     */
    public McpServiceResponse getService(Long id) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));
        
        McpServiceResponse response = McpServiceResponse.fromEntity(service);
        
        // 获取最新版本信息
        versionRepository.findLatestByServiceId(id).ifPresent(version -> {
            response.setLatestVersion(version.getVersion());
            response.setLatestVersionChangelog(version.getChangelog());
            response.setLatestVersionCreatedAt(version.getCreatedAt());
        });
        
        // 设置统计信息
        response.setVersionsCount(versionRepository.countByServiceId(id).intValue());
        
        return response;
    }

    /**
     * 更新服务
     */
    @Transactional
    public McpServiceResponse updateService(Long id, McpServiceCreateRequest request, User user) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 检查权限
        if (!service.getAuthorId().equals(user.getId())) {
            throw new BusinessException("无权限修改此服务");
        }

        // 更新服务信息
        service.setDisplayName(request.getDisplayName());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setTags(request.getTags());
        service.setRepositoryUrl(request.getRepositoryUrl());
        service.setDocumentationUrl(request.getDocumentationUrl());
        service.setIconUrl(request.getIconUrl());
        service.setPriceModel(request.getPriceModel());
        service.setPricePerRequest(request.getPricePerRequest());

        service = serviceRepository.save(service);

        log.info("用户 {} 更新了 MCP 服务: {}", user.getId(), service.getName());
        return McpServiceResponse.fromEntity(service);
    }

    /**
     * 发布服务
     */
    @Transactional
    public void publishService(Long id, User user) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 检查权限
        if (!service.getAuthorId().equals(user.getId())) {
            throw new BusinessException("无权限发布此服务");
        }

        // 检查是否有版本
        if (versionRepository.countByServiceId(id) == 0) {
            throw new BusinessException("服务必须至少有一个版本才能发布");
        }

        service.setStatus(McpService.ServiceStatus.PUBLISHED);
        serviceRepository.save(service);

        log.info("用户 {} 发布了 MCP 服务: {}", user.getId(), service.getName());
    }

    /**
     * 删除服务
     */
    @Transactional
    public void deleteService(Long id, User user) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 检查权限
        if (!service.getAuthorId().equals(user.getId())) {
            throw new BusinessException("无权限删除此服务");
        }

        serviceRepository.delete(service);

        log.info("用户 {} 删除了 MCP 服务: {}", user.getId(), service.getName());
    }

    /**
     * 获取已发布的服务列表
     */
    public Page<McpServiceResponse> getPublishedServices(Pageable pageable) {
        return serviceRepository.findPublishedServices(pageable)
                .map(McpServiceResponse::fromEntity);
    }

    /**
     * 搜索服务
     */
    public Page<McpServiceResponse> searchServices(String keyword, Pageable pageable) {
        return serviceRepository.searchServices(keyword, pageable)
                .map(McpServiceResponse::fromEntity);
    }

    /**
     * 根据分类获取服务
     */
    public Page<McpServiceResponse> getServicesByCategory(String category, Pageable pageable) {
        return serviceRepository.findByCategory(category, pageable)
                .map(McpServiceResponse::fromEntity);
    }

    /**
     * 根据标签获取服务
     */
    public Page<McpServiceResponse> getServicesByTag(String tag, Pageable pageable) {
        return serviceRepository.findByTag(tag, pageable)
                .map(McpServiceResponse::fromEntity);
    }

    /**
     * 获取精选服务
     */
    public List<McpServiceResponse> getFeaturedServices() {
        return serviceRepository.findFeaturedServices().stream()
                .map(McpServiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门服务
     */
    public Page<McpServiceResponse> getPopularServices(Pageable pageable) {
        return serviceRepository.findPopularServices(pageable)
                .map(McpServiceResponse::fromEntity);
    }

    /**
     * 获取高评分服务
     */
    public Page<McpServiceResponse> getHighRatedServices(Double minRating, Pageable pageable) {
        return serviceRepository.findHighRatedServices(minRating, pageable)
                .map(McpServiceResponse::fromEntity);
    }

    /**
     * 获取用户的服务
     */
    public List<McpServiceResponse> getUserServices(Long userId) {
        return serviceRepository.findByAuthorId(userId).stream()
                .map(McpServiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return serviceRepository.findAllCategories();
    }

    /**
     * 获取所有标签
     */
    public List<String> getAllTags() {
        return serviceRepository.findAllTags();
    }

    /**
     * 增加下载次数
     */
    @Transactional
    public void incrementDownloads(Long id) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));
        
        service.incrementDownloads();
        serviceRepository.save(service);
    }
}