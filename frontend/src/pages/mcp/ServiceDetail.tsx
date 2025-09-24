import React, { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  Tag,
  Rate,
  Tabs,
  Descriptions,
  Avatar,
  List,
  Comment,
  Form,
  Input,
  Modal,
  message,
  Spin,
  Badge,
  Statistic,
  Space,
  Divider,
} from 'antd';
import {
  DownloadOutlined,
  StarOutlined,
  LikeOutlined,
  ShareAltOutlined,
  RocketOutlined,
  CodeOutlined,
  FileTextOutlined,
  GithubOutlined,
  GlobalOutlined,
} from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { mcpApi } from '../../services/api/mcp';
import { useMcpStore } from '../../stores/mcp';
import type { ServiceReviewRequest } from '../../types/mcp';

const { TabPane } = Tabs;
const { TextArea } = Input;

export default function ServiceDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { currentService, setCurrentService } = useMcpStore();
  
  const [reviewModalVisible, setReviewModalVisible] = useState(false);
  const [deployModalVisible, setDeployModalVisible] = useState(false);
  const [reviewForm] = Form.useForm();

  // 获取服务详情
  const { data: service, isLoading } = useQuery({
    queryKey: ['service', id],
    queryFn: () => mcpApi.getService(Number(id)),
    enabled: !!id,
    onSuccess: (data) => {
      setCurrentService(data);
    },
  });

  // 获取服务评论
  const { data: reviewsData } = useQuery({
    queryKey: ['service-reviews', id],
    queryFn: () => mcpApi.getServiceReviews(Number(id)),
    enabled: !!id,
  });

  // 创建评论
  const createReviewMutation = useMutation({
    mutationFn: (data: ServiceReviewRequest) => mcpApi.createReview(data),
    onSuccess: () => {
      message.success('评论提交成功');
      setReviewModalVisible(false);
      reviewForm.resetFields();
      queryClient.invalidateQueries(['service-reviews', id]);
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '评论提交失败');
    },
  });

  // 增加下载次数
  const downloadMutation = useMutation({
    mutationFn: () => mcpApi.incrementDownloads(Number(id)),
    onSuccess: () => {
      message.success('下载成功');
      queryClient.invalidateQueries(['service', id]);
    },
  });

  // 提交评论
  const handleReviewSubmit = async () => {
    try {
      const values = await reviewForm.validateFields();
      createReviewMutation.mutate({
        serviceId: Number(id),
        ...values,
      });
    } catch (error) {
      // 表单验证失败
    }
  };

  // 部署服务
  const handleDeploy = () => {
    navigate(`/mcp/deploy?serviceId=${id}`);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spin size="large" />
      </div>
    );
  }

  if (!service) {
    return <div>服务不存在</div>;
  }

  return (
    <div className="p-6">
      {/* 服务头部信息 */}
      <Card className="mb-6">
        <Row gutter={24}>
          <Col span={4}>
            <div className="text-center">
              {service.iconUrl ? (
                <img 
                  src={service.iconUrl} 
                  alt={service.displayName} 
                  className="w-24 h-24 object-cover rounded-lg mx-auto mb-4"
                />
              ) : (
                <div className="w-24 h-24 bg-blue-500 rounded-lg flex items-center justify-center text-white text-3xl font-bold mx-auto mb-4">
                  {service.displayName.charAt(0)}
                </div>
              )}
            </div>
          </Col>
          
          <Col span={14}>
            <div className="space-y-3">
              <div className="flex items-center space-x-3">
                <h1 className="text-3xl font-bold">{service.displayName}</h1>
                {service.featured && <Badge status="success" text="精选" />}
                <Tag color={service.priceModel === 'FREE' ? 'green' : 'blue'}>
                  {service.priceModel === 'FREE' ? '免费' : '付费'}
                </Tag>
              </div>
              
              <p className="text-gray-600 text-lg">{service.description}</p>
              
              <div className="flex items-center space-x-6">
                <div className="flex items-center space-x-2">
                  <Rate disabled defaultValue={service.ratingAverage} />
                  <span className="font-medium">{service.ratingAverage}</span>
                  <span className="text-gray-500">({service.ratingCount} 评价)</span>
                </div>
                
                <div className="flex items-center space-x-1">
                  <DownloadOutlined />
                  <span>{service.downloadsCount} 下载</span>
                </div>
                
                {service.category && (
                  <Tag color="default">{service.category}</Tag>
                )}
              </div>
              
              <div className="flex flex-wrap gap-2">
                {service.tags.map(tag => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
              </div>
            </div>
          </Col>
          
          <Col span={6}>
            <div className="text-right space-y-3">
              <div>
                <Button
                  type="primary"
                  size="large"
                  icon={<RocketOutlined />}
                  onClick={handleDeploy}
                  block
                >
                  立即部署
                </Button>
              </div>
              
              <div>
                <Button
                  size="large"
                  icon={<DownloadOutlined />}
                  onClick={() => downloadMutation.mutate()}
                  loading={downloadMutation.isLoading}
                  block
                >
                  下载
                </Button>
              </div>
              
              <div className="flex space-x-2">
                <Button icon={<StarOutlined />}>收藏</Button>
                <Button icon={<ShareAltOutlined />}>分享</Button>
                <Button 
                  icon={<LikeOutlined />}
                  onClick={() => setReviewModalVisible(true)}
                >
                  评价
                </Button>
              </div>
              
              {service.repositoryUrl && (
                <div>
                  <Button 
                    icon={<GithubOutlined />}
                    href={service.repositoryUrl}
                    target="_blank"
                    block
                  >
                    查看源码
                  </Button>
                </div>
              )}
            </div>
          </Col>
        </Row>
      </Card>

      {/* 详细信息 */}
      <Row gutter={24}>
        <Col span={16}>
          <Card>
            <Tabs defaultActiveKey="overview">
              <TabPane tab="概览" key="overview">
                <div className="space-y-6">
                  <div>
                    <h3 className="text-lg font-semibold mb-3">服务描述</h3>
                    <p className="text-gray-700 leading-relaxed">
                      {service.description || '暂无详细描述'}
                    </p>
                  </div>
                  
                  <Divider />
                  
                  <div>
                    <h3 className="text-lg font-semibold mb-3">功能特性</h3>
                    <ul className="list-disc list-inside space-y-2 text-gray-700">
                      <li>高性能的 MCP 协议实现</li>
                      <li>支持多种数据格式</li>
                      <li>完整的 API 文档</li>
                      <li>容器化部署</li>
                    </ul>
                  </div>
                  
                  <Divider />
                  
                  <div>
                    <h3 className="text-lg font-semibold mb-3">使用场景</h3>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-4 bg-gray-50 rounded-lg">
                        <h4 className="font-medium mb-2">数据处理</h4>
                        <p className="text-sm text-gray-600">适用于大规模数据处理和分析场景</p>
                      </div>
                      <div className="p-4 bg-gray-50 rounded-lg">
                        <h4 className="font-medium mb-2">API 集成</h4>
                        <p className="text-sm text-gray-600">提供标准化的 API 接口</p>
                      </div>
                    </div>
                  </div>
                </div>
              </TabPane>
              
              <TabPane tab="版本历史" key="versions">
                <List
                  dataSource={[
                    {
                      version: service.latestVersion || '1.0.0',
                      changelog: service.latestVersionChangelog || '初始版本',
                      date: service.latestVersionCreatedAt || service.createdAt,
                    }
                  ]}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        title={`版本 ${item.version}`}
                        description={
                          <div>
                            <p>{item.changelog}</p>
                            <p className="text-gray-500 text-sm mt-2">
                              发布时间: {new Date(item.date).toLocaleDateString()}
                            </p>
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              </TabPane>
              
              <TabPane tab="API 文档" key="api">
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <CodeOutlined />
                    <span className="font-medium">API 端点</span>
                  </div>
                  
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <code>GET /api/v1/service</code>
                  </div>
                  
                  <div>
                    <h4 className="font-medium mb-2">请求参数</h4>
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <pre>{JSON.stringify({
                        "param1": "string",
                        "param2": "number"
                      }, null, 2)}</pre>
                    </div>
                  </div>
                  
                  <div>
                    <h4 className="font-medium mb-2">响应示例</h4>
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <pre>{JSON.stringify({
                        "status": "success",
                        "data": {
                          "result": "example"
                        }
                      }, null, 2)}</pre>
                    </div>
                  </div>
                </div>
              </TabPane>
              
              <TabPane tab="评论" key="reviews">
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <h3 className="text-lg font-semibold">用户评价</h3>
                    <Button 
                      type="primary" 
                      onClick={() => setReviewModalVisible(true)}
                    >
                      写评价
                    </Button>
                  </div>
                  
                  <List
                    dataSource={reviewsData?.content || []}
                    renderItem={(review) => (
                      <Comment
                        author={review.userName || `用户${review.userId}`}
                        avatar={<Avatar>{review.userName?.charAt(0) || 'U'}</Avatar>}
                        content={
                          <div>
                            <Rate disabled defaultValue={review.rating} size="small" />
                            <p className="mt-2">{review.comment}</p>
                          </div>
                        }
                        datetime={new Date(review.createdAt).toLocaleDateString()}
                      />
                    )}
                  />
                </div>
              </TabPane>
            </Tabs>
          </Card>
        </Col>
        
        <Col span={8}>
          <div className="space-y-6">
            {/* 服务信息 */}
            <Card title="服务信息">
              <Descriptions column={1} size="small">
                <Descriptions.Item label="作者">
                  用户{service.authorId}
                </Descriptions.Item>
                <Descriptions.Item label="最新版本">
                  {service.latestVersion || '1.0.0'}
                </Descriptions.Item>
                <Descriptions.Item label="发布时间">
                  {new Date(service.createdAt).toLocaleDateString()}
                </Descriptions.Item>
                <Descriptions.Item label="更新时间">
                  {new Date(service.updatedAt).toLocaleDateString()}
                </Descriptions.Item>
                {service.pricePerRequest && (
                  <Descriptions.Item label="价格">
                    ¥{service.pricePerRequest}/请求
                  </Descriptions.Item>
                )}
              </Descriptions>
              
              {service.documentationUrl && (
                <div className="mt-4">
                  <Button 
                    icon={<FileTextOutlined />}
                    href={service.documentationUrl}
                    target="_blank"
                    block
                  >
                    查看文档
                  </Button>
                </div>
              )}
            </Card>
            
            {/* 统计信息 */}
            <Card title="统计信息">
              <Row gutter={16}>
                <Col span={12}>
                  <Statistic title="下载量" value={service.downloadsCount} />
                </Col>
                <Col span={12}>
                  <Statistic title="评分" value={service.ratingAverage} precision={1} />
                </Col>
              </Row>
              
              <Row gutter={16} className="mt-4">
                <Col span={12}>
                  <Statistic title="版本数" value={service.versionsCount || 1} />
                </Col>
                <Col span={12}>
                  <Statistic title="评价数" value={service.ratingCount} />
                </Col>
              </Row>
            </Card>
            
            {/* 相关服务 */}
            <Card title="相关服务">
              <div className="space-y-3">
                {[1, 2, 3].map(i => (
                  <div key={i} className="flex items-center space-x-3 p-2 hover:bg-gray-50 rounded cursor-pointer">
                    <div className="w-10 h-10 bg-blue-500 rounded flex items-center justify-center text-white font-bold">
                      S{i}
                    </div>
                    <div className="flex-1">
                      <div className="font-medium">相关服务 {i}</div>
                      <div className="text-sm text-gray-500">简短描述</div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>
        </Col>
      </Row>

      {/* 评价模态框 */}
      <Modal
        title="写评价"
        open={reviewModalVisible}
        onOk={handleReviewSubmit}
        onCancel={() => setReviewModalVisible(false)}
        confirmLoading={createReviewMutation.isLoading}
      >
        <Form form={reviewForm} layout="vertical">
          <Form.Item
            name="rating"
            label="评分"
            rules={[{ required: true, message: '请选择评分' }]}
          >
            <Rate />
          </Form.Item>
          
          <Form.Item
            name="comment"
            label="评价内容"
            rules={[{ required: true, message: '请输入评价内容' }]}
          >
            <TextArea rows={4} placeholder="请分享您的使用体验..." />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}