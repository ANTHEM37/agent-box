import { useEffect, useState, useCallback } from 'react'
import { Table, Typography, message, Button, Space, Tag, Modal, Input, Select, Form, Card, Row, Col } from 'antd'
import { useNavigate } from 'react-router-dom'
import { workflowApi } from '../../services/api/workflow'
import type { WorkflowItem, PageResponse } from '../../types/workflow'

export default function WorkflowList() {
  const navigate = useNavigate()
  const [data, setData] = useState<WorkflowItem[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [deleteModalVisible, setDeleteModalVisible] = useState(false)
  const [workflowToDelete, setWorkflowToDelete] = useState<WorkflowItem | null>(null)
  const [searchForm] = Form.useForm()
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [categories, setCategories] = useState<string[]>([])
  const [stats, setStats] = useState<Record<string, number>>({})
  const [copyModalVisible, setCopyModalVisible] = useState(false)
  const [workflowToCopy, setWorkflowToCopy] = useState<WorkflowItem | null>(null)
  const [copyForm] = Form.useForm()

  // 加载分类列表
  const loadCategories = useCallback(async () => {
    try {
      const response = await workflowApi.getUserCategories()
      setCategories(response.data.data)
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }, [])

  // 加载统计信息
  const loadStats = useCallback(async () => {
    try {
      const response = await workflowApi.getUserWorkflowStats()
      setStats(response.data.data)
    } catch (error) {
      console.error('加载统计信息失败:', error)
    }
  }, [])

  const load = async (page = 1, size = 20, searchParams: any = {}) => {
    setLoading(true)
    try {
      const response = await workflowApi.list({
        page: page - 1,
        size,
        ...searchParams
      })
      const pageData: PageResponse<WorkflowItem> = response.data.data
      setData(pageData.content || [])
      setTotal(pageData.totalElements || 0)
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(currentPage, pageSize)
    loadCategories()
    loadStats()
  }, [currentPage, pageSize, loadCategories, loadStats])

  const handleSearch = (values: any) => {
    load(1, pageSize, values)
    setCurrentPage(1)
  }

  const handleReset = () => {
    searchForm.resetFields()
    load(1, pageSize)
    setCurrentPage(1)
  }

  const handleTableChange = (pagination: any) => {
    setCurrentPage(pagination.current)
    setPageSize(pagination.pageSize)
    const searchValues = searchForm.getFieldsValue()
    load(pagination.current, pagination.pageSize, searchValues)
  }

  const handleDelete = async () => {
    if (!workflowToDelete) return
    
    try {
      await workflowApi.delete(workflowToDelete.id)
      message.success('删除成功')
      setDeleteModalVisible(false)
      setWorkflowToDelete(null)
      load(currentPage, pageSize, searchForm.getFieldsValue())
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除失败')
    }
  }

  const handleExecute = async (workflowId: number) => {
    try {
      await workflowApi.execute({ workflowId })
      message.success('已触发执行')
    } catch (e: any) {
      message.error(e?.response?.data?.message || '执行失败')
    }
  }

  const handlePublish = async (workflowId: number) => {
    try {
      await workflowApi.publish(workflowId)
      message.success('发布成功')
      load(currentPage, pageSize, searchForm.getFieldsValue())
    } catch (e: any) {
      message.error(e?.response?.data?.message || '发布失败')
    }
  }

  const handleArchive = async (workflowId: number) => {
    try {
      await workflowApi.archive(workflowId)
      message.success('归档成功')
      load(currentPage, pageSize, searchForm.getFieldsValue())
    } catch (e: any) {
      message.error(e?.response?.data?.message || '归档失败')
    }
  }

  const handleCopy = async (values: any) => {
    if (!workflowToCopy) return
    
    try {
      await workflowApi.copy(workflowToCopy.id, values.newName)
      message.success('复制成功')
      setCopyModalVisible(false)
      setWorkflowToCopy(null)
      load(currentPage, pageSize, searchForm.getFieldsValue())
    } catch (e: any) {
      message.error(e?.response?.data?.message || '复制失败')
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'default'
      case 'PUBLISHED': return 'success'
      case 'ARCHIVED': return 'warning'
      default: return 'default'
    }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <Typography.Title level={4}>工作流列表</Typography.Title>
        <Button 
          type="primary" 
          onClick={() => navigate('/workflow/create')}
        >
          创建工作流
        </Button>
      </div>
      
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card size="small">
            <div>总计</div>
            <div style={{ fontSize: 24, fontWeight: 'bold' }}>{stats.total || 0}</div>
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <div>草稿</div>
            <div style={{ fontSize: 24, fontWeight: 'bold' }}>{stats.draft || 0}</div>
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <div>已发布</div>
            <div style={{ fontSize: 24, fontWeight: 'bold' }}>{stats.published || 0}</div>
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <div>已归档</div>
            <div style={{ fontSize: 24, fontWeight: 'bold' }}>{stats.archived || 0}</div>
          </Card>
        </Col>
      </Row>
      
      {/* 搜索表单 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={searchForm} layout="inline" onFinish={handleSearch}>
          <Form.Item name="keyword" label="关键词">
            <Input placeholder="名称或描述" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select style={{ width: 120 }} allowClear>
              <Select.Option value="DRAFT">草稿</Select.Option>
              <Select.Option value="PUBLISHED">已发布</Select.Option>
              <Select.Option value="ARCHIVED">已归档</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="category" label="分类">
            <Select style={{ width: 120 }} allowClear>
              {categories.map(category => (
                <Select.Option key={category} value={category}>{category}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">搜索</Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
      
      <Table
        rowKey="id"
        loading={loading}
        dataSource={data}
        pagination={{ 
          current: currentPage,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100']
        }}
        onChange={handleTableChange}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { 
            title: '名称', 
            dataIndex: 'name',
            render: (_, row) => (
              <Button 
                type="link" 
                onClick={() => navigate(`/workflow/edit/${row.id}`)}
              >
                {row.name}
              </Button>
            )
          },
          { 
            title: '描述', 
            dataIndex: 'description',
            ellipsis: true
          },
          { 
            title: '状态', 
            dataIndex: 'status',
            render: (status) => (
              <Tag color={getStatusColor(status)}>{status}</Tag>
            )
          },
          { 
            title: '分类', 
            dataIndex: 'category'
          },
          { 
            title: '标签', 
            dataIndex: 'tags',
            render: (tags) => (
              <>
                {tags?.slice(0, 2).map((tag: string) => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
                {tags && tags.length > 2 && <span>+{tags.length - 2}</span>}
              </>
            )
          },
          { 
            title: '创建时间', 
            dataIndex: 'createdAt',
            render: (createdAt) => new Date(createdAt).toLocaleString()
          },
          {
            title: '操作',
            render: (_, row) => (
              <Space>
                <Button 
                  size="small" 
                  onClick={() => navigate(`/workflow/edit/${row.id}`)}
                >
                  编辑
                </Button>
                <Button 
                  size="small" 
                  onClick={() => handleExecute(row.id)}
                >
                  执行
                </Button>
                {row.status === 'DRAFT' && (
                  <Button 
                    size="small" 
                    onClick={() => handlePublish(row.id)}
                  >
                    发布
                  </Button>
                )}
                {row.status === 'PUBLISHED' && (
                  <Button 
                    size="small" 
                    onClick={() => handleArchive(row.id)}
                  >
                    归档
                  </Button>
                )}
                <Button 
                  size="small" 
                  onClick={() => {
                    setWorkflowToCopy(row)
                    setCopyModalVisible(true)
                  }}
                >
                  复制
                </Button>
                <Button 
                  size="small" 
                  danger
                  onClick={() => {
                    setWorkflowToDelete(row)
                    setDeleteModalVisible(true)
                  }}
                >
                  删除
                </Button>
              </Space>
            )
          }
        ]}
      />
      
      <Modal
        title="确认删除"
        open={deleteModalVisible}
        onOk={handleDelete}
        onCancel={() => {
          setDeleteModalVisible(false)
          setWorkflowToDelete(null)
        }}
        okText="确认"
        cancelText="取消"
      >
        <p>确定要删除工作流 "{workflowToDelete?.name}" 吗？此操作不可恢复。</p>
      </Modal>
      
      <Modal
        title="复制工作流"
        open={copyModalVisible}
        onOk={() => copyForm.submit()}
        onCancel={() => {
          setCopyModalVisible(false)
          setWorkflowToCopy(null)
          copyForm.resetFields()
        }}
        okText="确认"
        cancelText="取消"
      >
        <Form form={copyForm} onFinish={handleCopy}>
          <Form.Item 
            name="newName" 
            label="新名称" 
            initialValue={workflowToCopy?.name ? `${workflowToCopy.name}_副本` : ''}
            rules={[{ required: true, message: '请输入新名称' }]}
          >
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}