import apiClient from '../http/client';
import { ApiResponse, Page } from '../../types/common';
import { 
  WorkflowCreateRequest, 
  WorkflowUpdateRequest, 
  WorkflowResponse 
} from '../../types/workflow';

// 创建工作流
export const createWorkflow = async (data: WorkflowCreateRequest): Promise<ApiResponse<WorkflowResponse>> => {
  const response = await apiClient.post<ApiResponse<WorkflowResponse>>('/api/workflows', data);
  return response.data;
};

// 更新工作流
export const updateWorkflow = async (id: number, data: WorkflowUpdateRequest): Promise<ApiResponse<WorkflowResponse>> => {
  const response = await apiClient.put<ApiResponse<WorkflowResponse>>(`/api/workflows/${id}`, data);
  return response.data;
};

// 获取工作流详情
export const getWorkflow = async (id: number): Promise<ApiResponse<WorkflowResponse>> => {
  const response = await apiClient.get<ApiResponse<WorkflowResponse>>(`/api/workflows/${id}`);
  return response.data;
};

// 删除工作流
export const deleteWorkflow = async (id: number): Promise<ApiResponse<void>> => {
  const response = await apiClient.delete<ApiResponse<void>>(`/api/workflows/${id}`);
  return response.data;
};

// 获取用户工作流列表
export const getUserWorkflows = async (
  page: number = 0, 
  size: number = 20,
  status?: string,
  category?: string,
  keyword?: string,
  tag?: string
): Promise<ApiResponse<Page<WorkflowResponse>>> => {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  
  if (status) params.append('status', status);
  if (category) params.append('category', category);
  if (keyword) params.append('keyword', keyword);
  if (tag) params.append('tag', tag);
  
  const response = await apiClient.get<ApiResponse<Page<WorkflowResponse>>>(`/api/workflows?${params.toString()}`);
  return response.data;
};

// 获取工作流模板
export const getTemplates = async (
  page: number = 0, 
  size: number = 20,
  category?: string
): Promise<ApiResponse<Page<WorkflowResponse>>> => {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  
  if (category) params.append('category', category);
  
  const response = await apiClient.get<ApiResponse<Page<WorkflowResponse>>>(`/api/workflows/templates?${params.toString()}`);
  return response.data;
};

// 发布工作流
export const publishWorkflow = async (id: number): Promise<ApiResponse<WorkflowResponse>> => {
  const response = await apiClient.post<ApiResponse<WorkflowResponse>>(`/api/workflows/${id}/publish`);
  return response.data;
};

// 归档工作流
export const archiveWorkflow = async (id: number): Promise<ApiResponse<WorkflowResponse>> => {
  const response = await apiClient.post<ApiResponse<WorkflowResponse>>(`/api/workflows/${id}/archive`);
  return response.data;
};