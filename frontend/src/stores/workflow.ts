import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { WorkflowItem, WorkflowDefinition, WorkflowNode, WorkflowEdge } from '../types/workflow'

interface WorkflowState {
  // 当前编辑的工作流
  currentWorkflow: WorkflowItem | null
  // 工作流定义
  workflowDefinition: WorkflowDefinition
  // 选中的节点
  selectedNode: WorkflowNode | null
  // 是否有未保存的更改
  hasUnsavedChanges: boolean

  // 设置当前工作流
  setCurrentWorkflow: (workflow: WorkflowItem | null) => void
  // 更新工作流定义
  setWorkflowDefinition: (definition: WorkflowDefinition) => void
  // 添加节点
  addNode: (node: WorkflowNode) => void
  // 更新节点
  updateNode: (nodeId: string, updates: Partial<WorkflowNode>) => void
  // 删除节点
  removeNode: (nodeId: string) => void
  // 添加连接
  addEdge: (edge: WorkflowEdge) => void
  // 删除连接
  removeEdge: (edgeId: string) => void
  // 设置选中节点
  setSelectedNode: (node: WorkflowNode | null) => void
  // 设置未保存状态
  setHasUnsavedChanges: (hasUnsaved: boolean) => void
  // 重置状态
  reset: () => void
}

const defaultWorkflowDefinition: WorkflowDefinition = {
  nodes: [],
  edges: [],
  variables: {},
  settings: {}
}

export const useWorkflowStore = create<WorkflowState>()(
  persist(
    (set) => ({
      currentWorkflow: null,
      workflowDefinition: defaultWorkflowDefinition,
      selectedNode: null,
      hasUnsavedChanges: false,

      setCurrentWorkflow: (workflow) => set({ currentWorkflow: workflow }),
      
      setWorkflowDefinition: (definition) => set({ workflowDefinition: definition }),
      
      addNode: (node) => set((state) => ({
        workflowDefinition: {
          ...state.workflowDefinition,
          nodes: [...state.workflowDefinition.nodes, node]
        },
        hasUnsavedChanges: true
      })),
      
      updateNode: (nodeId, updates) => set((state) => {
        const nodes = state.workflowDefinition.nodes.map((node: WorkflowNode) => 
          node.id === nodeId ? { ...node, ...updates } : node
        )
        return {
          workflowDefinition: {
            ...state.workflowDefinition,
            nodes
          },
          hasUnsavedChanges: true
        }
      }),
      
      removeNode: (nodeId) => set((state) => {
        // 同时删除相关的连接
        const edges = state.workflowDefinition.edges.filter(
          (edge: WorkflowEdge) => edge.source !== nodeId && edge.target !== nodeId
        )
        const nodes = state.workflowDefinition.nodes.filter((node: WorkflowNode) => node.id !== nodeId)
        return {
          workflowDefinition: {
            ...state.workflowDefinition,
            nodes,
            edges
          },
          hasUnsavedChanges: true
        }
      }),
      
      addEdge: (edge) => set((state) => ({
        workflowDefinition: {
          ...state.workflowDefinition,
          edges: [...state.workflowDefinition.edges, edge]
        },
        hasUnsavedChanges: true
      })),
      
      removeEdge: (edgeId) => set((state) => ({
        workflowDefinition: {
          ...state.workflowDefinition,
          edges: state.workflowDefinition.edges.filter((edge: WorkflowEdge) => edge.id !== edgeId)
        },
        hasUnsavedChanges: true
      })),
      
      setSelectedNode: (node) => set({ selectedNode: node }),
      
      setHasUnsavedChanges: (hasUnsaved) => set({ hasUnsavedChanges: hasUnsaved }),
      
      reset: () => set({
        currentWorkflow: null,
        workflowDefinition: defaultWorkflowDefinition,
        selectedNode: null,
        hasUnsavedChanges: false
      })
    }),
    {
      name: 'workflow-storage',
      partialize: (state) => ({
        currentWorkflow: state.currentWorkflow,
        workflowDefinition: state.workflowDefinition,
        hasUnsavedChanges: state.hasUnsavedChanges
      })
    }
  )
)