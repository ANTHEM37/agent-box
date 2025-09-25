import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Card, message, Popconfirm } from 'antd'
import { PlayCircleOutlined, PauseCircleOutlined, StopOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import { agentApi, AgentInstance, InstanceStatus } from '@/services/api/agent'
import type { ColumnsType } from 'antd/es/table'

const AgentInstanceList: React.FC = () => {
  const [instances, setInstances] = useState<AgentInstance[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })

  const loadInstances = async (page = 1, pageSize = 10) => {
    setLoading(true)
    try {
      const response = await agentApi.getAgentInstances(page - 1, pageSize)
      setInstances(response.content || [])
      setPagination({
        current: page,
        pageSize,
        total: response.totalElements || 0
      })
    } catch (error) {
      message.error('加载智能体实例失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadInstances()
  }, [])

  const handleTableChange = (pagination: any) => {
    loadInstances(pagination.current, pagination.pageSize)
  }

  const handleStart = async (id: number) => {
    try {
      await agentApi.startAgentInstance(id)
      message.success('启动成功')
      loadInstances(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('启动失败')
    }
  }

  const handleStop = async (id: number) => {
    try {
      await agentApi.stopAgentInstance(id)
      message.success('停止成功')
      loadInstances(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('停止失败')
    }
  }

  const handlePause = async (id: number) => {
    try {
      await agentApi.pauseAgentInstance(id)
      message.success('暂停成功')
      loadInstances(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('暂停失败')
    }
  }

  const handleResume = async (id: number) => {
    try {
      await agentApi.resumeAgentInstance(id)
      message.success('恢复成功')
      loadInstances(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('恢复失败')
    }
  }

  const handleDestroy = async (id: number) => {
    try {
      await agentApi.destroyAgentInstance(id)
      message.success('销毁成功')
      loadInstances(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('销毁失败')
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

  const columns: ColumnsType<AgentInstance> = [
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
      title: '会话ID',
      dataIndex: 'sessionId',
      key: 'sessionId',
      width: 120,
      ellipsis: true
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
      title: '创建者',
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 80
    },
    {
      title: '启动时间',
      dataIndex: 'startedAt',
      key: 'startedAt',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '最后活跃',
      dataIndex: 'lastActiveAt',
      key: 'lastActiveAt',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString()
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
          {record.status === InstanceStatus.CREATED && (
            <Button 
              type="link" 
              icon={<PlayCircleOutlined />}
              onClick={() => handleStart(record.id)}
            >
              启动
            </Button>
          )}
          {record.status === InstanceStatus.RUNNING && (
            <>
              <Button 
                type="link" 
                icon={<PauseCircleOutlined />}
                onClick={() => handlePause(record.id)}
              >
                暂停
              </Button>
              <Button 
                type="link" 
                icon={<StopOutlined />}
                onClick={() => handleStop(record.id)}
              >
                停止
              </Button>
            </>
          )}
          {record.status === InstanceStatus.PAUSED && (
            <Button 
              type="link" 
              icon={<PlayCircleOutlined />}
              onClick={() => handleResume(record.id)}
            >
              恢复
            </Button>
          )}
          <Popconfirm
            title="确定销毁这个智能体实例吗？"
            onConfirm={() => handleDestroy(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              销毁
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <Card>
      <div style={{ marginBottom: 16 }}>
        <h2>智能体实例管理</h2>
      </div>

      <Table
        columns={columns}
        dataSource={instances}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1000 }}
      />
    </Card>
  )
}

export default AgentInstanceList