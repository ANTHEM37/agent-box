import { apiClient } from './client';
import type {
  McpService,
  ServiceDeployment,
  ServiceReview,
  McpServiceCreateRequest,
  ServiceDeploymentRequest,
  ServiceReviewRequest,
  ServiceSearchParams,
  DeploymentStats,
} from '../../types/mcp';

// MCP 服务 API
export const mcpApi = {
  // 服务管理
  async createService(data: McpServiceCreateRequest): Promise<McpService> {
    const response = await apiClient.post('/mcp/services', data);
    return response.data;
  },

  async getService(id: number): Promise<McpService> {
    const response = await apiClient.get(`/mcp/services/${id}`);
    return response.data;
  },

  async updateService(id: number, data: McpServiceCreateRequest): Promise<McpService> {
    const response = await apiClient.put(`/mcp/services/${id}`, data);
    return response.data;
  },

  async publishService(id: number): Promise<void> {
    await apiClient.post(`/mcp/services/${id}/publish`);
  },

  async deleteService(id: number): Promise<void> {
    await apiClient.delete(`/mcp/services/${id}`);
  },

  // 服务列表和搜索
  async getPublishedServices(params?: ServiceSearchParams): Promise<{
    content: McpService[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get('/mcp/services/published', { params });
    return response.data;
  },

  async searchServices(params: ServiceSearchParams): Promise<{
    content: McpService[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get('/mcp/services/search', { params });
    return response.data;
  },

  async getServicesByCategory(category: string, params?: ServiceSearchParams): Promise<{
    content: McpService[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get(`/mcp/services/category/${category}`, { params });
    return response.data;
  },

  async getServicesByTag(tag: string, params?: ServiceSearchParams): Promise<{
    content: McpService[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get(`/mcp/services/tag/${tag}`, { params });
    return response.data;
  },

  async getFeaturedServices(): Promise<McpService[]> {
    const response = await apiClient.get('/mcp/services/featured');
    return response.data;
  },

  async getPopularServices(params?: ServiceSearchParams): Promise<{
    content: McpService[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get('/mcp/services/popular', { params });
    return response.data;
  },

  async getHighRatedServices(minRating = 4.0, params?: ServiceSearchParams): Promise<{
    content: McpService[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get('/mcp/services/high-rated', { 
      params: { minRating, ...params } 
    });
    return response.data;
  },

  async getUserServices(): Promise<McpService[]> {
    const response = await apiClient.get('/mcp/services/my');
    return response.data;
  },

  async getAllCategories(): Promise<string[]> {
    const response = await apiClient.get('/mcp/services/categories');
    return response.data;
  },

  async getAllTags(): Promise<string[]> {
    const response = await apiClient.get('/mcp/services/tags');
    return response.data;
  },

  async incrementDownloads(id: number): Promise<void> {
    await apiClient.post(`/mcp/services/${id}/download`);
  },

  // 部署管理
  async createDeployment(data: ServiceDeploymentRequest): Promise<ServiceDeployment> {
    const response = await apiClient.post('/mcp/deployments', data);
    return response.data;
  },

  async getDeployment(id: number): Promise<ServiceDeployment> {
    const response = await apiClient.get(`/mcp/deployments/${id}`);
    return response.data;
  },

  async getUserDeployments(params?: { page?: number; size?: number }): Promise<{
    content: ServiceDeployment[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get('/mcp/deployments/my', { params });
    return response.data;
  },

  async stopDeployment(id: number): Promise<void> {
    await apiClient.post(`/mcp/deployments/${id}/stop`);
  },

  async restartDeployment(id: number): Promise<void> {
    await apiClient.post(`/mcp/deployments/${id}/restart`);
  },

  async deleteDeployment(id: number): Promise<void> {
    await apiClient.delete(`/mcp/deployments/${id}`);
  },

  async scaleDeployment(id: number, replicas: number): Promise<void> {
    await apiClient.post(`/mcp/deployments/${id}/scale`, null, {
      params: { replicas }
    });
  },

  async getDeploymentStats(): Promise<DeploymentStats> {
    const response = await apiClient.get('/mcp/deployments/stats');
    return response.data;
  },

  // 评论管理
  async createReview(data: ServiceReviewRequest): Promise<ServiceReview> {
    const response = await apiClient.post('/mcp/reviews', data);
    return response.data;
  },

  async getServiceReviews(serviceId: number, params?: { page?: number; size?: number }): Promise<{
    content: ServiceReview[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get(`/mcp/reviews/service/${serviceId}`, { params });
    return response.data;
  },

  async updateReview(id: number, data: Partial<ServiceReviewRequest>): Promise<ServiceReview> {
    const response = await apiClient.put(`/mcp/reviews/${id}`, data);
    return response.data;
  },

  async deleteReview(id: number): Promise<void> {
    await apiClient.delete(`/mcp/reviews/${id}`);
  },

  async markReviewHelpful(id: number): Promise<void> {
    await apiClient.post(`/mcp/reviews/${id}/helpful`);
  },
};