import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import type { Workflow, WorkflowExecution, WorkflowStats } from '../types/workflow';

interface WorkflowState {
  // 工作流列表
  workflows: Workflow[];
  currentWorkflow: Workflow | null;
  templates: Workflow[];
  categories: string[];
  stats: WorkflowStats | null;
  
  // 执行记录
  executions: WorkflowExecution[];
  currentExecution: WorkflowExecution | null;
  
  // 加载状态
  loading: boolean;
  error: string | null;
  
  // 分页信息
  pagination: {
    page: number;
    size: number;
    total: number;
    totalPages: number;
  };
  
  // Actions
  setWorkflows: (workflows: Workflow[]) => void;
  setCurrentWorkflow: (workflow: Workflow | null) => void;
  setTemplates: (templates: Workflow[]) => void;
  setCategories: (categories: string[]) => void;
  setStats: (stats: WorkflowStats) => void;
  setExecutions: (executions: WorkflowExecution[]) => void;
  setCurrentExecution: (execution: WorkflowExecution | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPagination: (pagination: Partial<WorkflowState['pagination']>) => void;
  
  // 工作流操作
  addWorkflow: (workflow: Workflow) => void;
  updateWorkflow: (workflow: Workflow) => void;
  removeWorkflow: (id: number) => void;
  
  // 执行操作
  addExecution: (execution: WorkflowExecution) => void;
  updateExecution: (execution: WorkflowExecution) => void;
  
  // 重置状态
  reset: () => void;
}

const initialState = {
  workflows: [],
  currentWorkflow: null,
  templates: [],
  categories: [],
  stats: null,
  executions: [],
  currentExecution: null,
  loading: false,
  error: null,
  pagination: {
    page: 0,
    size: 20,
    total: 0,
    totalPages: 0,
  },
};

export const useWorkflowStore = create<WorkflowState>()(
  devtools(
    (set, get) => ({
      ...initialState,
      
      setWorkflows: (workflows) => set({ workflows }),
      setCurrentWorkflow: (workflow) => set({ currentWorkflow: workflow }),
      setTemplates: (templates) => set({ templates }),
      setCategories: (categories) => set({ categories }),
      setStats: (stats) => set({ stats }),
      setExecutions: (executions) => set({ executions }),
      setCurrentExecution: (execution) => set({ currentExecution: execution }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),
      setPagination: (pagination) => 
        set((state) => ({ 
          pagination: { ...state.pagination, ...pagination } 
        })),
      
      addWorkflow: (workflow) =>
        set((state) => ({
          workflows: [workflow, ...state.workflows],
        })),
      
      updateWorkflow: (workflow) =>
        set((state) => ({
          workflows: state.workflows.map((w) =>
            w.id === workflow.id ? workflow : w
          ),
          currentWorkflow: 
            state.currentWorkflow?.id === workflow.id ? workflow : state.currentWorkflow,
        })),
      
      removeWorkflow: (id) =>
        set((state) => ({
          workflows: state.workflows.filter((w) => w.id !== id),
          currentWorkflow: 
            state.currentWorkflow?.id === id ? null : state.currentWorkflow,
        })),
      
      addExecution: (execution) =>
        set((state) => ({
          executions: [execution, ...state.executions],
        })),
      
      updateExecution: (execution) =>
        set((state) => ({
          executions: state.executions.map((e) =>
            e.id === execution.id ? execution : e
          ),
          currentExecution: 
            state.currentExecution?.id === execution.id ? execution : state.currentExecution,
        })),
      
      reset: () => set(initialState),
    }),
    {
      name: 'workflow-store',
    }
  )
);