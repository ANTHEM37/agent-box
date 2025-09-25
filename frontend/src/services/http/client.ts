import axios, { 
  AxiosInstance, 
  AxiosRequestConfig, 
  AxiosResponse,
  InternalAxiosRequestConfig,
  AxiosRequestHeaders
} from 'axios';
import { createStandaloneNavigation } from '@utils/navigation';

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 处理重复的/api路径
    if (config.url?.startsWith('/api')) {
      config.url = config.url.replace(/^\/api/, '');
    }
    
    // 添加认证token
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers = config.headers || {} as AxiosRequestHeaders;
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: any) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error: any) => {
    if (error.response?.status === 401) {
      // token过期或无效，清除本地存储并跳转到登录页
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      const { navigate } = createStandaloneNavigation();
      navigate('/login');
    }
    return Promise.reject(error);
  }
);

export default apiClient;