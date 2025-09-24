// 知识库相关类型定义

export interface KnowledgeBase {
  id: number
  name: string
  description?: string
  embeddingModel: string
  chunkSize: number
  chunkOverlap: number
  status: 'ACTIVE' | 'INACTIVE' | 'PROCESSING' | 'ERROR'
  documentCount: number
  totalTokens: number
  createdAt: string
  updatedAt: string
}

export interface KnowledgeBaseCreateRequest {
  name: string
  description?: string
  embeddingModel?: string
  chunkSize?: number
  chunkOverlap?: number
}

export interface Document {
  id: number
  filename: string
  originalFilename: string
  fileSize: number
  fileType: string
  mimeType: string
  status: 'UPLOADED' | 'PROCESSING' | 'PROCESSED' | 'ERROR' | 'DELETED'
  createdAt: string
}

export interface SearchRequest {
  knowledgeBaseId: number
  query: string
  topK?: number
  threshold?: number
  includeMetadata?: boolean
}

export interface SearchResult {
  content: string
  score: number
  documentName?: string
  chunkIndex?: number
  metadata?: Record<string, any>
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}