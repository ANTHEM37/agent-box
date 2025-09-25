import apiClient from '../http/client';
import { ApiResponse, Page } from '../../types/common';
import { KnowledgeBaseCreateRequest, KnowledgeBaseResponse } from '../../types/knowledge';

// 创建知识库
export const createKnowledgeBase = async (data: KnowledgeBaseCreateRequest): Promise<ApiResponse<KnowledgeBaseResponse>> => {
  const response = await apiClient.post<ApiResponse<KnowledgeBaseResponse>>('/knowledge-bases', data);
  return response.data;
};

// 获取知识库列表
export const getKnowledgeBases = async (page: number = 0, size: number = 10): Promise<ApiResponse<Page<KnowledgeBaseResponse>>> => {
  const response = await apiClient.get<ApiResponse<Page<KnowledgeBaseResponse>>>(`/knowledge-bases?page=${page}&size=${size}`);
  return response.data;
};

// 获取知识库详情
export const getKnowledgeBase = async (id: number): Promise<ApiResponse<KnowledgeBaseResponse>> => {
  const response = await apiClient.get<ApiResponse<KnowledgeBaseResponse>>(`/knowledge-bases/${id}`);
  return response.data;
};

// 更新知识库
export const updateKnowledgeBase = async (id: number, data: KnowledgeBaseCreateRequest): Promise<ApiResponse<KnowledgeBaseResponse>> => {
  const response = await apiClient.put<ApiResponse<KnowledgeBaseResponse>>(`/knowledge-bases/${id}`, data);
  return response.data;
};

// 删除知识库
export const deleteKnowledgeBase = async (id: number): Promise<ApiResponse<void>> => {
  const response = await apiClient.delete<ApiResponse<void>>(`/knowledge-bases/${id}`);
  return response.data;
};

// 搜索知识库
export const searchKnowledgeBases = async (keyword: string): Promise<ApiResponse<KnowledgeBaseResponse[]>> => {
  const response = await apiClient.get<ApiResponse<KnowledgeBaseResponse[]>>(`/knowledge-bases/search?keyword=${keyword}`);
  return response.data;
};