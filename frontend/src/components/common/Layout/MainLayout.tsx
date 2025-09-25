import React from 'react'
import { Layout, Menu, Avatar, Dropdown, Space, Button } from 'antd'
import { 
  DashboardOutlined, 
  DatabaseOutlined, 
  SearchOutlined,
  RobotOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '../../../stores/auth'

const { Header, Sider, Content } = Layout

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '仪表板'
    },
    {
      key: '/agent',
      icon: <RobotOutlined />,
      label: '智能体管理',
      children: [
        {
          key: '/agent',
          label: '智能体仪表板'
        },
        {
          key: '/agent/definitions',
          label: '智能体定义'
        },
        {
          key: '/agent/instances',
          label: '智能体实例'
        },
        {
          key: '/agent/tasks',
          label: '任务管理'
        },
        {
          key: '/agent/messages',
          label: '消息管理'
        }
      ]
    },
    {
      key: '/knowledge',
      icon: <DatabaseOutlined />,
      label: '知识库'
    },
    {
      key: '/search',
      icon: <SearchOutlined />,
      label: '智能搜索'
    }
  ]

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料'
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '设置'
    },
    {
      type: 'divider' as const
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        logout()
        navigate('/login')
      }
    }
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
  }

  return (
    <Layout className="min-h-screen">
      <Sider
        theme="light"
        width={250}
        className="shadow-md"
      >
        <div className="h-16 flex items-center justify-center border-b">
          <h1 className="text-lg font-bold text-blue-600">
            AI Agent Platform
          </h1>
        </div>
        
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
          className="border-r-0"
        />
      </Sider>

      <Layout>
        <Header className="bg-white shadow-sm px-6 flex justify-between items-center">
          <div className="text-lg font-medium">
            {menuItems.find(item => item.key === location.pathname)?.label || ''}
          </div>
          
          <Space>
            <Dropdown
              menu={{ items: userMenuItems }}
              placement="bottomRight"
              trigger={['click']}
            >
              <Button type="text" className="flex items-center">
                <Space>
                  <Avatar 
                    size="small" 
                    icon={<UserOutlined />}
                    src={user?.avatarUrl}
                  />
                  <span>{user?.fullName || user?.username}</span>
                </Space>
              </Button>
            </Dropdown>
          </Space>
        </Header>

        <Content className="bg-gray-50">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}