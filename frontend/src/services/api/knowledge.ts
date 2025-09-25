import http from '../http/client'

export interface KnowledgeBasePayload {
  name: string
  description?: string
}

export interface KnowledgeBaseResponse {
  id: number
  name: string
  description?: string
  createdAt?: string
}

export interface PageResp<T> { content: T[]; totalElements: number }
type ApiResponse<T> = { code: number; message: string; data: T }

export const knowledgeApi = {
  async list(page = 0, size = 10): Promise<PageResp<KnowledgeBaseResponse>> {
    const resp = await http.get<ApiResponse<PageResp<KnowledgeBaseResponse>>>(
      `/knowledge-bases`, { params: { page, size } }
    )
    return resp.data.data
  },

  async search(keyword: string): Promise<KnowledgeBaseResponse[]> {
    const resp = await http.get<ApiResponse<KnowledgeBaseResponse[]>>(
      `/knowledge-bases/search`, { params: { keyword } }
    )
    return resp.data.data
  },

  async create(payload: KnowledgeBasePayload): Promise<KnowledgeBaseResponse> {
    const resp = await http.post<ApiResponse<KnowledgeBaseResponse>>(`/knowledge-bases`, payload)
    return resp.data.data
  },

  async update(id: number, payload: KnowledgeBasePayload): Promise<KnowledgeBaseResponse> {
    const resp = await http.put<ApiResponse<KnowledgeBaseResponse>>(`/knowledge-bases/${id}`, payload)
    return resp.data.data
  },

  async remove(id: number): Promise<void> {
    await http.delete<ApiResponse<void>>(`/knowledge-bases/${id}`)
  },

  async get(id: number): Promise<KnowledgeBaseResponse> {
    const resp = await http.get<ApiResponse<KnowledgeBaseResponse>>(`/knowledge-bases/${id}`)
    return resp.data.data
  },

  async active(): Promise<KnowledgeBaseResponse[]> {
    const resp = await http.get<ApiResponse<KnowledgeBaseResponse[]>>(`/knowledge-bases/active`)
    return resp.data.data
  },
}


