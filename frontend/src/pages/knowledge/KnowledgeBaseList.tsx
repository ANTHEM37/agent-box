import { useEffect, useState } from 'react'
import { Button, Flex, Input, Space, Table, Typography, message, Popconfirm, Switch, Drawer, Descriptions } from 'antd'
import { knowledgeApi, type KnowledgeBaseResponse } from '../../services/api/knowledge'
import KnowledgeBaseModal from './KnowledgeBaseModal'
import DocumentManagement from './DocumentManagement'
import SearchInterface from './SearchInterface'

export default function KnowledgeBaseList() {
  const [data, setData] = useState<KnowledgeBaseResponse[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<KnowledgeBaseResponse | null>(null)
  const [onlyActive, setOnlyActive] = useState(false)
  const [detail, setDetail] = useState<KnowledgeBaseResponse | null>(null)

  const load = async (p = page, s = pageSize) => {
    setLoading(true)
    try {
      if (onlyActive) {
        const list = await knowledgeApi.active()
        setData(list)
        setTotal(list.length)
      } else if (keyword.trim()) {
        const list = await knowledgeApi.search(keyword.trim())
        setData(list)
        setTotal(list.length)
      } else {
        const pageResp = await knowledgeApi.list(p - 1, s)
        setData(pageResp.content)
        setTotal(pageResp.totalElements)
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(1, pageSize)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div>
      <Flex align="center" justify="space-between" style={{ marginBottom: 12 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>知识库列表</Typography.Title>
        <Space>
          <span>仅活跃</span>
          <Switch checked={onlyActive} onChange={(v) => { setOnlyActive(v); load(1, pageSize) }} />
          <Input.Search
            allowClear
            placeholder="搜索名称..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onSearch={() => load(1, pageSize)}
          />
          <Button type="primary" onClick={() => { setEditing(null); setModalOpen(true) }}>新建</Button>
        </Space>
      </Flex>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={data}
        pagination={{
          current: page,
          pageSize,
          total,
          onChange: (cp, ps) => {
            setPage(cp)
            setPageSize(ps)
            load(cp, ps)
          },
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '名称', dataIndex: 'name' },
          { title: '描述', dataIndex: 'description' },
          { title: '创建时间', dataIndex: 'createdAt' },
          {
            title: '操作',
            width: 180,
            render: (_, row) => (
              <Space>
                <Button size="small" onClick={async () => {
                  try { const d = await knowledgeApi.get(row.id); setDetail(d) } catch (e: any) { message.error('获取详情失败') }
                }}>查看</Button>
                <Button size="small" onClick={() => { setEditing(row); setModalOpen(true) }}>编辑</Button>
                <Popconfirm title="确认删除该知识库？" onConfirm={async () => {
                  try { await knowledgeApi.remove(row.id); message.success('已删除'); load(page, pageSize) } catch (e: any) { message.error(e?.response?.data?.message || '删除失败') }
                }}>
                  <Button size="small" danger>删除</Button>
                </Popconfirm>
              </Space>
            )
          }
        ]}
      />

      <KnowledgeBaseModal
        open={modalOpen}
        initialValues={editing ? { name: editing.name, description: editing.description } : undefined}
        onCancel={() => setModalOpen(false)}
        onOk={async (values) => {
          try {
            if (editing) {
              await knowledgeApi.update(editing.id, values)
              message.success('已更新')
            } else {
              await knowledgeApi.create(values)
              message.success('已创建')
            }
            setModalOpen(false)
            load(page, pageSize)
          } catch (e: any) {
            message.error(e?.response?.data?.message || '操作失败')
          }
        }}
      />

      <Drawer open={!!detail} width={920} onClose={() => setDetail(null)} title="知识库详情">
        {detail && (
          <div>
            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="名称">{detail.name}</Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>{detail.description || '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{detail.createdAt || '-'}</Descriptions.Item>
            </Descriptions>

            <div style={{ height: 12 }} />
            <DocumentManagement knowledgeBaseId={detail.id} />
            <div style={{ height: 12 }} />
            <SearchInterface knowledgeBaseId={detail.id} />
          </div>
        )}
      </Drawer>
    </div>
  )
}


