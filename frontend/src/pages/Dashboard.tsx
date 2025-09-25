import { Card, Col, Row, Statistic } from 'antd'

export default function Dashboard() {
  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} md={8}>
        <Card><Statistic title="知识库数量" value={0} /></Card>
      </Col>
      <Col xs={24} md={8}>
        <Card><Statistic title="工作流数量" value={0} /></Card>
      </Col>
      <Col xs={24} md={8}>
        <Card><Statistic title="MCP 服务" value={0} /></Card>
      </Col>
    </Row>
  )
}


