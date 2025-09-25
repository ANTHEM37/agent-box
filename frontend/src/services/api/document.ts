import http from '../http/client'

export interface DocumentItem {
  id: number
  fileName: string
  fileType?: string
  size?: number
  status?: string
  createdAt?: string
}

export interface PageResp<T> { content: T[]; totalElements: number }
type ApiResponse<T> = { code: number; message: string; data: T }

export const documentApi = {
  async upload(knowledgeBaseId: number, file: File) {
    const form = new FormData()
    form.append('file', file)
    const resp = await http.post<ApiResponse<DocumentItem>>(
      `/knowledge-bases/${knowledgeBaseId}/documents/upload`, form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    )
    return resp.data.data
  },

  async process(knowledgeBaseId: number, documentId: number) {
    await http.post<ApiResponse<void>>(`/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/process`)
  },

  async list(knowledgeBaseId: number, page = 0, size = 10) {
    const resp = await http.get<ApiResponse<PageResp<DocumentItem>>>(
      `/knowledge-bases/${knowledgeBaseId}/documents`, { params: { page, size } }
    )
    return resp.data.data
  },

  async remove(knowledgeBaseId: number, documentId: number) {
    await http.delete<ApiResponse<void>>(`/knowledge-bases/${knowledgeBaseId}/documents/${documentId}`)
  },
}


