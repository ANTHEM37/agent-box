import apiClient from '../http/client';
import { AgentDefinition } from '../../types/agent';

// 创建智能体定义
export const createAgentDefinition = async (data: AgentDefinition): Promise<AgentDefinition> => {
  const response = await apiClient.post<AgentDefinition>('/agent-definitions', data);
  return response.data;
};

// 更新智能体定义
export const updateAgentDefinition = async (id: number, data: AgentDefinition): Promise<AgentDefinition> => {
  const response = await apiClient.put<AgentDefinition>(`/agent-definitions/${id}`, data);
  return response.data;
};

// 根据ID获取智能体定义
export const getAgentDefinitionById = async (id: number): Promise<AgentDefinition> => {
  const response = await apiClient.get<AgentDefinition>(`/agent-definitions/${id}`);
  return response.data;
};

// 获取所有智能体定义
export const getAllAgentDefinitions = async (): Promise<AgentDefinition[]> => {
  const response = await apiClient.get<AgentDefinition[]>('/agent-definitions');
  return response.data;
};

// 删除智能体定义
export const deleteAgentDefinition = async (id: number): Promise<void> => {
  await apiClient.delete(`/agent-definitions/${id}`);
};