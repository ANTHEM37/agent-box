import React from 'react'
import { Card, Row, Col, Statistic, Typography } from 'antd'
import {
  BookOutlined,
  ApartmentOutlined,
  ShopOutlined,
  UserOutlined,
} from '@ant-design/icons'

const { Title } = Typography

const Dashboard: React.FC = () => {
  return (
    <div>
      <Title level={2}>仪表板</Title>
      
      <Row gutter={[16, 16]} className="mb-6">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="知识库数量"
              value={5}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="工作流数量"
              value={12}
              prefix={<ApartmentOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="MCP服务"
              value={8}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={156}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>
      
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="最近活动" size="small">
            <div className="space-y-2">
              <div className="flex justify-between">
                <span>创建了新的知识库</span>
                <span className="text-gray-500">2小时前</span>
              </div>
              <div className="flex justify-between">
                <span>执行了工作流任务</span>
                <span className="text-gray-500">4小时前</span>
              </div>
              <div className="flex justify-between">
                <span>部署了MCP服务</span>
                <span className="text-gray-500">1天前</span>
              </div>
            </div>
          </Card>
        </Col>
        
        <Col xs={24} lg={12}>
          <Card title="系统状态" size="small">
            <div className="space-y-2">
              <div className="flex justify-between">
                <span>数据库连接</span>
                <span className="text-green-500">正常</span>
              </div>
              <div className="flex justify-between">
                <span>向量数据库</span>
                <span className="text-green-500">正常</span>
              </div>
              <div className="flex justify-between">
                <span>消息队列</span>
                <span className="text-green-500">正常</span>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard