export interface AgentDefinition {
  id?: number;
  name: string;
  description?: string;
  type: AgentType;
  roleDefinition?: string;
  systemPrompt?: string;
  capabilities?: string[];
  modelConfig?: string;
  config?: string;
  temperature?: number;
  maxTokens?: number;
  enabled?: boolean;
  version?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum AgentType {
  AGENT = 'AGENT',
  WORKFLOW = 'WORKFLOW',
  COORDINATOR = 'COORDINATOR',
  SPECIALIST = 'SPECIALIST'
}