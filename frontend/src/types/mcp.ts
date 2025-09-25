export interface McpServiceCreateRequest {
  name: string;
  displayName: string;
  description?: string;
  category?: string;
  tags?: string[];
  repositoryUrl?: string;
  documentationUrl?: string;
  iconUrl?: string;
  priceModel: PriceModel;
  pricePerRequest?: number;
  version: string;
  changelog?: string;
  dockerImage: string;
  configSchema?: string;
  apiSpec?: string;
}

export interface McpServiceResponse {
  id: number;
  name: string;
  displayName: string;
  description?: string;
  category?: string;
  tags?: string[];
  authorId: number;
  authorName: string;
  repositoryUrl?: string;
  documentationUrl?: string;
  iconUrl?: string;
  priceModel: PriceModel;
  pricePerRequest?: number;
  status: ServiceStatus;
  featured: boolean;
  downloadsCount: number;
  ratingAverage: number;
  ratingCount: number;
  createdAt: string;
  updatedAt: string;
  
  // 最新版本信息
  latestVersion: string;
  latestVersionChangelog?: string;
  latestVersionCreatedAt: string;
  
  // 统计信息
  versionsCount: number;
  reviewsCount: number;
  deploymentsCount: number;
}

export enum PriceModel {
  FREE = 'FREE',
  PER_REQUEST = 'PER_REQUEST',
  SUBSCRIPTION = 'SUBSCRIPTION'
}

export enum ServiceStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  PUBLISHED = 'PUBLISHED',
  REJECTED = 'REJECTED',
  ARCHIVED = 'ARCHIVED'
}