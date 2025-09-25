export interface WorkflowNode {
  id: string
  type: string
  name: string
  data?: Record<string, any>
  position: {
    x: number
    y: number
  }
  config?: Record<string, any>
}

export interface WorkflowEdge {
  id: string
  source: string
  target: string
  sourceHandle?: string
  targetHandle?: string
  type?: string
  data?: Record<string, any>
}

export interface WorkflowDefinition {
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
  variables?: Record<string, any>
  settings?: Record<string, any>
}

export interface WorkflowItem {
  id: number
  name: string
  description?: string
  definition: WorkflowDefinition
  version: number
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  category?: string
  tags?: string[]
  isTemplate: boolean
  userId?: number
  userName?: string
  createdAt: string
  updatedAt: string
  executionCount?: number
  successCount?: number
  successRate?: number
  avgExecutionTime?: number
}

export interface WorkflowExecution {
  id: number
  workflowId: number
  workflowName?: string
  workflowVersion: number
  status: 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
  inputData?: Record<string, any>
  outputData?: Record<string, any>
  startedAt: string
  completedAt?: string
  durationMs?: number
  errorMessage?: string
  userId?: number
  totalNodes?: number
  completedNodes?: number
  failedNodes?: number
  progress?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface WorkflowCreateRequest {
  name: string
  description?: string
  definition: WorkflowDefinition
  category?: string
  tags?: string[]
  isTemplate?: boolean
}

export interface WorkflowUpdateRequest {
  name?: string
  description?: string
  definition?: WorkflowDefinition
  category?: string
  tags?: string[]
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
}

export interface WorkflowExecutionRequest {
  workflowId: number
  inputData?: Record<string, any>
}