import { httpClient } from '../http/client'
import type { 
  KnowledgeBase, 
  KnowledgeBaseCreateRequest, 
  Document, 
  SearchRequest, 
  SearchResult,
  PageResponse 
} from '../../types/knowledge'

export const knowledgeApi = {
  // 知识库管理
  async getKnowledgeBases(page = 0, size = 10): Promise<PageResponse<KnowledgeBase>> {
    const response = await httpClient.get('/knowledge-bases', {
      params: { page, size }
    })
    return response.data.data
  },

  async createKnowledgeBase(data: KnowledgeBaseCreateRequest): Promise<KnowledgeBase> {
    const response = await httpClient.post('/knowledge-bases', data)
    return response.data.data
  },

  async getKnowledgeBase(id: number): Promise<KnowledgeBase> {
    const response = await httpClient.get(`/knowledge-bases/${id}`)
    return response.data.data
  },

  async updateKnowledgeBase(id: number, data: KnowledgeBaseCreateRequest): Promise<KnowledgeBase> {
    const response = await httpClient.put(`/knowledge-bases/${id}`, data)
    return response.data.data
  },

  async deleteKnowledgeBase(id: number): Promise<void> {
    await httpClient.delete(`/knowledge-bases/${id}`)
  },

  async searchKnowledgeBases(keyword: string): Promise<KnowledgeBase[]> {
    const response = await httpClient.get('/knowledge-bases/search', {
      params: { keyword }
    })
    return response.data.data
  },

  async getActiveKnowledgeBases(): Promise<KnowledgeBase[]> {
    const response = await httpClient.get('/knowledge-bases/active')
    return response.data.data
  },

  // 文档管理
  async uploadDocument(knowledgeBaseId: number, file: File): Promise<Document> {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await httpClient.post(
      `/knowledge-bases/${knowledgeBaseId}/documents/upload`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )
    return response.data.data
  },

  async processDocument(knowledgeBaseId: number, documentId: number): Promise<void> {
    await httpClient.post(`/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/process`)
  },

  async getDocuments(knowledgeBaseId: number, page = 0, size = 10): Promise<PageResponse<Document>> {
    const response = await httpClient.get(`/knowledge-bases/${knowledgeBaseId}/documents`, {
      params: { page, size }
    })
    return response.data.data
  },

  async deleteDocument(knowledgeBaseId: number, documentId: number): Promise<void> {
    await httpClient.delete(`/knowledge-bases/${knowledgeBaseId}/documents/${documentId}`)
  },

  // 搜索功能
  async semanticSearch(request: SearchRequest): Promise<SearchResult[]> {
    const response = await httpClient.post('/search/semantic', request)
    return response.data.data
  },

  async hybridSearch(request: SearchRequest): Promise<SearchResult[]> {
    const response = await httpClient.post('/search/hybrid', request)
    return response.data.data
  },

  async getContextForRAG(knowledgeBaseId: number, query: string, maxTokens = 2000): Promise<string> {
    const response = await httpClient.get('/search/context', {
      params: { knowledgeBaseId, query, maxTokens }
    })
    return response.data.data
  }
}