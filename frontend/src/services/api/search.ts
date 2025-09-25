import http from '../http/client'

export interface SearchRequest {
  knowledgeBaseId: number
  query: string
  limit?: number
  threshold?: number
}

export interface SearchResult {
  id?: string
  content: string
  score: number
  source?: string
}

type ApiResponse<T> = { code: number; message: string; data: T }

export const searchApi = {
  async semantic(req: SearchRequest) {
    const resp = await http.post<ApiResponse<SearchResult[]>>('/search/semantic', req)
    return resp.data.data
  },

  async hybrid(req: SearchRequest) {
    const resp = await http.post<ApiResponse<SearchResult[]>>('/search/hybrid', req)
    return resp.data.data
  },

  async context(knowledgeBaseId: number, query: string, maxTokens = 2000) {
    const resp = await http.get<ApiResponse<string>>('/search/context', {
      params: { knowledgeBaseId, query, maxTokens }
    })
    return resp.data.data
  },
}


