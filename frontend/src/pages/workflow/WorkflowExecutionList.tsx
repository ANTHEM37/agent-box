import { useEffect, useState, useCallback } from 'react'
import { Table, Typography, message, Button, Space, Tag, Modal, Descriptions, Card, Row, Col, Form, Input, Select } from 'antd'
import { workflowApi } from '../../services/api/workflow'
import type { WorkflowExecution, PageResponse } from '../../types/workflow'

export default function WorkflowExecutionList() {
  const [data, setData] = useState<WorkflowExecution[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedExecution, setSelectedExecution] = useState<WorkflowExecution | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [searchForm] = Form.useForm()

  const load = async (page = 1, size = 20, searchParams: any = {}) => {
    setLoading(true)
    try {
      const response = await workflowApi.getUserExecutions({
        page: page - 1,
        size,
        ...searchParams
      })
      const pageData: PageResponse<WorkflowExecution> = response.data.data
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
  }, [currentPage, pageSize])

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

  const handleCancelExecution = async (executionId: number) => {
    try {
      await workflowApi.cancelExecution(executionId)
      message.success('已取消执行')
      load(currentPage, pageSize, searchForm.getFieldsValue())
    } catch (e: any) {
      message.error(e?.response?.data?.message || '取消失败')
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'RUNNING': return 'processing'
      case 'COMPLETED': return 'success'
      case 'FAILED': return 'error'
      case 'CANCELLED': return 'warning'
      default: return 'default'
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'RUNNING': return '运行中'
      case 'COMPLETED': return '已完成'
      case 'FAILED': return '失败'
      case 'CANCELLED': return '已取消'
      default: return status
    }
  }

  const showDetail = async (executionId: number) => {
    try {
      const response = await workflowApi.getExecution(executionId)
      setSelectedExecution(response.data.data)
      setDetailModalVisible(true)
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载详情失败')
    }
  }

  return (
    <div>
      <Typography.Title level={4}>执行记录</Typography.Title>
      
      {/* 搜索表单 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={searchForm} layout="inline" onFinish={handleSearch}>
          <Form.Item name="status" label="状态">
            <Select style={{ width: 120 }} allowClear>
              <Select.Option value="RUNNING">运行中</Select.Option>
              <Select.Option value="COMPLETED">已完成</Select.Option>
              <Select.Option value="FAILED">失败</Select.Option>
              <Select.Option value="CANCELLED">已取消</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="workflowId" label="工作流ID">
            <Input type="number" />
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
            title: '工作流名称', 
            dataIndex: 'workflowName',
            render: (_, row) => (
              <Button 
                type="link" 
                onClick={() => row.workflowId && window.open(`/workflow/edit/${row.workflowId}`, '_blank')}
              >
                {row.workflowName}
              </Button>
            )
          },
          { 
            title: '状态', 
            dataIndex: 'status',
            render: (status) => (
              <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
            )
          },
          { 
            title: '开始时间', 
            dataIndex: 'startedAt',
            render: (startedAt) => new Date(startedAt).toLocaleString()
          },
          { 
            title: '完成时间', 
            dataIndex: 'completedAt',
            render: (completedAt) => completedAt ? new Date(completedAt).toLocaleString() : '-'
          },
          { 
            title: '执行时长(ms)', 
            dataIndex: 'durationMs'
          },
          { 
            title: '进度', 
            dataIndex: 'progress',
            render: (progress) => progress ? `${Math.round(progress * 100)}%` : '-'
          },
          {
            title: '操作',
            render: (_, row) => (
              <Space>
                <a onClick={() => showDetail(row.id)}>查看详情</a>
                {row.status === 'RUNNING' && (
                  <a onClick={() => handleCancelExecution(row.id)}>取消执行</a>
                )}
              </Space>
            )
          }
        ]}
      />
      
      <Modal
        title="执行详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {selectedExecution && (
          <div>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="执行ID">{selectedExecution.id}</Descriptions.Item>
              <Descriptions.Item label="工作流名称">{selectedExecution.workflowName}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={getStatusColor(selectedExecution.status)}>
                  {getStatusText(selectedExecution.status)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="开始时间">
                {new Date(selectedExecution.startedAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="完成时间">
                {selectedExecution.completedAt ? new Date(selectedExecution.completedAt).toLocaleString() : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="执行时长">
                {selectedExecution.durationMs ? `${selectedExecution.durationMs} ms` : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="进度">
                {selectedExecution.progress ? `${Math.round(selectedExecution.progress * 100)}%` : '-'}
              </Descriptions.Item>
              {selectedExecution.errorMessage && (
                <Descriptions.Item label="错误信息">
                  <div style={{ color: 'red', whiteSpace: 'pre-wrap' }}>
                    {selectedExecution.errorMessage}
                  </div>
                </Descriptions.Item>
              )}
            </Descriptions>
            
            <div style={{ marginTop: 20 }}>
              <Typography.Title level={5}>输入数据</Typography.Title>
              <pre style={{ 
                backgroundColor: '#f5f5f5', 
                padding: 10, 
                borderRadius: 4,
                maxHeight: 200,
                overflow: 'auto'
              }}>
                {selectedExecution.inputData ? JSON.stringify(selectedExecution.inputData, null, 2) : '无'}
              </pre>
            </div>
            
            <div style={{ marginTop: 20 }}>
              <Typography.Title level={5}>输出数据</Typography.Title>
              <pre style={{ 
                backgroundColor: '#f5f5f5', 
                padding: 10, 
                borderRadius: 4,
                maxHeight: 200,
                overflow: 'auto'
              }}>
                {selectedExecution.outputData ? JSON.stringify(selectedExecution.outputData, null, 2) : '无'}
              </pre>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}