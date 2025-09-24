package com.agent.platform.mcp.service;

import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.mcp.dto.ServiceDeploymentRequest;
import com.agent.platform.mcp.dto.ServiceDeploymentResponse;
import com.agent.platform.mcp.entity.McpService;
import com.agent.platform.mcp.entity.ServiceDeployment;
import com.agent.platform.mcp.entity.ServiceInstance;
import com.agent.platform.mcp.entity.ServiceVersion;
import com.agent.platform.mcp.repository.McpServiceRepository;
import com.agent.platform.mcp.repository.ServiceDeploymentRepository;
import com.agent.platform.mcp.repository.ServiceInstanceRepository;
import com.agent.platform.mcp.repository.ServiceVersionRepository;
import com.agent.platform.mcp.deployment.DockerManager;
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
 * 部署服务业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentService {

    private final ServiceDeploymentRepository deploymentRepository;
    private final ServiceInstanceRepository instanceRepository;
    private final McpServiceRepository serviceRepository;
    private final ServiceVersionRepository versionRepository;
    private final DockerManager dockerManager;

    /**
     * 创建部署
     */
    @Transactional
    public ServiceDeploymentResponse createDeployment(ServiceDeploymentRequest request, User user) {
        // 验证服务和版本
        McpService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException("服务不存在"));
        
        ServiceVersion version = versionRepository.findById(request.getVersionId())
                .orElseThrow(() -> new BusinessException("版本不存在"));

        if (!version.getService().getId().equals(service.getId())) {
            throw new BusinessException("版本与服务不匹配");
        }

        // 检查部署名称是否重复
        if (deploymentRepository.findByDeploymentNameAndUserId(request.getDeploymentName(), user.getId()).isPresent()) {
            throw new BusinessException("部署名称已存在");
        }

        // 创建部署记录
        ServiceDeployment deployment = new ServiceDeployment();
        deployment.setService(service);
        deployment.setVersion(version);
        deployment.setUserId(user.getId());
        deployment.setDeploymentName(request.getDeploymentName());
        deployment.setConfig(request.getConfig());
        deployment.setReplicas(request.getReplicas());
        deployment.setCpuLimit(request.getCpuLimit());
        deployment.setMemoryLimit(request.getMemoryLimit());
        deployment.setAutoScaling(request.getAutoScaling());
        deployment.setMinReplicas(request.getMinReplicas());
        deployment.setMaxReplicas(request.getMaxReplicas());
        deployment.setStatus(ServiceDeployment.DeploymentStatus.DEPLOYING);

        deployment = deploymentRepository.save(deployment);

        // 异步执行部署
        deployAsync(deployment);

        log.info("用户 {} 创建了部署: {}", user.getId(), deployment.getDeploymentName());
        return ServiceDeploymentResponse.fromEntity(deployment);
    }

    /**
     * 异步部署服务
     */
    private void deployAsync(ServiceDeployment deployment) {
        try {
            // 启动 Docker 容器
            List<ServiceInstance> instances = dockerManager.deployService(deployment);
            
            // 保存实例信息
            instanceRepository.saveAll(instances);
            
            // 更新部署状态
            deployment.setStatus(ServiceDeployment.DeploymentStatus.RUNNING);
            deployment.setEndpointUrl(generateEndpointUrl(deployment));
            deploymentRepository.save(deployment);
            
            log.info("部署 {} 成功启动", deployment.getDeploymentName());
        } catch (Exception e) {
            log.error("部署 {} 失败: {}", deployment.getDeploymentName(), e.getMessage());
            deployment.setStatus(ServiceDeployment.DeploymentStatus.FAILED);
            deploymentRepository.save(deployment);
        }
    }

    /**
     * 生成服务端点URL
     */
    private String generateEndpointUrl(ServiceDeployment deployment) {
        return String.format("http://mcp-service-%d.local", deployment.getId());
    }

    /**
     * 获取部署详情
     */
    public ServiceDeploymentResponse getDeployment(Long id, User user) {
        ServiceDeployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署不存在"));

        // 检查权限
        if (!deployment.getUserId().equals(user.getId())) {
            throw new BusinessException("无权限访问此部署");
        }

        return ServiceDeploymentResponse.fromEntity(deployment);
    }

    /**
     * 获取用户的部署列表
     */
    public Page<ServiceDeploymentResponse> getUserDeployments(User user, Pageable pageable) {
        return deploymentRepository.findByUserId(user.getId(), pageable)
                .map(ServiceDeploymentResponse::fromEntity);
    }

    /**
     * 停止部署
     */
    @Transactional
    public void stopDeployment(Long id, User user) {
        ServiceDeployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署不存在"));

        // 检查权限
        if (!deployment.getUserId().equals(user.getId())) {
            throw new BusinessException("无权限操作此部署");
        }

        // 停止所有实例
        List<ServiceInstance> instances = instanceRepository.findByDeploymentId(id);
        for (ServiceInstance instance : instances) {
            try {
                dockerManager.stopContainer(instance.getContainerId());
                instance.setStatus(ServiceInstance.InstanceStatus.STOPPED);
                instanceRepository.save(instance);
            } catch (Exception e) {
                log.error("停止实例 {} 失败: {}", instance.getContainerId(), e.getMessage());
            }
        }

        deployment.setStatus(ServiceDeployment.DeploymentStatus.STOPPED);
        deploymentRepository.save(deployment);

        log.info("用户 {} 停止了部署: {}", user.getId(), deployment.getDeploymentName());
    }

    /**
     * 重启部署
     */
    @Transactional
    public void restartDeployment(Long id, User user) {
        ServiceDeployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署不存在"));

        // 检查权限
        if (!deployment.getUserId().equals(user.getId())) {
            throw new BusinessException("无权限操作此部署");
        }

        // 先停止再启动
        stopDeployment(id, user);
        
        deployment.setStatus(ServiceDeployment.DeploymentStatus.DEPLOYING);
        deploymentRepository.save(deployment);
        
        // 异步重新部署
        deployAsync(deployment);

        log.info("用户 {} 重启了部署: {}", user.getId(), deployment.getDeploymentName());
    }

    /**
     * 删除部署
     */
    @Transactional
    public void deleteDeployment(Long id, User user) {
        ServiceDeployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署不存在"));

        // 检查权限
        if (!deployment.getUserId().equals(user.getId())) {
            throw new BusinessException("无权限删除此部署");
        }

        // 先停止部署
        if (deployment.getStatus() == ServiceDeployment.DeploymentStatus.RUNNING) {
            stopDeployment(id, user);
        }

        // 删除所有实例
        List<ServiceInstance> instances = instanceRepository.findByDeploymentId(id);
        for (ServiceInstance instance : instances) {
            try {
                dockerManager.removeContainer(instance.getContainerId());
            } catch (Exception e) {
                log.error("删除容器 {} 失败: {}", instance.getContainerId(), e.getMessage());
            }
        }

        // 删除部署记录
        deploymentRepository.delete(deployment);

        log.info("用户 {} 删除了部署: {}", user.getId(), deployment.getDeploymentName());
    }

    /**
     * 扩缩容部署
     */
    @Transactional
    public void scaleDeployment(Long id, Integer replicas, User user) {
        ServiceDeployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署不存在"));

        // 检查权限
        if (!deployment.getUserId().equals(user.getId())) {
            throw new BusinessException("无权限操作此部署");
        }

        if (replicas < 1) {
            throw new BusinessException("副本数不能小于1");
        }

        List<ServiceInstance> currentInstances = instanceRepository.findRunningInstancesByDeploymentId(id);
        int currentReplicas = currentInstances.size();

        if (replicas > currentReplicas) {
            // 扩容：创建新实例
            int newInstances = replicas - currentReplicas;
            for (int i = 0; i < newInstances; i++) {
                try {
                    ServiceInstance instance = dockerManager.createInstance(deployment);
                    instanceRepository.save(instance);
                } catch (Exception e) {
                    log.error("创建新实例失败: {}", e.getMessage());
                }
            }
        } else if (replicas < currentReplicas) {
            // 缩容：停止多余实例
            int removeCount = currentReplicas - replicas;
            for (int i = 0; i < removeCount && i < currentInstances.size(); i++) {
                ServiceInstance instance = currentInstances.get(i);
                try {
                    dockerManager.stopContainer(instance.getContainerId());
                    instance.setStatus(ServiceInstance.InstanceStatus.STOPPED);
                    instanceRepository.save(instance);
                } catch (Exception e) {
                    log.error("停止实例 {} 失败: {}", instance.getContainerId(), e.getMessage());
                }
            }
        }

        deployment.setReplicas(replicas);
        deploymentRepository.save(deployment);

        log.info("用户 {} 将部署 {} 扩缩容至 {} 个副本", user.getId(), deployment.getDeploymentName(), replicas);
    }

    /**
     * 获取部署统计信息
     */
    public DeploymentStats getDeploymentStats(User user) {
        Long totalDeployments = deploymentRepository.countByUserId(user.getId());
        Long runningDeployments = deploymentRepository.countRunningDeploymentsByUserId(user.getId());
        
        return new DeploymentStats(totalDeployments, runningDeployments);
    }

    /**
     * 部署统计信息
     */
    public static class DeploymentStats {
        private final Long totalDeployments;
        private final Long runningDeployments;

        public DeploymentStats(Long totalDeployments, Long runningDeployments) {
            this.totalDeployments = totalDeployments;
            this.runningDeployments = runningDeployments;
        }

        public Long getTotalDeployments() { return totalDeployments; }
        public Long getRunningDeployments() { return runningDeployments; }
    }
}