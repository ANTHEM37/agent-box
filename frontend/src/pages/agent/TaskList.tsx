import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Card, message, Popconfirm, Select } from 'antd'
import { EyeOutlined, PlayCircleOutlined, StopOutlined, CloseCircleOutlined, RedoOutlined } from '@ant-design/icons'
import { agentApi, Task, TaskStatus, TaskType, TaskSubType } from '@/services/api/agent'
import type { ColumnsType } from 'antd/es/table'

const { Option } = Select

const TaskList: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(false)
  const [statusFilter, setStatusFilter] = useState<TaskStatus | 'all'>('all')
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })

  const loadTasks = async (page = 1, pageSize = 10, status?: TaskStatus) => {
    setLoading(true)
    try {
      let response
      if (status && status !== 'all') {
        response = await agentApi.getTasksByStatus(status)
        setTasks(response || [])
        setPagination({
          current: page,
          pageSize,
          total: response?.length || 0
        })
      } else {
        response = await agentApi.getTasks(page - 1, pageSize)
        setTasks(response.content || [])
        setPagination({
          current: page,
          pageSize,
          total: response.totalElements || 0
        })
      }
    } catch (error) {
      message.error('加载任务失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTasks(1, 10, statusFilter === 'all' ? undefined : statusFilter)
  }, [statusFilter])

  const handleTableChange = (pagination: any) => {
    loadTasks(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
  }

  const handleStatusFilterChange = (value: TaskStatus | 'all') => {
    setStatusFilter(value)
    setPagination({ ...pagination, current: 1 })
  }

  const handleStart = async (id: number) => {
    try {
      await agentApi.startTaskExecution(id)
      message.success('任务开始执行')
      loadTasks(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
    } catch (error) {
      message.error('启动失败')
    }
  }

  const handleComplete = async (id: number) => {
    try {
      await agentApi.completeTask(id, '任务完成')
      message.success('任务完成')
      loadTasks(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
    } catch (error) {
      message.error('完成失败')
    }
  }

  const handleCancel = async (id: number) => {
    try {
      await agentApi.cancelTask(id)
      message.success('任务取消')
      loadTasks(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
    } catch (error) {
      message.error('取消失败')
    }
  }

  const handleRetry = async (id: number) => {
    try {
      await agentApi.retryTask(id)
      message.success('任务重试')
      loadTasks(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
    } catch (error) {
      message.error('重试失败')
    }
  }

  const getStatusColor = (status: TaskStatus) => {
    const colors = {
      [TaskStatus.PENDING]: 'blue',
      [TaskStatus.RUNNING]: 'orange',
      [TaskStatus.COMPLETED]: 'success',
      [TaskStatus.FAILED]: 'error',
      [TaskStatus.CANCELLED]: 'default'
    }
    return colors[status] || 'default'
  }

  const getStatusText = (status: TaskStatus) => {
    const texts = {
      [TaskStatus.PENDING]: '等待中',
      [TaskStatus.RUNNING]: '执行中',
      [TaskStatus.COMPLETED]: '已完成',
      [TaskStatus.FAILED]: '已失败',
      [TaskStatus.CANCELLED]: '已取消'
    }
    return texts[status] || status
  }

  const getTypeText = (type: TaskType) => {
    const texts = {
      [TaskType.SINGLE]: '单任务',
      [TaskType.SEQUENTIAL]: '顺序任务',
      [TaskType.PARALLEL]: '并行任务',
      [TaskType.WORKFLOW]: '工作流'
    }
    return texts[type] || type
  }

  const getSubTypeText = (subType: TaskSubType) => {
    const texts = {
      [TaskSubType.TEXT_PROCESSING]: '文本处理',
      [TaskSubType.DATA_ANALYSIS]: '数据分析',
      [TaskSubType.DECISION_MAKING]: '决策制定',
      [TaskSubType.COLLABORATIVE]: '协作任务'
    }
    return texts[subType] || subType
  }

  const columns: ColumnsType<Task> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      width: 150
    },
    {
      title: '智能体实例',
      dataIndex: 'agentInstance',
      key: 'agentInstance',
      render: (instance) => instance?.id || '-'
    },
    {
      title: '任务类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: TaskType) => getTypeText(type)
    },
    {
      title: '子类型',
      dataIndex: 'subType',
      key: 'subType',
      width: 100,
      render: (subType: TaskSubType) => getSubTypeText(subType)
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: TaskStatus) => (
        <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
      )
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      render: (priority: number) => (
        <Tag color={priority > 5 ? 'red' : priority > 3 ? 'orange' : 'blue'}>
          {priority}
        </Tag>
      )
    },
    {
      title: '重试次数',
      dataIndex: 'retryCount',
      key: 'retryCount',
      width: 80
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '完成时间',
      dataIndex: 'completedAt',
      key: 'completedAt',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          {record.status === TaskStatus.PENDING && (
            <Button 
              type="link" 
              icon={<PlayCircleOutlined />}
              onClick={() => handleStart(record.id)}
            >
              开始
            </Button>
          )}
          {record.status === TaskStatus.RUNNING && (
            <Button 
              type="link" 
              icon={<StopOutlined />}
              onClick={() => handleComplete(record.id)}
            >
              完成
            </Button>
          )}
          {(record.status === TaskStatus.PENDING || record.status === TaskStatus.RUNNING) && (
            <Popconfirm
              title="确定取消这个任务吗？"
              onConfirm={() => handleCancel(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link" danger icon={<CloseCircleOutlined />}>
                取消
              </Button>
            </Popconfirm>
          )}
          {record.status === TaskStatus.FAILED && (
            <Button 
              type="link" 
              icon={<RedoOutlined />}
              onClick={() => handleRetry(record.id)}
            >
              重试
            </Button>
          )}
        </Space>
      )
    }
  ]

  const statusOptions = [
    { value: 'all', label: '全部状态' },
    { value: TaskStatus.PENDING, label: '等待中' },
    { value: TaskStatus.RUNNING, label: '执行中' },
    { value: TaskStatus.COMPLETED, label: '已完成' },
    { value: TaskStatus.FAILED, label: '已失败' },
    { value: TaskStatus.CANCELLED, label: '已取消' }
  ]

  return (
    <Card>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>任务管理</h2>
        <Select
          value={statusFilter}
          onChange={handleStatusFilterChange}
          style={{ width: 120 }}
        >
          {statusOptions.map(option => (
            <Option key={option.value} value={option.value}>
              {option.label}
            </Option>
          ))}
        </Select>
      </div>

      <Table
        columns={columns}
        dataSource={tasks}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
      />
    </Card>
  )
}

export default TaskList