import React, { useState } from 'react'
import { 
  Card, 
  Input, 
  Button, 
  Select, 
  Space, 
  List, 
  Tag, 
  Slider, 
  Switch,
  Empty,
  Spin,
  Typography
} from 'antd'
import { SearchOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { useQuery, useMutation } from '@tanstack/react-query'
import { knowledgeApi } from '../../services/api/knowledge'
import type { SearchRequest, SearchResult, KnowledgeBase } from '../../types/knowledge'

const { TextArea } = Input
const { Option } = Select
const { Text, Paragraph } = Typography

export default function SearchInterface() {
  const [searchForm, setSearchForm] = useState<SearchRequest>({
    knowledgeBaseId: 0,
    query: '',
    topK: 5,
    threshold: 0.7,
    includeMetadata: true
  })
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searchType, setSearchType] = useState<'semantic' | 'hybrid'>('semantic')

  // 获取活跃知识库列表
  const { data: knowledgeBases } = useQuery({
    queryKey: ['activeKnowledgeBases'],
    queryFn: () => knowledgeApi.getActiveKnowledgeBases()
  })

  // 搜索
  const searchMutation = useMutation({
    mutationFn: (request: SearchRequest) => {
      return searchType === 'semantic' 
        ? knowledgeApi.semanticSearch(request)
        : knowledgeApi.hybridSearch(request)
    },
    onSuccess: (results) => {
      setSearchResults(results)
    },
    onError: (error: any) => {
      console.error('搜索失败:', error)
    }
  })

  const handleSearch = () => {
    if (!searchForm.knowledgeBaseId) {
      return
    }
    if (!searchForm.query.trim()) {
      return
    }

    searchMutation.mutate(searchForm)
  }

  const handleFormChange = (field: keyof SearchRequest, value: any) => {
    setSearchForm(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const highlightText = (text: string, query: string) => {
    if (!query) return text
    
    const regex = new RegExp(`(${query})`, 'gi')
    const parts = text.split(regex)
    
    return parts.map((part, index) => 
      regex.test(part) ? (
        <mark key={index} className="bg-yellow-200">{part}</mark>
      ) : (
        part
      )
    )
  }

  const getScoreColor = (score: number) => {
    if (score >= 0.8) return 'green'
    if (score >= 0.6) return 'orange'
    return 'red'
  }

  return (
    <div className="p-6">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 搜索配置面板 */}
        <div className="lg:col-span-1">
          <Card title="搜索配置" className="h-fit">
            <Space direction="vertical" className="w-full">
              <div>
                <label className="block text-sm font-medium mb-2">知识库</label>
                <Select
                  placeholder="选择知识库"
                  className="w-full"
                  value={searchForm.knowledgeBaseId || undefined}
                  onChange={(value) => handleFormChange('knowledgeBaseId', value)}
                >
                  {knowledgeBases?.map((kb: KnowledgeBase) => (
                    <Option key={kb.id} value={kb.id}>
                      {kb.name}
                    </Option>
                  ))}
                </Select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">搜索类型</label>
                <Select
                  value={searchType}
                  onChange={setSearchType}
                  className="w-full"
                >
                  <Option value="semantic">语义搜索</Option>
                  <Option value="hybrid">混合搜索</Option>
                </Select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  返回结果数: {searchForm.topK}
                </label>
                <Slider
                  min={1}
                  max={20}
                  value={searchForm.topK}
                  onChange={(value) => handleFormChange('topK', value)}
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  相似度阈值: {searchForm.threshold}
                </label>
                <Slider
                  min={0}
                  max={1}
                  step={0.1}
                  value={searchForm.threshold}
                  onChange={(value) => handleFormChange('threshold', value)}
                />
              </div>

              <div className="flex items-center justify-between">
                <label className="text-sm font-medium">包含元数据</label>
                <Switch
                  checked={searchForm.includeMetadata}
                  onChange={(checked) => handleFormChange('includeMetadata', checked)}
                />
              </div>
            </Space>
          </Card>
        </div>

        {/* 搜索界面和结果 */}
        <div className="lg:col-span-2">
          <Card>
            <Space direction="vertical" className="w-full">
              <div className="flex gap-2">
                <TextArea
                  placeholder="输入您的问题或关键词..."
                  value={searchForm.query}
                  onChange={(e) => handleFormChange('query', e.target.value)}
                  rows={3}
                  onPressEnter={(e) => {
                    if (e.ctrlKey || e.metaKey) {
                      handleSearch()
                    }
                  }}
                />
              </div>
              
              <div className="flex justify-between items-center">
                <Text type="secondary">
                  Ctrl/Cmd + Enter 快速搜索
                </Text>
                <Space>
                  <Button
                    type="primary"
                    icon={searchType === 'semantic' ? <SearchOutlined /> : <ThunderboltOutlined />}
                    onClick={handleSearch}
                    loading={searchMutation.isPending}
                    disabled={!searchForm.knowledgeBaseId || !searchForm.query.trim()}
                  >
                    {searchType === 'semantic' ? '语义搜索' : '混合搜索'}
                  </Button>
                </Space>
              </div>
            </Space>
          </Card>

          {/* 搜索结果 */}
          <Card title="搜索结果" className="mt-4">
            <Spin spinning={searchMutation.isPending}>
              {searchResults.length === 0 ? (
                <Empty 
                  description="暂无搜索结果"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              ) : (
                <List
                  dataSource={searchResults}
                  renderItem={(item, index) => (
                    <List.Item key={index}>
                      <div className="w-full">
                        <div className="flex justify-between items-start mb-2">
                          <Space>
                            <Tag color={getScoreColor(item.score)}>
                              相似度: {(item.score * 100).toFixed(1)}%
                            </Tag>
                            {item.documentName && (
                              <Tag color="blue">{item.documentName}</Tag>
                            )}
                            {item.chunkIndex !== undefined && (
                              <Tag color="purple">分块 #{item.chunkIndex + 1}</Tag>
                            )}
                          </Space>
                        </div>
                        
                        <Paragraph className="mb-2">
                          {highlightText(item.content, searchForm.query)}
                        </Paragraph>

                        {searchForm.includeMetadata && item.metadata && (
                          <div className="text-xs text-gray-500 mt-2">
                            <Space wrap>
                              {Object.entries(item.metadata).map(([key, value]) => (
                                <span key={key}>
                                  {key}: {String(value)}
                                </span>
                              ))}
                            </Space>
                          </div>
                        )}
                      </div>
                    </List.Item>
                  )}
                />
              )}
            </Spin>
          </Card>
        </div>
      </div>
    </div>
  )
}