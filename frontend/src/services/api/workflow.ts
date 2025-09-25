import http from '../http/client'
import type {
  WorkflowItem,
  PageResponse,
  WorkflowCreateRequest,
  WorkflowUpdateRequest,
  WorkflowExecution,
  WorkflowExecutionRequest
} from '../../types/workflow'

// 工作流 API 服务
export const workflowApi = {
  // 获取工作流列表
  list: (params?: {
    page?: number
    size?: number
    status?: string
    category?: string
    keyword?: string
    tag?: string
  }) => {
    return http.get<{ data: PageResponse<WorkflowItem> }>('/workflows', { params })
  },

  // 创建工作流
  create: (data: WorkflowCreateRequest) => {
    return http.post<{ data: WorkflowItem }>('/workflows', data)
  },

  // 获取工作流详情
  get: (id: number) => {
    return http.get<{ data: WorkflowItem }>(`/workflows/${id}`)
  },

  // 更新工作流
  update: (id: number, data: WorkflowUpdateRequest) => {
    return http.put<{ data: WorkflowItem }>(`/workflows/${id}`, data)
  },

  // 删除工作流
  delete: (id: number) => {
    return http.delete(`/workflows/${id}`)
  },

  // 发布工作流
  publish: (id: number) => {
    return http.post<{ data: WorkflowItem }>(`/workflows/${id}/publish`)
  },

  // 归档工作流
  archive: (id: number) => {
    return http.post<{ data: WorkflowItem }>(`/workflows/${id}/archive`)
  },

  // 复制工作流
  copy: (id: number, newName: string) => {
    return http.post<{ data: WorkflowItem }>(`/workflows/${id}/copy`, null, {
      params: { newName }
    })
  },

  // 获取工作流模板
  getTemplates: (params?: { page?: number; size?: number; category?: string }) => {
    return http.get<{ data: PageResponse<WorkflowItem> }>('/workflows/templates', { params })
  },

  // 获取所有分类
  getAllCategories: () => {
    return http.get<{ data: string[] }>('/workflows/categories')
  },

  // 获取用户的分类
  getUserCategories: () => {
    return http.get<{ data: string[] }>('/workflows/categories/mine')
  },

  // 获取用户工作流统计
  getUserWorkflowStats: () => {
    return http.get<{ data: Record<string, number> }>('/workflows/stats')
  },

  // 执行工作流
  execute: (data: WorkflowExecutionRequest) => {
    return http.post<{ data: WorkflowExecution }>('/workflow-executions/execute', data)
  },

  // 获取执行详情
  getExecution: (id: number) => {
    return http.get<{ data: WorkflowExecution }>(`/workflow-executions/${id}`)
  },

  // 取消执行
  cancelExecution: (id: number) => {
    return http.post(`/workflow-executions/${id}/cancel`)
  },

  // 获取用户的执行记录
  getUserExecutions: (params?: {
    page?: number
    size?: number
    status?: string
    workflowId?: number
  }) => {
    return http.get<{ data: PageResponse<WorkflowExecution> }>('/workflow-executions', { params })
  },

  // 获取工作流的执行记录
  getWorkflowExecutions: (workflowId: number, params?: { page?: number; size?: number }) => {
    return http.get<{ data: PageResponse<WorkflowExecution> }>(
      `/workflow-executions/workflow/${workflowId}`,
      { params }
    )
  }
}