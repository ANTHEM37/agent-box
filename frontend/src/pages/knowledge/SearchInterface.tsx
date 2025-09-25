import { useEffect, useState } from 'react'
import { Button, Card, Input, Radio, Select, Space, Table, Typography, message } from 'antd'
import { knowledgeApi, type KnowledgeBaseResponse } from '../../services/api/knowledge'
import { searchApi, type SearchResult } from '../../services/api/search'

export default function SearchInterface({ knowledgeBaseId: fixedKbId }: { knowledgeBaseId?: number }) {
  const [kbs, setKbs] = useState<KnowledgeBaseResponse[]>([])
  const [knowledgeBaseId, setKnowledgeBaseId] = useState<number | undefined>(fixedKbId)
  const [query, setQuery] = useState('')
  const [mode, setMode] = useState<'semantic' | 'hybrid'>('semantic')
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState<SearchResult[]>([])

  useEffect(() => {
    if (fixedKbId) {
      // 固定知识库：仍拉取列表用于显示名称，但禁用选择
      knowledgeApi.list(0, 100).then((p) => setKbs(p.content))
      setKnowledgeBaseId(fixedKbId)
    } else {
      knowledgeApi.list(0, 100).then((p) => {
        setKbs(p.content)
        if (!knowledgeBaseId && p.content.length) setKnowledgeBaseId(p.content[0].id)
      })
    }
  }, [fixedKbId])

  const onSearch = async () => {
    if (!knowledgeBaseId) return message.warning('请选择知识库')
    if (!query.trim()) return message.warning('请输入查询内容')
    setLoading(true)
    try {
      const req = { knowledgeBaseId, query }
      const data = mode === 'semantic' ? await searchApi.semantic(req) : await searchApi.hybrid(req)
      setResults(data)
    } catch (e: any) {
      message.error(e?.response?.data?.message || '搜索失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <Typography.Title level={4}>智能搜索</Typography.Title>
      <Card size="small" style={{ marginBottom: 12 }}>
        <Space wrap>
          <Select
            style={{ width: 240 }}
            placeholder="选择知识库"
            value={knowledgeBaseId}
            options={kbs.map(k => ({ label: k.name, value: k.id }))}
            onChange={setKnowledgeBaseId}
            disabled={!!fixedKbId}
          />
          <Input
            style={{ width: 420 }}
            placeholder="输入查询问题..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onPressEnter={onSearch}
          />
          <Radio.Group value={mode} onChange={(e) => setMode(e.target.value)}>
            <Radio.Button value="semantic">语义搜索</Radio.Button>
            <Radio.Button value="hybrid">混合搜索</Radio.Button>
          </Radio.Group>
          <Button type="primary" onClick={onSearch} loading={loading}>搜索</Button>
        </Space>
      </Card>

      <Table
        rowKey={(_, i) => String(i)}
        dataSource={results}
        columns={[
          { title: '相似度', dataIndex: 'score', width: 120, render: (v: number) => v?.toFixed(3) },
          { title: '内容片段', dataIndex: 'content' },
          { title: '来源', dataIndex: 'source', width: 180 },
        ]}
      />
    </div>
  )
}


