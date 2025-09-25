import { Layout, Menu } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import { DatabaseOutlined, DeploymentUnitOutlined, HomeOutlined, PartitionOutlined } from '@ant-design/icons'
import type { PropsWithChildren } from 'react'

const { Header, Sider, Content } = Layout

export default function AppLayout({ children }: PropsWithChildren) {
  const navigate = useNavigate()
  const location = useLocation()

  const items = [
    { key: '/', icon: <HomeOutlined />, label: '概览' },
    { key: '/knowledge', icon: <DatabaseOutlined />, label: '知识库' },
    { 
      key: 'workflow-root', 
      icon: <PartitionOutlined />, 
      label: '工作流',
      children: [
        { key: '/workflows', label: '工作流列表' },
        { key: '/workflow/executions', label: '执行记录' }
      ]
    },
    { key: '/mcp', icon: <DeploymentUnitOutlined />, label: 'MCP 服务' },
  ]

  // 检查是否是工作流设计器页面
  const isWorkflowDesigner = location.pathname.includes('/workflow/create') || location.pathname.includes('/workflow/edit');

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div style={{ color: '#fff', padding: '16px', fontWeight: 600 }}>AI Agent</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={items}
          onClick={({ key }) => navigate(String(key))}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: 0 }} />
        <Content 
          style={{ 
            margin: '16px',
            height: isWorkflowDesigner ? 'calc(100vh - 64px - 32px)' : 'auto',
            overflow: isWorkflowDesigner ? 'hidden' : 'auto'
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  )
}