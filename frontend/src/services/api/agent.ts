import { httpClient } from '../http/client'
import type { Page } from '../types/common'

export interface AgentDefinition {
  id: number
  name: string
  description: string
  type: AgentType
  config: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface AgentInstance {
  id: number
  agentDefinitionId: number
  agentDefinition?: AgentDefinition
  sessionId?: string
  status: InstanceStatus
  createdBy: number
  startedAt?: string
  lastActiveAt?: string
  createdAt: string
  updatedAt: string
}

export interface Task {
  id: number
  agentInstanceId: number
  agentInstance?: AgentInstance
  name: string
  description: string
  type: TaskType
  subType: TaskSubType
  status: TaskStatus
  inputParameters: string
  outputResult: string
  priority: number
  startTime?: string
  endTime?: string
  completedAt?: string
  retryCount: number
  estimatedDuration?: number
  actualDuration?: number
  errorMessage?: string
  parentTaskId?: number
  parentTask?: Task
  createdAt: string
  updatedAt: string
}

export interface Message {
  id: number
  senderId: number
  sender?: AgentInstance
  receiverId?: number
  receiver?: AgentInstance
  content: string
  messageType: MessageType
  status: MessageStatus
  sentAt: string
  deliveredAt?: string
  readAt?: string
  createdAt: string
  updatedAt: string
}

export enum AgentType {
  TEXT_PROCESSING = 'TEXT_PROCESSING',
  DATA_ANALYSIS = 'DATA_ANALYSIS',
  DECISION_MAKING = 'DECISION_MAKING',
  COLLABORATIVE = 'COLLABORATIVE'
}

export enum InstanceStatus {
  CREATED = 'CREATED',
  RUNNING = 'RUNNING',
  PAUSED = 'PAUSED',
  STOPPED = 'STOPPED',
  DESTROYED = 'DESTROYED'
}

export enum TaskType {
  SINGLE = 'SINGLE',
  SEQUENTIAL = 'SEQUENTIAL',
  PARALLEL = 'PARALLEL',
  WORKFLOW = 'WORKFLOW'
}

export enum TaskSubType {
  TEXT_PROCESSING = 'TEXT_PROCESSING',
  DATA_ANALYSIS = 'DATA_ANALYSIS',
  DECISION_MAKING = 'DECISION_MAKING',
  COLLABORATIVE = 'COLLABORATIVE'
}

export enum TaskStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export enum MessageType {
  TEXT = 'TEXT',
  COLLABORATION_REQUEST = 'COLLABORATION_REQUEST',
  TASK_RESULT = 'TASK_RESULT',
  SYSTEM_NOTIFICATION = 'SYSTEM_NOTIFICATION'
}

export enum MessageStatus {
  SENT = 'SENT',
  DELIVERED = 'DELIVERED',
  READ = 'READ',
  FAILED = 'FAILED'
}

export interface CreateAgentDefinitionRequest {
  name: string
  description: string
  type: AgentType
  config: string
}

export interface CreateAgentInstanceRequest {
  agentDefinitionId: number
  createdBy: number
  sessionId?: string
}

export interface CreateTaskRequest {
  agentInstanceId: number
  name: string
  description: string
  type: TaskType
  subType: TaskSubType
  inputParameters: string
  priority?: number
  estimatedDuration?: number
}

export interface SendMessageRequest {
  senderId: number
  receiverId?: number
  content: string
  messageType: MessageType
}

export const agentApi = {
  // 智能体定义相关接口
  getAgentDefinitions: (page = 0, size = 10) =>
    httpClient.get<Page<AgentDefinition>>(`/api/agent/definitions?page=${page}&size=${size}`),
    
  getAgentDefinitionById: (id: number) =>
    httpClient.get<AgentDefinition>(`/api/agent/definitions/${id}`),
    
  getAgentDefinitionByName: (name: string) =>
    httpClient.get<AgentDefinition>(`/api/agent/definitions/name/${name}`),
    
  createAgentDefinition: (data: CreateAgentDefinitionRequest) =>
    httpClient.post<AgentDefinition>('/api/agent/definitions', data),
    
  updateAgentDefinition: (id: number, data: Partial<AgentDefinition>) =>
    httpClient.put<AgentDefinition>(`/api/agent/definitions/${id}`, data),
    
  deleteAgentDefinition: (id: number) =>
    httpClient.delete(`/api/agent/definitions/${id}`),
    
  enableAgentDefinition: (id: number) =>
    httpClient.post<AgentDefinition>(`/api/agent/definitions/${id}/enable`),
    
  disableAgentDefinition: (id: number) =>
    httpClient.post<AgentDefinition>(`/api/agent/definitions/${id}/disable`),
    
  getAgentDefinitionsByType: (type: AgentType) =>
    httpClient.get<AgentDefinition[]>(`/api/agent/definitions/type/${type}`),
    
  getEnabledAgentDefinitions: () =>
    httpClient.get<AgentDefinition[]>('/api/agent/definitions/enabled'),
    
  validateAgentDefinition: (data: AgentDefinition) =>
    httpClient.post<boolean>('/api/agent/definitions/validate', data),

  // 智能体实例相关接口
  getAgentInstances: (page = 0, size = 10) =>
    httpClient.get<Page<AgentInstance>>(`/api/agent/instances?page=${page}&size=${size}`),
    
  getAgentInstanceById: (id: number) =>
    httpClient.get<AgentInstance>(`/api/agent/instances/${id}`),
    
  getAgentInstanceBySessionId: (sessionId: string) =>
    httpClient.get<AgentInstance>(`/api/agent/instances/session/${sessionId}`),
    
  createAgentInstance: (data: CreateAgentInstanceRequest) =>
    httpClient.post<AgentInstance>('/api/agent/instances', data),
    
  startAgentInstance: (id: number) =>
    httpClient.post<AgentInstance>(`/api/agent/instances/${id}/start`),
    
  stopAgentInstance: (id: number) =>
    httpClient.post<AgentInstance>(`/api/agent/instances/${id}/stop`),
    
  pauseAgentInstance: (id: number) =>
    httpClient.post<AgentInstance>(`/api/agent/instances/${id}/pause`),
    
  resumeAgentInstance: (id: number) =>
    httpClient.post<AgentInstance>(`/api/agent/instances/${id}/resume`),
    
  destroyAgentInstance: (id: number) =>
    httpClient.delete(`/api/agent/instances/${id}`),
    
  getAgentInstancesByDefinitionId: (definitionId: number) =>
    httpClient.get<AgentInstance[]>(`/api/agent/instances/definition/${definitionId}`),
    
  getAgentInstancesByStatus: (status: InstanceStatus) =>
    httpClient.get<AgentInstance[]>(`/api/agent/instances/status/${status}`),
    
  updateAgentInstanceStatus: (id: number, status: InstanceStatus) =>
    httpClient.put<AgentInstance>(`/api/agent/instances/${id}/status?status=${status}`),
    
  updateLastActiveTime: (id: number) =>
    httpClient.post<AgentInstance>(`/api/agent/instances/${id}/heartbeat`),
    
  isInstanceActive: (id: number) =>
    httpClient.get<boolean>(`/api/agent/instances/${id}/active`),
    
  getInstanceStats: () =>
    httpClient.get<any>('/api/agent/instances/stats'),

  // 任务相关接口
  getTasks: (page = 0, size = 10) =>
    httpClient.get<Page<Task>>(`/api/agent/tasks?page=${page}&size=${size}`),
    
  getTaskById: (id: number) =>
    httpClient.get<Task>(`/api/agent/tasks/${id}`),
    
  createTask: (data: CreateTaskRequest) =>
    httpClient.post<Task>('/api/agent/tasks', data),
    
  getTasksByAgentInstanceId: (agentInstanceId: number) =>
    httpClient.get<Task[]>(`/api/agent/tasks/agent-instance/${agentInstanceId}`),
    
  getTasksByStatus: (status: TaskStatus) =>
    httpClient.get<Task[]>(`/api/agent/tasks/status/${status}`),
    
  getPendingTasksOrderByPriority: () =>
    httpClient.get<Task[]>('/api/agent/tasks/pending'),
    
  updateTaskStatus: (id: number, status: TaskStatus) =>
    httpClient.put<Task>(`/api/agent/tasks/${id}/status?status=${status}`),
    
  startTaskExecution: (id: number) =>
    httpClient.post<Task>(`/api/agent/tasks/${id}/start`),
    
  completeTask: (id: number, result: string) =>
    httpClient.post<Task>(`/api/agent/tasks/${id}/complete?result=${encodeURIComponent(result)}`),
    
  failTask: (id: number, errorMessage: string) =>
    httpClient.post<Task>(`/api/agent/tasks/${id}/fail?errorMessage=${encodeURIComponent(errorMessage)}`),
    
  cancelTask: (id: number) =>
    httpClient.post<Task>(`/api/agent/tasks/${id}/cancel`),
    
  retryTask: (id: number) =>
    httpClient.post<Task>(`/api/agent/tasks/${id}/retry`),
    
  getSubTasks: (id: number) =>
    httpClient.get<Task[]>(`/api/agent/tasks/${id}/subtasks`),
    
  getTaskStats: () =>
    httpClient.get<any>('/api/agent/tasks/stats'),

  // 消息相关接口
  getMessages: (page = 0, size = 10) =>
    httpClient.get<Page<Message>>(`/api/agent/messages?page=${page}&size=${size}`),
    
  getMessageById: (id: number) =>
    httpClient.get<Message>(`/api/agent/messages/${id}`),
    
  sendMessage: (data: SendMessageRequest) =>
    httpClient.post<Message>('/api/agent/messages', data),
    
  getMessagesBySenderId: (senderId: number) =>
    httpClient.get<Message[]>(`/api/agent/messages/sender/${senderId}`),
    
  getReceiversMessages: (receiverId: number) =>
    httpClient.get<Message[]>(`/api/agent/messages/receiver/${receiverId}`),
    
  getConversationBetweenAgents: (agent1Id: number, agent2Id: number) =>
    httpClient.get<Message[]>(`/api/agent/messages/conversation?agent1Id=${agent1Id}&agent2Id=${agent2Id}`),
    
  markMessageAsRead: (id: number) =>
    httpClient.post<Message>(`/api/agent/messages/${id}/read`),
    
  markMessageAsDelivered: (id: number) =>
    httpClient.post<Message>(`/api/agent/messages/${id}/delivered`),
    
  getUnreadMessageCount: (receiverId: number) =>
    httpClient.get<number>(`/api/agent/messages/receiver/${receiverId}/unread-count`),
    
  getMessagesByStatus: (status: MessageStatus) =>
    httpClient.get<Message[]>(`/api/agent/messages/status/${status}`),
    
  replyToMessage: (id: number, replyMessage: Message) =>
    httpClient.post<Message>(`/api/agent/messages/${id}/reply`, replyMessage),
    
  forwardMessage: (id: number, newReceiverId: number) =>
    httpClient.post<Message>(`/api/agent/messages/${id}/forward?newReceiverId=${newReceiverId}`),
    
  deleteMessage: (id: number) =>
    httpClient.delete(`/api/agent/messages/${id}`),
    
  getMessageStats: () =>
    httpClient.get<any>('/api/agent/messages/stats')
}