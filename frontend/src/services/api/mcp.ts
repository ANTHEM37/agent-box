import apiClient from '../http/client';
import { ApiResponse, Page } from '../../types/common';
import { 
  McpServiceCreateRequest, 
  McpServiceResponse 
} from '../../types/mcp';

// 创建 MCP 服务
export const createService = async (data: McpServiceCreateRequest): Promise<ApiResponse<McpServiceResponse>> => {
  const response = await apiClient.post<ApiResponse<McpServiceResponse>>('/mcp/services', data);
  return response.data;
};

// 获取服务详情
export const getService = async (id: number): Promise<ApiResponse<McpServiceResponse>> => {
  const response = await apiClient.get<ApiResponse<McpServiceResponse>>(`/mcp/services/${id}`);
  return response.data;
};

// 更新服务
export const updateService = async (id: number, data: McpServiceCreateRequest): Promise<ApiResponse<McpServiceResponse>> => {
  const response = await apiClient.put<ApiResponse<McpServiceResponse>>(`/mcp/services/${id}`, data);
  return response.data;
};

// 发布服务
export const publishService = async (id: number): Promise<ApiResponse<void>> => {
  const response = await apiClient.post<ApiResponse<void>>(`/mcp/services/${id}/publish`);
  return response.data;
};

// 删除服务
export const deleteService = async (id: number): Promise<ApiResponse<void>> => {
  const response = await apiClient.delete<ApiResponse<void>>(`/mcp/services/${id}`);
  return response.data;
};

// 获取已发布的服务列表
export const getPublishedServices = async (page: number = 0, size: number = 20): Promise<ApiResponse<Page<McpServiceResponse>>> => {
  const response = await apiClient.get<ApiResponse<Page<McpServiceResponse>>>(`/mcp/services/published?page=${page}&size=${size}`);
  return response.data;
};

// 搜索服务
export const searchServices = async (keyword: string, page: number = 0, size: number = 20): Promise<ApiResponse<Page<McpServiceResponse>>> => {
  const response = await apiClient.get<ApiResponse<Page<McpServiceResponse>>>(`/mcp/services/search?keyword=${keyword}&page=${page}&size=${size}`);
  return response.data;
};

// 根据分类获取服务
export const getServicesByCategory = async (category: string, page: number = 0, size: number = 20): Promise<ApiResponse<Page<McpServiceResponse>>> => {
  const response = await apiClient.get<ApiResponse<Page<McpServiceResponse>>>(`/mcp/services/category/${category}?page=${page}&size=${size}`);
  return response.data;
};

// 获取用户的服务
export const getUserServices = async (): Promise<ApiResponse<McpServiceResponse[]>> => {
  const response = await apiClient.get<ApiResponse<McpServiceResponse[]>>(`/mcp/services/my`);
  return response.data;
};