import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Modal, message, Popconfirm, Card } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, PauseCircleOutlined } from '@ant-design/icons'
import { agentApi, AgentDefinition, AgentType } from '@/services/api/agent'
import type { ColumnsType } from 'antd/es/table'
import CreateAgentDefinitionModal from './components/CreateAgentDefinitionModal'

const AgentDefinitionList: React.FC = () => {
  const [definitions, setDefinitions] = useState<AgentDefinition[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingDefinition, setEditingDefinition] = useState<AgentDefinition | null>(null)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })

  const loadDefinitions = async (page = 1, pageSize = 10) => {
    setLoading(true)
    try {
      const response = await agentApi.getAgentDefinitions(page - 1, pageSize)
      setDefinitions(response.content || [])
      setPagination({
        current: page,
        pageSize,
        total: response.totalElements || 0
      })
    } catch (error) {
      message.error('加载智能体定义失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDefinitions()
  }, [])

  const handleTableChange = (pagination: any) => {
    loadDefinitions(pagination.current, pagination.pageSize)
  }

  const handleCreate = () => {
    setEditingDefinition(null)
    setModalVisible(true)
  }

  const handleEdit = (definition: AgentDefinition) => {
    setEditingDefinition(definition)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await agentApi.deleteAgentDefinition(id)
      message.success('删除成功')
      loadDefinitions(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleEnable = async (id: number) => {
    try {
      await agentApi.enableAgentDefinition(id)
      message.success('启用成功')
      loadDefinitions(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('启用失败')
    }
  }

  const handleDisable = async (id: number) => {
    try {
      await agentApi.disableAgentDefinition(id)
      message.success('禁用成功')
      loadDefinitions(pagination.current, pagination.pageSize)
    } catch (error) {
      message.error('禁用失败')
    }
  }

  const handleModalClose = () => {
    setModalVisible(false)
    setEditingDefinition(null)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    setEditingDefinition(null)
    loadDefinitions(pagination.current, pagination.pageSize)
  }

  const getTypeColor = (type: AgentType) => {
    const colors = {
      [AgentType.TEXT_PROCESSING]: 'blue',
      [AgentType.DATA_ANALYSIS]: 'green',
      [AgentType.DECISION_MAKING]: 'orange',
      [AgentType.COLLABORATIVE]: 'purple'
    }
    return colors[type] || 'default'
  }

  const columns: ColumnsType<AgentDefinition> = [
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
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: AgentType) => (
        <Tag color={getTypeColor(type)}>{type}</Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 80,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'success' : 'error'}>
          {enabled ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString()
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button 
            type="link" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          {record.enabled ? (
            <Button 
              type="link" 
              icon={<PauseCircleOutlined />}
              onClick={() => handleDisable(record.id)}
            >
              禁用
            </Button>
          ) : (
            <Button 
              type="link" 
              icon={<PlayCircleOutlined />}
              onClick={() => handleEnable(record.id)}
            >
              启用
            </Button>
          )}
          <Popconfirm
            title="确定删除这个智能体定义吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>智能体定义管理</h2>
          <Button 
            type="primary" 
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            新建智能体定义
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={definitions}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 1000 }}
        />
      </Card>

      <CreateAgentDefinitionModal
        visible={modalVisible}
        editingDefinition={editingDefinition}
        onClose={handleModalClose}
        onSuccess={handleModalSuccess}
      />
    </div>
  )
}

export default AgentDefinitionList