import { useEffect, useState } from 'react'
import { Button, Upload, Table, Typography, message, Space, Popconfirm } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import { documentApi, type DocumentItem } from '../../services/api/document'

export default function DocumentManagement({ knowledgeBaseId }: { knowledgeBaseId: number }) {
  const [data, setData] = useState<DocumentItem[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [loading, setLoading] = useState(false)

  const load = async (p = page, s = pageSize) => {
    setLoading(true)
    try {
      const resp = await documentApi.list(knowledgeBaseId, p - 1, s)
      setData(resp.content)
      setTotal(resp.totalElements)
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(1, pageSize)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [knowledgeBaseId])

  return (
    <div>
      <Typography.Title level={5}>文档管理</Typography.Title>

      <Upload
        showUploadList={false}
        accept={'.pdf,.doc,.docx,.txt,.md'}
        customRequest={async (options) => {
          const { file, onSuccess, onError } = options
          try {
            const anyFile = file as any
            const realFile: File = anyFile?.originFileObj || (file as File)
            if (!realFile) throw new Error('未获取到文件对象')
            await documentApi.upload(knowledgeBaseId, realFile)
            message.success('上传成功')
            onSuccess && onSuccess({}, new XMLHttpRequest())
            load(page, pageSize)
          } catch (e) {
            message.error('上传失败')
            onError && onError(e as any)
          }
        }}
      >
        <Button type="primary" icon={<UploadOutlined />}>上传文档</Button>
      </Upload>

      <Table
        style={{ marginTop: 12 }}
        rowKey="id"
        loading={loading}
        dataSource={data}
        pagination={{
          current: page,
          pageSize,
          total,
          onChange: (cp, ps) => { setPage(cp); setPageSize(ps); load(cp, ps) }
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '文件名', dataIndex: 'fileName' },
          { title: '类型', dataIndex: 'fileType' },
          { title: '大小', dataIndex: 'size' },
          { title: '状态', dataIndex: 'status' },
          {
            title: '操作',
            render: (_, row) => (
              <Space>
                <Button size="small" onClick={async () => {
                  try { await documentApi.process(knowledgeBaseId, row.id); message.success('已触发处理') } catch { message.error('处理失败') }
                }}>处理</Button>
                <Popconfirm title="确认删除该文档？" onConfirm={async () => {
                  try { await documentApi.remove(knowledgeBaseId, row.id); message.success('已删除'); load(page, pageSize) } catch { message.error('删除失败') }
                }}>
                  <Button size="small" danger>删除</Button>
                </Popconfirm>
              </Space>
            )
          }
        ]}
      />
    </div>
  )
}


