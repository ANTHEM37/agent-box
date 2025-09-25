// 通用类型定义

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  success: boolean
}

export interface ListResponse<T> {
  code: number
  message: string
  data: T[]
  success: boolean
}

export interface PaginationParams {
  page?: number
  size?: number
  sort?: string
}

export interface BaseEntity {
  id: number
  createdAt: string
  updatedAt: string
}

export interface SearchParams {
  keyword?: string
  status?: string
  type?: string
  startDate?: string
  endDate?: string
}