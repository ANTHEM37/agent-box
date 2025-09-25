import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Card, message, Select } from 'antd'
import { EyeOutlined, CheckCircleOutlined, SendOutlined } from '@ant-design/icons'
import { agentApi, Message, MessageType, MessageStatus } from '@/services/api/agent'
import type { ColumnsType } from 'antd/es/table'

const { Option } = Select

const MessageList: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([])
  const [loading, setLoading] = useState(false)
  const [statusFilter, setStatusFilter] = useState<MessageStatus | 'all'>('all')
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })

  const loadMessages = async (page = 1, pageSize = 10, status?: MessageStatus) => {
    setLoading(true)
    try {
      let response
      if (status && status !== 'all') {
        response = await agentApi.getMessagesByStatus(status)
        setMessages(response || [])
        setPagination({
          current: page,
          pageSize,
          total: response?.length || 0
        })
      } else {
        response = await agentApi.getMessages(page - 1, pageSize)
        setMessages(response.content || [])
        setPagination({
          current: page,
          pageSize,
          total: response.totalElements || 0
        })
      }
    } catch (error) {
      message.error('加载消息失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadMessages(1, 10, statusFilter === 'all' ? undefined : statusFilter)
  }, [statusFilter])

  const handleTableChange = (pagination: any) => {
    loadMessages(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
  }

  const handleStatusFilterChange = (value: MessageStatus | 'all') => {
    setStatusFilter(value)
    setPagination({ ...pagination, current: 1 })
  }

  const handleMarkAsRead = async (id: number) => {
    try {
      await agentApi.markMessageAsRead(id)
      message.success('标记为已读')
      loadMessages(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
    } catch (error) {
      message.error('操作失败')
    }
  }

  const handleMarkAsDelivered = async (id: number) => {
    try {
      await agentApi.markMessageAsDelivered(id)
      message.success('标记为已送达')
      loadMessages(pagination.current, pagination.pageSize, statusFilter === 'all' ? undefined : statusFilter)
    } catch (error) {
      message.error('操作失败')
    }
  }

  const getStatusColor = (status: MessageStatus) => {
    const colors = {
      [MessageStatus.SENT]: 'blue',
      [MessageStatus.DELIVERED]: 'orange',
      [MessageStatus.READ]: 'success',
      [MessageStatus.FAILED]: 'error'
    }
    return colors[status] || 'default'
  }

  const getStatusText = (status: MessageStatus) => {
    const texts = {
      [MessageStatus.SENT]: '已发送',
      [MessageStatus.DELIVERED]: '已送达',
      [MessageStatus.READ]: '已读',
      [MessageStatus.FAILED]: '发送失败'
    }
    return texts[status] || status
  }

  const getTypeText = (type: MessageType) => {
    const texts = {
      [MessageType.TEXT]: '文本消息',
      [MessageType.COLLABORATION_REQUEST]: '协作请求',
      [MessageType.TASK_RESULT]: '任务结果',
      [MessageType.SYSTEM_NOTIFICATION]: '系统通知'
    }
    return texts[type] || type
  }

  const columns: ColumnsType<Message> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60
    },
    {
      title: '发送者',
      dataIndex: 'sender',
      key: 'sender',
      render: (sender) => sender?.id || '-'
    },
    {
      title: '接收者',
      dataIndex: 'receiver',
      key: 'receiver',
      render: (receiver) => receiver?.id || '-'
    },
    {
      title: '消息类型',
      dataIndex: 'messageType',
      key: 'messageType',
      width: 120,
      render: (type: MessageType) => getTypeText(type)
    },
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
      render: (content: string) => content.length > 50 ? `${content.substring(0, 50)}...` : content
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: MessageStatus) => (
        <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
      )
    },
    {
      title: '发送时间',
      dataIndex: 'sentAt',
      key: 'sentAt',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString()
    },
    {
      title: '送达时间',
      dataIndex: 'deliveredAt',
      key: 'deliveredAt',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '阅读时间',
      dataIndex: 'readAt',
      key: 'readAt',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          {record.status === MessageStatus.SENT && (
            <Button 
              type="link" 
              icon={<CheckCircleOutlined />}
              onClick={() => handleMarkAsDelivered(record.id)}
            >
              送达
            </Button>
          )}
          {record.status === MessageStatus.DELIVERED && (
            <Button 
              type="link" 
              icon={<CheckCircleOutlined />}
              onClick={() => handleMarkAsRead(record.id)}
            >
              已读
            </Button>
          )}
        </Space>
      )
    }
  ]

  const statusOptions = [
    { value: 'all', label: '全部状态' },
    { value: MessageStatus.SENT, label: '已发送' },
    { value: MessageStatus.DELIVERED, label: '已送达' },
    { value: MessageStatus.READ, label: '已读' },
    { value: MessageStatus.FAILED, label: '发送失败' }
  ]

  return (
    <Card>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>消息管理</h2>
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
        dataSource={messages}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
      />
    </Card>
  )
}

export default MessageList