import React, { useState } from 'react'
import { 
  Card, 
  Button, 
  Table, 
  Space, 
  Modal, 
  Form, 
  Input, 
  message,
  Popconfirm,
  Tag,
  Tooltip
} from 'antd'
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  FolderOpenOutlined,
  SearchOutlined 
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { knowledgeApi } from '../../services/api/knowledge'
import type { KnowledgeBase } from '../../types/knowledge'

const { TextArea } = Input

export default function KnowledgeBaseList() {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingKb, setEditingKb] = useState<KnowledgeBase | null>(null)
  const [form] = Form.useForm()
  const queryClient = useQueryClient()

  // 获取知识库列表
  const { data: knowledgeBases, isLoading } = useQuery({
    queryKey: ['knowledgeBases'],
    queryFn: () => knowledgeApi.getKnowledgeBases()
  })

  // 创建知识库
  const createMutation = useMutation({
    mutationFn: knowledgeApi.createKnowledgeBase,
    onSuccess: () => {
      message.success('知识库创建成功')
      setIsModalOpen(false)
      form.resetFields()
      queryClient.invalidateQueries({ queryKey: ['knowledgeBases'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '创建失败')
    }
  })

  // 更新知识库
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: any }) => 
      knowledgeApi.updateKnowledgeBase(id, data),
    onSuccess: () => {
      message.success('知识库更新成功')
      setIsModalOpen(false)
      setEditingKb(null)
      form.resetFields()
      queryClient.invalidateQueries({ queryKey: ['knowledgeBases'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '更新失败')
    }
  })

  // 删除知识库
  const deleteMutation = useMutation({
    mutationFn: knowledgeApi.deleteKnowledgeBase,
    onSuccess: () => {
      message.success('知识库删除成功')
      queryClient.invalidateQueries({ queryKey: ['knowledgeBases'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '删除失败')
    }
  })

  const handleCreate = () => {
    setEditingKb(null)
    form.resetFields()
    setIsModalOpen(true)
  }

  const handleEdit = (kb: KnowledgeBase) => {
    setEditingKb(kb)
    form.setFieldsValue({
      name: kb.name,
      description: kb.description,
      embeddingModel: kb.embeddingModel,
      chunkSize: kb.chunkSize,
      chunkOverlap: kb.chunkOverlap
    })
    setIsModalOpen(true)
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      if (editingKb) {
        updateMutation.mutate({ id: editingKb.id, data: values })
      } else {
        createMutation.mutate(values)
      }
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id)
  }

  const getStatusTag = (status: string) => {
    const statusMap = {
      ACTIVE: { color: 'green', text: '活跃' },
      INACTIVE: { color: 'red', text: '停用' },
      PROCESSING: { color: 'blue', text: '处理中' },
      ERROR: { color: 'red', text: '错误' }
    }
    const config = statusMap[status as keyof typeof statusMap] || { color: 'default', text: status }
    return <Tag color={config.color}>{config.text}</Tag>
  }

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: KnowledgeBase) => (
        <Space>
          <FolderOpenOutlined />
          <span>{text}</span>
        </Space>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <span>{text || '-'}</span>
        </Tooltip>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => getStatusTag(status)
    },
    {
      title: '文档数',
      dataIndex: 'documentCount',
      key: 'documentCount',
      render: (count: number) => count || 0
    },
    {
      title: 'Token数',
      dataIndex: 'totalTokens',
      key: 'totalTokens',
      render: (tokens: number) => tokens?.toLocaleString() || 0
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: KnowledgeBase) => (
        <Space>
          <Button 
            type="link" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个知识库吗？"
            description="删除后无法恢复，请谨慎操作。"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button 
              type="link" 
              danger 
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div className="p-6">
      <Card>
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold">知识库管理</h2>
          <Button 
            type="primary" 
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            创建知识库
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={knowledgeBases?.content || []}
          rowKey="id"
          loading={isLoading}
          pagination={{
            total: knowledgeBases?.totalElements || 0,
            pageSize: knowledgeBases?.size || 10,
            current: (knowledgeBases?.number || 0) + 1,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
        />
      </Card>

      <Modal
        title={editingKb ? '编辑知识库' : '创建知识库'}
        open={isModalOpen}
        onOk={handleSubmit}
        onCancel={() => {
          setIsModalOpen(false)
          setEditingKb(null)
          form.resetFields()
        }}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            embeddingModel: 'text-embedding-ada-002',
            chunkSize: 500,
            chunkOverlap: 50
          }}
        >
          <Form.Item
            name="name"
            label="知识库名称"
            rules={[
              { required: true, message: '请输入知识库名称' },
              { max: 100, message: '名称长度不能超过100个字符' }
            ]}
          >
            <Input placeholder="请输入知识库名称" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[
              { max: 500, message: '描述长度不能超过500个字符' }
            ]}
          >
            <TextArea 
              rows={3} 
              placeholder="请输入知识库描述（可选）" 
            />
          </Form.Item>

          <Form.Item
            name="embeddingModel"
            label="嵌入模型"
          >
            <Input placeholder="text-embedding-ada-002" />
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="chunkSize"
              label="分块大小"
              rules={[
                { required: true, message: '请输入分块大小' },
                { type: 'number', min: 100, max: 2000, message: '分块大小应在100-2000之间' }
              ]}
            >
              <Input type="number" placeholder="500" />
            </Form.Item>

            <Form.Item
              name="chunkOverlap"
              label="分块重叠"
              rules={[
                { required: true, message: '请输入分块重叠' },
                { type: 'number', min: 0, max: 200, message: '分块重叠应在0-200之间' }
              ]}
            >
              <Input type="number" placeholder="50" />
            </Form.Item>
          </div>
        </Form>
      </Modal>
    </div>
  )
}