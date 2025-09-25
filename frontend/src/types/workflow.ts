export interface WorkflowCreateRequest {
  name: string;
  description?: string;
  definition: WorkflowDefinition;
  category?: string;
  tags?: string[];
  isTemplate?: boolean;
}

export interface WorkflowUpdateRequest {
  name: string;
  description?: string;
  definition: WorkflowDefinition;
  category?: string;
  tags?: string[];
  status?: WorkflowStatus;
  isTemplate?: boolean;
}

export interface WorkflowResponse {
  id: number;
  name: string;
  description?: string;
  definition: WorkflowDefinition;
  version: number;
  status: WorkflowStatus;
  category?: string;
  tags?: string[];
  isTemplate: boolean;
  userId: number;
  userName: string;
  createdAt: string;
  updatedAt: string;
  
  // 统计信息
  executionCount: number;
  successCount: number;
  successRate: number;
  avgExecutionTime: number;
}

export interface WorkflowDefinition {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
}

export interface WorkflowNode {
  id: string;
  type: string;
  position: { x: number; y: number };
  data: any;
}

export interface WorkflowEdge {
  id: string;
  source: string;
  target: string;
  type: string;
}

export enum WorkflowStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}