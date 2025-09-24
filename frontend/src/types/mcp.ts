// MCP 服务市场类型定义

export interface McpService {
  id: number;
  name: string;
  displayName: string;
  description?: string;
  category?: string;
  tags: string[];
  authorId: number;
  authorName?: string;
  repositoryUrl?: string;
  documentationUrl?: string;
  iconUrl?: string;
  priceModel: 'FREE' | 'PAID' | 'FREEMIUM';
  pricePerRequest?: number;
  status: 'DRAFT' | 'PUBLISHED' | 'DEPRECATED';
  featured: boolean;
  downloadsCount: number;
  ratingAverage: number;
  ratingCount: number;
  createdAt: string;
  updatedAt: string;
  
  // 最新版本信息
  latestVersion?: string;
  latestVersionChangelog?: string;
  latestVersionCreatedAt?: string;
  
  // 统计信息
  versionsCount?: number;
  reviewsCount?: number;
  deploymentsCount?: number;
}

export interface ServiceVersion {
  id: number;
  serviceId: number;
  version: string;
  changelog?: string;
  dockerImage: string;
  configSchema?: string;
  apiSpec?: string;
  isLatest: boolean;
  downloadCount: number;
  sizeMb?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceDeployment {
  id: number;
  serviceId: number;
  serviceName: string;
  serviceDisplayName: string;
  versionId: number;
  version: string;
  userId: number;
  deploymentName: string;
  config?: string;
  status: 'DEPLOYING' | 'RUNNING' | 'STOPPED' | 'FAILED' | 'UPDATING';
  endpointUrl?: string;
  replicas: number;
  cpuLimit?: string;
  memoryLimit?: string;
  autoScaling: boolean;
  minReplicas: number;
  maxReplicas: number;
  createdAt: string;
  updatedAt: string;
  
  // 实例信息
  instances?: ServiceInstance[];
  runningInstancesCount?: number;
  totalInstancesCount?: number;
  
  // 统计信息
  totalRequests?: number;
  totalErrors?: number;
  errorRate?: number;
  avgResponseTime?: number;
}

export interface ServiceInstance {
  id: number;
  deploymentId: number;
  containerId?: string;
  host?: string;
  port?: number;
  status: 'STARTING' | 'RUNNING' | 'STOPPING' | 'STOPPED' | 'FAILED' | 'UNHEALTHY';
  healthCheckUrl?: string;
  lastHealthCheck?: string;
  cpuUsage?: number;
  memoryUsage?: number;
  requestCount: number;
  errorCount: number;
  avgResponseTime?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceReview {
  id: number;
  serviceId: number;
  userId: number;
  userName?: string;
  userAvatar?: string;
  rating: number;
  comment?: string;
  helpfulCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceUsage {
  id: number;
  deploymentId: number;
  userId: number;
  requestCount: number;
  responseTimeAvg?: number;
  errorCount: number;
  dataTransferMb: number;
  cost: number;
  date: string;
  createdAt: string;
  updatedAt: string;
}

// 请求类型
export interface McpServiceCreateRequest {
  name: string;
  displayName: string;
  description?: string;
  category?: string;
  tags?: string[];
  repositoryUrl?: string;
  documentationUrl?: string;
  iconUrl?: string;
  priceModel: 'FREE' | 'PAID' | 'FREEMIUM';
  pricePerRequest?: number;
  
  // 初始版本信息
  version: string;
  changelog?: string;
  dockerImage: string;
  configSchema?: string;
  apiSpec?: string;
}

export interface ServiceDeploymentRequest {
  serviceId: number;
  versionId: number;
  deploymentName: string;
  config?: string;
  replicas?: number;
  cpuLimit?: string;
  memoryLimit?: string;
  autoScaling?: boolean;
  minReplicas?: number;
  maxReplicas?: number;
}

export interface ServiceReviewRequest {
  serviceId: number;
  rating: number;
  comment?: string;
}

// 搜索和过滤
export interface ServiceSearchParams {
  keyword?: string;
  category?: string;
  tag?: string;
  priceModel?: 'FREE' | 'PAID' | 'FREEMIUM';
  minRating?: number;
  sortBy?: string;
  sortOrder?: string;
  page?: number;
  size?: number;
}

// 统计信息
export interface ServiceStats {
  totalServices: number;
  publishedServices: number;
  totalDownloads: number;
  averageRating: number;
  categoriesCount: number;
  tagsCount: number;
}

export interface DeploymentStats {
  totalDeployments: number;
  runningDeployments: number;
  stoppedDeployments: number;
  failedDeployments: number;
  totalInstances: number;
  runningInstances: number;
}

export interface UsageStats {
  totalRequests: number;
  totalErrors: number;
  errorRate: number;
  avgResponseTime: number;
  totalCost: number;
  dataTransfer: number;
}

// 监控数据
export interface MonitoringData {
  timestamp: string;
  cpuUsage: number;
  memoryUsage: number;
  requestCount: number;
  errorCount: number;
  responseTime: number;
}

// 服务配置
export interface ServiceConfig {
  [key: string]: any;
}

// API 规范
export interface ApiSpec {
  openapi: string;
  info: {
    title: string;
    version: string;
    description?: string;
  };
  paths: {
    [path: string]: any;
  };
  components?: {
    schemas?: {
      [schema: string]: any;
    };
  };
}