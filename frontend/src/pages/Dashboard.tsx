import React from 'react';
import { Card, Row, Col, Statistic, Typography } from 'antd';
import { 
  RobotOutlined, 
  DatabaseOutlined, 
  ApiOutlined, 
  PlayCircleOutlined 
} from '@ant-design/icons';

const { Title } = Typography;

const Dashboard: React.FC = () => {
  return (
    <div className="page-container">
      <div className="page-header">
        <Title level={2}>仪表板</Title>
      </div>
      
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic
              title="智能体数量"
              value={12}
              prefix={<RobotOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="知识库数量"
              value={8}
              prefix={<DatabaseOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="工作流数量"
              value={5}
              prefix={<PlayCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="MCP服务数量"
              value={5}
              prefix={<ApiOutlined />}
            />
          </Card>
        </Col>
      </Row>
      
      <Row gutter={16} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="最近活动">
            <p>暂无最近活动</p>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;