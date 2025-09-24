import { apiClient } from './client';
import type { 
  Workflow, 
  WorkflowCreateRequest, 
  WorkflowUpdateRequest,
  WorkflowExecution,
  WorkflowExecutionRequest,
  WorkflowStats
} from '../../types/workflow';

export const workflowApi = {
  // 工作流管理
  async createWorkflow(data: WorkflowCreateRequest): Promise<Workflow> {
    const response = await apiClient.post('/workflows', data);
    return response.data;
  },

  async updateWorkflow(id: number, data: WorkflowUpdateRequest): Promise<Workflow> {
    const response = await apiClient.put(`/workflows/${id}`, data);
    return response.data;
  },

  async getWorkflow(id: number): Promise<Workflow> {
    const response = await apiClient.get(`/workflows/${id}`);
    return response.data;
  },

  async deleteWorkflow(id: number): Promise<void> {
    await apiClient.delete(`/workflows/${id}`);
  },

  async getUserWorkflows(params: {
    page?: number;
    size?: number;
    status?: string;
    category?: string;
    keyword?: string;
    tag?: string;
  } = {}): Promise<{
    content: Workflow[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    const response = await apiClient.get('/workflows', { params });
    return response.data;
  },

  async getTemplates(params: {
    page?: number;
    size?: number;
    category?: string;
  } = {}): Promise<{
    content: Workflow[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    const response = await apiClient.get('/workflows/templates', { params });
    return response.data;
  },

  async getAllCategories(): Promise<string[]> {
    const response = await apiClient.get('/workflows/categories');
    return response.data;
  },

  async getUserCategories(): Promise<string[]> {
    const response = await apiClient.get('/workflows/categories/mine');
    return response.data;
  },

  async getUserStats(): Promise<WorkflowStats> {
    const response = await apiClient.get('/workflows/stats');
    return response.data;
  },

  async publishWorkflow(id: number): Promise<Workflow> {
    const response = await apiClient.post(`/workflows/${id}/publish`);
    return response.data;
  },

  async archiveWorkflow(id: number): Promise<Workflow> {
    const response = await apiClient.post(`/workflows/${id}/archive`);
    return response.data;
  },

  async copyWorkflow(id: number, newName: string): Promise<Workflow> {
    const response = await apiClient.post(`/workflows/${id}/copy`, null, {
      params: { newName }
    });
    return response.data;
  },

  // 工作流执行
  async executeWorkflow(data: WorkflowExecutionRequest): Promise<WorkflowExecution> {
    const response = await apiClient.post('/workflow-executions/execute', data);
    return response.data;
  },

  async getExecution(id: number): Promise<WorkflowExecution> {
    const response = await apiClient.get(`/workflow-executions/${id}`);
    return response.data;
  },

  async cancelExecution(id: number): Promise<void> {
    await apiClient.post(`/workflow-executions/${id}/cancel`);
  },

  async getUserExecutions(params: {
    page?: number;
    size?: number;
    status?: string;
    workflowId?: number;
  } = {}): Promise<{
    content: WorkflowExecution[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    const response = await apiClient.get('/workflow-executions', { params });
    return response.data;
  },

  async getWorkflowExecutions(workflowId: number, params: {
    page?: number;
    size?: number;
  } = {}): Promise<{
    content: WorkflowExecution[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    const response = await apiClient.get(`/workflow-executions/workflow/${workflowId}`, { params });
    return response.data;
  }
};