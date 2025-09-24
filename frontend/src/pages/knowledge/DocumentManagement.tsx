import React, { useState } from 'react'
import { 
  Card, 
  Button, 
  Table, 
  Upload, 
  message, 
  Space, 
  Tag, 
  Progress,
  Popconfirm,
  Modal
} from 'antd'
import { 
  UploadOutlined, 
  DeleteOutlined, 
  PlayCircleOutlined,
  FileTextOutlined,
  EyeOutlined
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { knowledgeApi } from '../../services/api/knowledge'
import type { Document } from '../../types/knowledge'
import type { UploadFile } from 'antd'

const { Dragger } = Upload

export default function DocumentManagement() {
  const { knowledgeBaseId } = useParams<{ knowledgeBaseId: string }>()
  const [uploadVisible, setUploadVisible] = useState(false)
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const queryClient = useQueryClient()

  const kbId = parseInt(knowledgeBaseId || '0')

  // 获取知识库信息
  const { data: knowledgeBase } = useQuery({
    queryKey: ['knowledgeBase', kbId],
    queryFn: () => knowledgeApi.getKnowledgeBase(kbId),
    enabled: !!kbId
  })

  // 获取文档列表
  const { data: documents, isLoading } = useQuery({
    queryKey: ['documents', kbId],
    queryFn: () => knowledgeApi.getDocuments(kbId),
    enabled: !!kbId
  })

  // 上传文档
  const uploadMutation = useMutation({
    mutationFn: (file: File) => knowledgeApi.uploadDocument(kbId, file),
    onSuccess: () => {
      message.success('文档上传成功')
      setFileList([])
      setUploadVisible(false)
      queryClient.invalidateQueries({ queryKey: ['documents', kbId] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '上传失败')
    }
  })

  // 处理文档
  const processMutation = useMutation({
    mutationFn: (documentId: number) => knowledgeApi.processDocument(kbId, documentId),
    onSuccess: () => {
      message.success('文档处理已开始')
      queryClient.invalidateQueries({ queryKey: ['documents', kbId] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '处理失败')
    }
  })

  // 删除文档
  const deleteMutation = useMutation({
    mutationFn: (documentId: number) => knowledgeApi.deleteDocument(kbId, documentId),
    onSuccess: () => {
      message.success('文档删除成功')
      queryClient.invalidateQueries({ queryKey: ['documents', kbId] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '删除失败')
    }
  })

  const handleUpload = () => {
    if (fileList.length === 0) {
      message.warning('请选择要上传的文件')
      return
    }

    const file = fileList[0].originFileObj
    if (file) {
      uploadMutation.mutate(file)
    }
  }

  const handleProcess = (documentId: number) => {
    processMutation.mutate(documentId)
  }

  const handleDelete = (documentId: number) => {
    deleteMutation.mutate(documentId)
  }

  const getStatusTag = (status: string) => {
    const statusMap = {
      UPLOADED: { color: 'blue', text: '已上传' },
      PROCESSING: { color: 'orange', text: '处理中' },
      PROCESSED: { color: 'green', text: '已处理' },
      ERROR: { color: 'red', text: '错误' },
      DELETED: { color: 'default', text: '已删除' }
    }
    const config = statusMap[status as keyof typeof statusMap] || { color: 'default', text: status }
    return <Tag color={config.color}>{config.text}</Tag>
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const columns = [
    {
      title: '文件名',
      dataIndex: 'originalFilename',
      key: 'originalFilename',
      render: (text: string, record: Document) => (
        <Space>
          <FileTextOutlined />
          <span>{text}</span>
        </Space>
      )
    },
    {
      title: '文件类型',
      dataIndex: 'fileType',
      key: 'fileType',
      render: (type: string) => <Tag>{type.toUpperCase()}</Tag>
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      render: (size: number) => formatFileSize(size)
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => getStatusTag(status)
    },
    {
      title: '上传时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Document) => (
        <Space>
          {record.status === 'UPLOADED' && (
            <Button 
              type="link" 
              icon={<PlayCircleOutlined />}
              onClick={() => handleProcess(record.id)}
              loading={processMutation.isPending}
            >
              处理
            </Button>
          )}
          <Popconfirm
            title="确定要删除这个文档吗？"
            description="删除后无法恢复，请谨慎操作。"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button 
              type="link" 
              danger 
              icon={<DeleteOutlined />}
              loading={deleteMutation.isPending}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const uploadProps = {
    fileList,
    onChange: ({ fileList }: { fileList: UploadFile[] }) => setFileList(fileList),
    beforeUpload: (file: File) => {
      // 检查文件类型
      const allowedTypes = ['application/pdf', 'application/msword', 
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'text/plain', 'text/markdown']
      
      if (!allowedTypes.includes(file.type)) {
        message.error('只支持 PDF、Word、TXT、Markdown 格式的文件')
        return false
      }

      // 检查文件大小（10MB）
      if (file.size > 10 * 1024 * 1024) {
        message.error('文件大小不能超过 10MB')
        return false
      }

      return false // 阻止自动上传
    },
    onRemove: () => {
      setFileList([])
    }
  }

  return (
    <div className="p-6">
      <Card>
        <div className="flex justify-between items-center mb-6">
          <div>
            <h2 className="text-xl font-semibold">文档管理</h2>
            {knowledgeBase && (
              <p className="text-gray-600 mt-1">
                知识库：{knowledgeBase.name} | 
                文档数：{knowledgeBase.documentCount} | 
                Token数：{knowledgeBase.totalTokens?.toLocaleString()}
              </p>
            )}
          </div>
          <Button 
            type="primary" 
            icon={<UploadOutlined />}
            onClick={() => setUploadVisible(true)}
          >
            上传文档
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={documents?.content || []}
          rowKey="id"
          loading={isLoading}
          pagination={{
            total: documents?.totalElements || 0,
            pageSize: documents?.size || 10,
            current: (documents?.number || 0) + 1,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
        />
      </Card>

      <Modal
        title="上传文档"
        open={uploadVisible}
        onOk={handleUpload}
        onCancel={() => {
          setUploadVisible(false)
          setFileList([])
        }}
        confirmLoading={uploadMutation.isPending}
        width={600}
      >
        <div className="py-4">
          <Dragger {...uploadProps} className="mb-4">
            <p className="ant-upload-drag-icon">
              <UploadOutlined />
            </p>
            <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p className="ant-upload-hint">
              支持 PDF、Word、TXT、Markdown 格式，文件大小不超过 10MB
            </p>
          </Dragger>
          
          {fileList.length > 0 && (
            <div className="mt-4 p-4 bg-gray-50 rounded">
              <h4 className="mb-2">待上传文件：</h4>
              <p><strong>文件名：</strong>{fileList[0].name}</p>
              <p><strong>大小：</strong>{formatFileSize(fileList[0].size || 0)}</p>
              <p><strong>类型：</strong>{fileList[0].type}</p>
            </div>
          )}
        </div>
      </Modal>
    </div>
  )
}