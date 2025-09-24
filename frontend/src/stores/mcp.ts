import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import type { McpService, ServiceDeployment, ServiceSearchParams } from '../types/mcp';

interface McpState {
  // 服务市场状态
  services: McpService[];
  featuredServices: McpService[];
  userServices: McpService[];
  currentService: McpService | null;
  
  // 部署状态
  deployments: ServiceDeployment[];
  currentDeployment: ServiceDeployment | null;
  
  // 搜索和过滤
  searchParams: ServiceSearchParams;
  categories: string[];
  tags: string[];
  
  // 加载状态
  loading: {
    services: boolean;
    deployments: boolean;
    creating: boolean;
    updating: boolean;
    deleting: boolean;
  };
  
  // 分页信息
  pagination: {
    services: {
      page: number;
      size: number;
      total: number;
      totalPages: number;
    };
    deployments: {
      page: number;
      size: number;
      total: number;
      totalPages: number;
    };
  };
  
  // Actions
  setServices: (services: McpService[]) => void;
  setFeaturedServices: (services: McpService[]) => void;
  setUserServices: (services: McpService[]) => void;
  setCurrentService: (service: McpService | null) => void;
  addService: (service: McpService) => void;
  updateService: (service: McpService) => void;
  removeService: (id: number) => void;
  
  setDeployments: (deployments: ServiceDeployment[]) => void;
  setCurrentDeployment: (deployment: ServiceDeployment | null) => void;
  addDeployment: (deployment: ServiceDeployment) => void;
  updateDeployment: (deployment: ServiceDeployment) => void;
  removeDeployment: (id: number) => void;
  
  setSearchParams: (params: Partial<ServiceSearchParams>) => void;
  setCategories: (categories: string[]) => void;
  setTags: (tags: string[]) => void;
  
  setLoading: (key: keyof McpState['loading'], value: boolean) => void;
  setPagination: (type: 'services' | 'deployments', pagination: Partial<McpState['pagination']['services']>) => void;
  
  // 重置状态
  reset: () => void;
}

const initialState = {
  services: [],
  featuredServices: [],
  userServices: [],
  currentService: null,
  
  deployments: [],
  currentDeployment: null,
  
  searchParams: {
    page: 0,
    size: 20,
    sortBy: 'created',
    sortOrder: 'desc',
  } as ServiceSearchParams,
  categories: [],
  tags: [],
  
  loading: {
    services: false,
    deployments: false,
    creating: false,
    updating: false,
    deleting: false,
  },
  
  pagination: {
    services: {
      page: 0,
      size: 20,
      total: 0,
      totalPages: 0,
    },
    deployments: {
      page: 0,
      size: 20,
      total: 0,
      totalPages: 0,
    },
  },
};

export const useMcpStore = create<McpState>()(
  devtools(
    (set, get) => ({
      ...initialState,
      
      // 服务相关 actions
      setServices: (services) => set({ services }),
      
      setFeaturedServices: (featuredServices) => set({ featuredServices }),
      
      setUserServices: (userServices) => set({ userServices }),
      
      setCurrentService: (currentService) => set({ currentService }),
      
      addService: (service) => set((state) => ({
        services: [service, ...state.services],
        userServices: service.authorId === getCurrentUserId() 
          ? [service, ...state.userServices] 
          : state.userServices,
      })),
      
      updateService: (service) => set((state) => ({
        services: state.services.map(s => s.id === service.id ? service : s),
        userServices: state.userServices.map(s => s.id === service.id ? service : s),
        featuredServices: state.featuredServices.map(s => s.id === service.id ? service : s),
        currentService: state.currentService?.id === service.id ? service : state.currentService,
      })),
      
      removeService: (id) => set((state) => ({
        services: state.services.filter(s => s.id !== id),
        userServices: state.userServices.filter(s => s.id !== id),
        featuredServices: state.featuredServices.filter(s => s.id !== id),
        currentService: state.currentService?.id === id ? null : state.currentService,
      })),
      
      // 部署相关 actions
      setDeployments: (deployments) => set({ deployments }),
      
      setCurrentDeployment: (currentDeployment) => set({ currentDeployment }),
      
      addDeployment: (deployment) => set((state) => ({
        deployments: [deployment, ...state.deployments],
      })),
      
      updateDeployment: (deployment) => set((state) => ({
        deployments: state.deployments.map(d => d.id === deployment.id ? deployment : d),
        currentDeployment: state.currentDeployment?.id === deployment.id ? deployment : state.currentDeployment,
      })),
      
      removeDeployment: (id) => set((state) => ({
        deployments: state.deployments.filter(d => d.id !== id),
        currentDeployment: state.currentDeployment?.id === id ? null : state.currentDeployment,
      })),
      
      // 搜索和过滤 actions
      setSearchParams: (params) => set((state) => ({
        searchParams: { ...state.searchParams, ...params },
      })),
      
      setCategories: (categories) => set({ categories }),
      
      setTags: (tags) => set({ tags }),
      
      // 加载状态 actions
      setLoading: (key, value) => set((state) => ({
        loading: { ...state.loading, [key]: value },
      })),
      
      // 分页 actions
      setPagination: (type, pagination) => set((state) => ({
        pagination: {
          ...state.pagination,
          [type]: { ...state.pagination[type], ...pagination },
        },
      })),
      
      // 重置状态
      reset: () => set(initialState),
    }),
    {
      name: 'mcp-store',
    }
  )
);

// 辅助函数：获取当前用户ID（需要根据实际认证系统实现）
function getCurrentUserId(): number | null {
  // 这里应该从认证状态中获取当前用户ID
  // 暂时返回 null，需要根据实际情况实现
  return null;
}

// 选择器函数
export const mcpSelectors = {
  // 获取已发布的服务
  getPublishedServices: (state: McpState) => 
    state.services.filter(service => service.status === 'PUBLISHED'),
  
  // 获取运行中的部署
  getRunningDeployments: (state: McpState) => 
    state.deployments.filter(deployment => deployment.status === 'RUNNING'),
  
  // 获取失败的部署
  getFailedDeployments: (state: McpState) => 
    state.deployments.filter(deployment => deployment.status === 'FAILED'),
  
  // 根据分类过滤服务
  getServicesByCategory: (state: McpState, category: string) => 
    state.services.filter(service => service.category === category),
  
  // 根据标签过滤服务
  getServicesByTag: (state: McpState, tag: string) => 
    state.services.filter(service => service.tags.includes(tag)),
  
  // 获取高评分服务
  getHighRatedServices: (state: McpState, minRating = 4.0) => 
    state.services.filter(service => service.ratingAverage >= minRating),
  
  // 获取免费服务
  getFreeServices: (state: McpState) => 
    state.services.filter(service => service.priceModel === 'FREE'),
  
  // 获取付费服务
  getPaidServices: (state: McpState) => 
    state.services.filter(service => service.priceModel === 'PAID'),
};