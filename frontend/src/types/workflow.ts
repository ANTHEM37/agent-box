// 工作流相关类型定义

export interface WorkflowNode {
  id: string;
  type: string;
  name: string;
  data: Record<string, any>;
  position: {
    x: number;
    y: number;
  };
  config: Record<string, any>;
}

export interface WorkflowEdge {
  id: string;
  source: string;
  target: string;
  sourceHandle?: string;
  targetHandle?: string;
  type?: string;
  data?: Record<string, any>;
}

export interface WorkflowDefinition {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
  variables?: Record<string, any>;
  settings?: Record<string, any>;
}

export interface Workflow {
  id: number;
  name: string;
  description?: string;
  definition: WorkflowDefinition;
  version: number;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  category?: string;
  tags?: string[];
  isTemplate: boolean;
  userId: number;
  userName: string;
  createdAt: string;
  updatedAt: string;
  
  // 统计信息
  executionCount?: number;
  successCount?: number;
  successRate?: number;
  avgExecutionTime?: number;
}

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
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
}

export interface WorkflowExecution {
  id: number;
  workflowId: number;
  workflowName: string;
  workflowVersion: number;
  status: 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  inputData?: Record<string, any>;
  outputData?: Record<string, any>;
  startedAt: string;
  completedAt?: string;
  durationMs?: number;
  errorMessage?: string;
  userId: number;
  
  // 节点执行统计
  totalNodes?: number;
  completedNodes?: number;
  failedNodes?: number;
  progress?: number;
}

export interface WorkflowExecutionRequest {
  workflowId: number;
  inputData?: Record<string, any>;
  async?: boolean;
}

export interface NodeType {
  type: string;
  displayName: string;
  description: string;
  category: string;
  icon: string;
  configSchema?: Record<string, any>;
}

// 预定义节点类型
export const NODE_TYPES: NodeType[] = [
  {
    type: 'start',
    displayName: '开始',
    description: '工作流开始节点',
    category: 'control',
    icon: 'PlayCircleOutlined'
  },
  {
    type: 'end',
    displayName: '结束',
    description: '工作流结束节点',
    category: 'control',
    icon: 'StopOutlined'
  },
  {
    type: 'llm_chat',
    displayName: 'LLM对话',
    description: '调用大语言模型进行对话',
    category: 'ai',
    icon: 'MessageOutlined'
  },
  {
    type: 'knowledge_retrieval',
    displayName: '知识库检索',
    description: '从知识库中检索相关信息',
    category: 'ai',
    icon: 'DatabaseOutlined'
  },
  {
    type: 'http_request',
    displayName: 'HTTP请求',
    description: '发送HTTP请求到外部API',
    category: 'integration',
    icon: 'ApiOutlined'
  },
  {
    type: 'condition',
    displayName: '条件判断',
    description: '根据条件控制执行路径',
    category: 'control',
    icon: 'BranchesOutlined'
  },
  {
    type: 'variable_set',
    displayName: '变量设置',
    description: '设置工作流变量',
    category: 'data',
    icon: 'SettingOutlined'
  },
  {
    type: 'code_execution',
    displayName: '代码执行',
    description: '执行自定义代码逻辑',
    category: 'advanced',
    icon: 'CodeOutlined'
  }
];

export interface WorkflowStats {
  total: number;
  draft: number;
  published: number;
  archived: number;
}