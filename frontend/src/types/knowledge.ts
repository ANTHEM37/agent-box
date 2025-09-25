export interface KnowledgeBaseCreateRequest {
  name: string;
  description?: string;
  embeddingModel?: string;
  chunkSize?: number;
  chunkOverlap?: number;
}

export interface KnowledgeBaseResponse {
  id: number;
  name: string;
  description?: string;
  embeddingModel: string;
  chunkSize: number;
  chunkOverlap: number;
  status: KnowledgeBaseStatus;
  documentCount: number;
  totalTokens: number;
  createdAt: string;
  updatedAt: string;
}

export enum KnowledgeBaseStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  ARCHIVED = 'ARCHIVED'
}

export interface DocumentUploadResponse {
  id: number;
  filename: string;
  originalName: string;
  size: number;
  contentType: string;
  uploadTime: string;
  status: string;
}