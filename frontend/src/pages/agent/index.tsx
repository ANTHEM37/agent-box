import React, { useState } from 'react'
import { Tabs, Card } from 'antd'
import { DashboardOutlined, RobotOutlined, PlayCircleOutlined, ScheduleOutlined, MessageOutlined } from '@ant-design/icons'
import AgentDashboard from './AgentDashboard'
import AgentDefinitionList from './AgentDefinitionList'
import AgentInstanceList from './AgentInstanceList'
import TaskList from './TaskList'
import MessageList from './MessageList'

const { TabPane } = Tabs

const AgentManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState('dashboard')

  const tabItems = [
    {
      key: 'dashboard',
      label: (
        <span>
          <DashboardOutlined />
          仪表板
        </span>
      ),
      children: <AgentDashboard />
    },
    {
      key: 'definitions',
      label: (
        <span>
          <RobotOutlined />
          智能体定义
        </span>
      ),
      children: <AgentDefinitionList />
    },
    {
      key: 'instances',
      label: (
        <span>
          <PlayCircleOutlined />
          智能体实例
        </span>
      ),
      children: <AgentInstanceList />
    },
    {
      key: 'tasks',
      label: (
        <span>
          <ScheduleOutlined />
          任务管理
        </span>
      ),
      children: <TaskList />
    },
    {
      key: 'messages',
      label: (
        <span>
          <MessageOutlined />
          消息管理
        </span>
      ),
      children: <MessageList />
    }
  ]

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          type="card"
          size="large"
        >
          {tabItems.map(item => (
            <TabPane key={item.key} tab={item.label}>
              {item.children}
            </TabPane>
          ))}
        </Tabs>
      </Card>
    </div>
  )
}

export default AgentManagement