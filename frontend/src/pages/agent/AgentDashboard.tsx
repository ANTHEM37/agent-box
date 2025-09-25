import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Table, Tag, Space, Button, message } from 'antd'
import { PlayCircleOutlined, PauseCircleOutlined, StopOutlined, EyeOutlined } from '@ant-design/icons'
import { agentApi, AgentInstance, InstanceStatus, Task, TaskStatus } from '@/services/api/agent'
import type { ColumnsType } from 'antd/es/table'

const AgentDashboard: React.FC = () => {
  const [stats, setStats] = useState({
    totalInstances: 0,
    runningInstances: 0,
    totalTasks: 0,
    pendingTasks: 0
  })
  const [recentInstances, setRecentInstances] = useState<AgentInstance[]>([])
  const [recentTasks, setRecentTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(false)

  const loadDashboardData = async () => {
    setLoading(true)
    try {
      // 获取实例统计
      const instanceStats = await agentApi.getInstanceStats()
      // 获取任务统计
      const taskStats = await agentApi.getTaskStats()
      // 获取最近实例
      const instancesResponse = await agentApi.getAgentInstances(0, 5)
      // 获取最近任务
      const tasksResponse = await agentApi.getTasks(0, 5)

      setStats({
        totalInstances: instanceStats?.totalInstances || 0,
        runningInstances: instanceStats?.runningInstances || 0,
        totalTasks: taskStats?.totalTasks || 0,
        pendingTasks: taskStats?.pendingTasks || 0
      })
      setRecentInstances(instancesResponse.content || [])
      setRecentTasks(tasksResponse.content || [])
    } catch (error) {
      message.error('加载仪表板数据失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDashboardData()
  }, [])

  const handleStartInstance = async (id: number) => {
    try {
      await agentApi.startAgentInstance(id)
      message.success('实例启动成功')
      loadDashboardData()
    } catch (error) {
      message.error('启动失败')
    }
  }

  const handleStopInstance = async (id: number) => {
    try {
      await agentApi.stopAgentInstance(id)
      message.success('实例停止成功')
      loadDashboardData()
    } catch (error) {
      message.error('停止失败')
    }
  }

  const getStatusColor = (status: InstanceStatus) => {
    const colors = {
      [InstanceStatus.CREATED]: 'blue',
      [InstanceStatus.RUNNING]: 'success',
      [InstanceStatus.PAUSED]: 'orange',
      [InstanceStatus.STOPPED]: 'default',
      [InstanceStatus.DESTROYED]: 'error'
    }
    return colors[status] || 'default'
  }

  const getStatusText = (status: InstanceStatus) => {
    const texts = {
      [InstanceStatus.CREATED]: '已创建',
      [InstanceStatus.RUNNING]: '运行中',
      [InstanceStatus.PAUSED]: '已暂停',
      [InstanceStatus.STOPPED]: '已停止',
      [InstanceStatus.DESTROYED]: '已销毁'
    }
    return texts[status] || status
  }

  const getTaskStatusColor = (status: TaskStatus) => {
    const colors = {
      [TaskStatus.PENDING]: 'blue',
      [TaskStatus.RUNNING]: 'orange',
      [TaskStatus.COMPLETED]: 'success',
      [TaskStatus.FAILED]: 'error',
      [TaskStatus.CANCELLED]: 'default'
    }
    return colors[status] || 'default'
  }

  const getTaskStatusText = (status: TaskStatus) => {
    const texts = {
      [TaskStatus.PENDING]: '等待中',
      [TaskStatus.RUNNING]: '执行中',
      [TaskStatus.COMPLETED]: '已完成',
      [TaskStatus.FAILED]: '已失败',
      [TaskStatus.CANCELLED]: '已取消'
    }
    return texts[status] || status
  }

  const instanceColumns: ColumnsType<AgentInstance> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60
    },
    {
      title: '智能体定义',
      dataIndex: 'agentDefinition',
      key: 'agentDefinition',
      render: (definition) => definition?.name || '-'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: InstanceStatus) => (
        <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} size="small">
            查看
          </Button>
          {record.status === InstanceStatus.CREATED && (
            <Button 
              type="link" 
              icon={<PlayCircleOutlined />}
              size="small"
              onClick={() => handleStartInstance(record.id)}
            >
              启动
            </Button>
          )}
          {record.status === InstanceStatus.RUNNING && (
            <Button 
              type="link" 
              icon={<StopOutlined />}
              size="small"
              onClick={() => handleStopInstance(record.id)}
            >
              停止
            </Button>
          )}
        </Space>
      )
    }
  ]

  const taskColumns: ColumnsType<Task> = [
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
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: TaskStatus) => (
        <Tag color={getTaskStatusColor(status)}>{getTaskStatusText(status)}</Tag>
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
    }
  ]

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总实例数"
              value={stats.totalInstances}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="运行中实例"
              value={stats.runningInstances}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总任务数"
              value={stats.totalTasks}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="等待中任务"
              value={stats.pendingTasks}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col span={12}>
          <Card title="最近智能体实例" loading={loading}>
            <Table
              columns={instanceColumns}
              dataSource={recentInstances}
              rowKey="id"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="最近任务" loading={loading}>
            <Table
              columns={taskColumns}
              dataSource={recentTasks}
              rowKey="id"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default AgentDashboard