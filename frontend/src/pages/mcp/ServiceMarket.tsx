import React, { useEffect, useState } from 'react';
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Button,
  Tag,
  Rate,
  Avatar,
  Pagination,
  Spin,
  Empty,
  Space,
  Tabs,
  Statistic,
  Badge,
} from 'antd';
import {
  SearchOutlined,
  DownloadOutlined,
  StarOutlined,
  EyeOutlined,
  FilterOutlined,
  AppstoreOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { mcpApi } from '../../services/api/mcp';
import { useMcpStore } from '../../stores/mcp';
import type { McpService, ServiceSearchParams } from '../../types/mcp';

const { Search } = Input;
const { Option } = Select;
const { TabPane } = Tabs;

export default function ServiceMarket() {
  const navigate = useNavigate();
  const {
    services,
    featuredServices,
    searchParams,
    categories,
    tags,
    loading,
    pagination,
    setServices,
    setFeaturedServices,
    setSearchParams,
    setCategories,
    setTags,
    setLoading,
    setPagination,
  } = useMcpStore();

  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [activeTab, setActiveTab] = useState('all');

  // 获取精选服务
  const { data: featuredData } = useQuery({
    queryKey: ['featured-services'],
    queryFn: mcpApi.getFeaturedServices,
    onSuccess: (data) => {
      setFeaturedServices(data);
    },
  });

  // 获取服务列表
  const { data: servicesData, isLoading } = useQuery({
    queryKey: ['services', searchParams],
    queryFn: () => {
      switch (activeTab) {
        case 'popular':
          return mcpApi.getPopularServices(searchParams);
        case 'high-rated':
          return mcpApi.getHighRatedServices(4.0, searchParams);
        case 'free':
          return mcpApi.searchServices({ ...searchParams, priceModel: 'FREE' });
        default:
          return searchParams.keyword 
            ? mcpApi.searchServices(searchParams)
            : mcpApi.getPublishedServices(searchParams);
      }
    },
    onSuccess: (data) => {
      setServices(data.content);
      setPagination('services', {
        page: data.number,
        size: data.size,
        total: data.totalElements,
        totalPages: data.totalPages,
      });
    },
  });

  // 获取分类和标签
  useQuery({
    queryKey: ['categories'],
    queryFn: mcpApi.getAllCategories,
    onSuccess: setCategories,
  });

  useQuery({
    queryKey: ['tags'],
    queryFn: mcpApi.getAllTags,
    onSuccess: setTags,
  });

  // 搜索处理
  const handleSearch = (keyword: string) => {
    setSearchParams({ keyword, page: 0 });
  };

  // 过滤处理
  const handleFilter = (key: keyof ServiceSearchParams, value: any) => {
    setSearchParams({ [key]: value, page: 0 });
  };

  // 分页处理
  const handlePageChange = (page: number, size?: number) => {
    setSearchParams({ page: page - 1, size });
  };

  // 标签页切换
  const handleTabChange = (key: string) => {
    setActiveTab(key);
    setSearchParams({ page: 0 });
  };

  // 服务卡片组件
  const ServiceCard = ({ service }: { service: McpService }) => (
    <Card
      hoverable
      className="h-full"
      cover={
        <div className="h-48 bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
          {service.iconUrl ? (
            <img src={service.iconUrl} alt={service.displayName} className="w-16 h-16 object-cover rounded" />
          ) : (
            <div className="w-16 h-16 bg-blue-500 rounded flex items-center justify-center text-white text-2xl font-bold">
              {service.displayName.charAt(0)}
            </div>
          )}
        </div>
      }
      actions={[
        <Button
          key="view"
          type="text"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/mcp/services/${service.id}`)}
        >
          查看详情
        </Button>,
        <Button
          key="download"
          type="text"
          icon={<DownloadOutlined />}
          onClick={() => mcpApi.incrementDownloads(service.id)}
        >
          {service.downloadsCount}
        </Button>,
      ]}
    >
      <Card.Meta
        title={
          <div className="flex items-center justify-between">
            <span className="truncate">{service.displayName}</span>
            {service.featured && <Badge status="success" text="精选" />}
          </div>
        }
        description={
          <div className="space-y-2">
            <p className="text-gray-600 text-sm line-clamp-2">{service.description}</p>
            
            <div className="flex items-center space-x-2">
              <Rate disabled defaultValue={service.ratingAverage} size="small" />
              <span className="text-sm text-gray-500">({service.ratingCount})</span>
            </div>
            
            <div className="flex items-center justify-between">
              <Tag color={service.priceModel === 'FREE' ? 'green' : 'blue'}>
                {service.priceModel === 'FREE' ? '免费' : '付费'}
              </Tag>
              {service.category && (
                <Tag color="default">{service.category}</Tag>
              )}
            </div>
            
            <div className="flex flex-wrap gap-1">
              {service.tags.slice(0, 3).map(tag => (
                <Tag key={tag} size="small">{tag}</Tag>
              ))}
              {service.tags.length > 3 && (
                <Tag size="small">+{service.tags.length - 3}</Tag>
              )}
            </div>
          </div>
        }
      />
    </Card>
  );

  // 服务列表项组件
  const ServiceListItem = ({ service }: { service: McpService }) => (
    <Card className="mb-4">
      <div className="flex items-center space-x-4">
        <div className="flex-shrink-0">
          {service.iconUrl ? (
            <img src={service.iconUrl} alt={service.displayName} className="w-16 h-16 object-cover rounded" />
          ) : (
            <div className="w-16 h-16 bg-blue-500 rounded flex items-center justify-center text-white text-xl font-bold">
              {service.displayName.charAt(0)}
            </div>
          )}
        </div>
        
        <div className="flex-1 min-w-0">
          <div className="flex items-center space-x-2 mb-2">
            <h3 className="text-lg font-semibold truncate">{service.displayName}</h3>
            {service.featured && <Badge status="success" text="精选" />}
            <Tag color={service.priceModel === 'FREE' ? 'green' : 'blue'}>
              {service.priceModel === 'FREE' ? '免费' : '付费'}
            </Tag>
          </div>
          
          <p className="text-gray-600 mb-2 line-clamp-2">{service.description}</p>
          
          <div className="flex items-center space-x-4 text-sm text-gray-500">
            <div className="flex items-center space-x-1">
              <Rate disabled defaultValue={service.ratingAverage} size="small" />
              <span>({service.ratingCount})</span>
            </div>
            <span>{service.downloadsCount} 下载</span>
            {service.category && <span>{service.category}</span>}
          </div>
          
          <div className="flex flex-wrap gap-1 mt-2">
            {service.tags.slice(0, 5).map(tag => (
              <Tag key={tag} size="small">{tag}</Tag>
            ))}
          </div>
        </div>
        
        <div className="flex-shrink-0">
          <Space direction="vertical">
            <Button
              type="primary"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/mcp/services/${service.id}`)}
            >
              查看详情
            </Button>
            <Button
              icon={<DownloadOutlined />}
              onClick={() => mcpApi.incrementDownloads(service.id)}
            >
              下载
            </Button>
          </Space>
        </div>
      </div>
    </Card>
  );

  return (
    <div className="p-6">
      {/* 页面标题 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold mb-2">MCP 服务市场</h1>
        <p className="text-gray-600">发现和部署强大的 MCP 服务</p>
      </div>

      {/* 精选服务 */}
      {featuredServices.length > 0 && (
        <div className="mb-8">
          <h2 className="text-xl font-semibold mb-4 flex items-center">
            <StarOutlined className="mr-2 text-yellow-500" />
            精选服务
          </h2>
          <Row gutter={[16, 16]}>
            {featuredServices.slice(0, 4).map(service => (
              <Col key={service.id} xs={24} sm={12} lg={6}>
                <ServiceCard service={service} />
              </Col>
            ))}
          </Row>
        </div>
      )}

      {/* 搜索和过滤 */}
      <Card className="mb-6">
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} md={8}>
            <Search
              placeholder="搜索服务..."
              allowClear
              enterButton={<SearchOutlined />}
              onSearch={handleSearch}
              defaultValue={searchParams.keyword}
            />
          </Col>
          
          <Col xs={12} md={4}>
            <Select
              placeholder="选择分类"
              allowClear
              style={{ width: '100%' }}
              value={searchParams.category}
              onChange={(value) => handleFilter('category', value)}
            >
              {categories.map(category => (
                <Option key={category} value={category}>{category}</Option>
              ))}
            </Select>
          </Col>
          
          <Col xs={12} md={4}>
            <Select
              placeholder="价格模式"
              allowClear
              style={{ width: '100%' }}
              value={searchParams.priceModel}
              onChange={(value) => handleFilter('priceModel', value)}
            >
              <Option value="FREE">免费</Option>
              <Option value="PAID">付费</Option>
              <Option value="FREEMIUM">免费增值</Option>
            </Select>
          </Col>
          
          <Col xs={12} md={4}>
            <Select
              placeholder="排序方式"
              style={{ width: '100%' }}
              value={`${searchParams.sortBy}-${searchParams.sortOrder}`}
              onChange={(value) => {
                const [sortBy, sortOrder] = value.split('-');
                setSearchParams({ sortBy, sortOrder, page: 0 });
              }}
            >
              <Option value="created-desc">最新发布</Option>
              <Option value="downloads-desc">下载量</Option>
              <Option value="rating-desc">评分</Option>
              <Option value="name-asc">名称</Option>
            </Select>
          </Col>
          
          <Col xs={12} md={4}>
            <div className="flex space-x-2">
              <Button
                type={viewMode === 'grid' ? 'primary' : 'default'}
                icon={<AppstoreOutlined />}
                onClick={() => setViewMode('grid')}
              />
              <Button
                type={viewMode === 'list' ? 'primary' : 'default'}
                icon={<UnorderedListOutlined />}
                onClick={() => setViewMode('list')}
              />
            </div>
          </Col>
        </Row>
      </Card>

      {/* 服务列表 */}
      <Card>
        <Tabs activeKey={activeTab} onChange={handleTabChange}>
          <TabPane tab="全部服务" key="all" />
          <TabPane tab="热门服务" key="popular" />
          <TabPane tab="高评分" key="high-rated" />
          <TabPane tab="免费服务" key="free" />
        </Tabs>

        <Spin spinning={isLoading}>
          {services.length === 0 ? (
            <Empty description="暂无服务" />
          ) : (
            <>
              {viewMode === 'grid' ? (
                <Row gutter={[16, 16]}>
                  {services.map(service => (
                    <Col key={service.id} xs={24} sm={12} lg={8} xl={6}>
                      <ServiceCard service={service} />
                    </Col>
                  ))}
                </Row>
              ) : (
                <div>
                  {services.map(service => (
                    <ServiceListItem key={service.id} service={service} />
                  ))}
                </div>
              )}

              {/* 分页 */}
              <div className="mt-6 text-center">
                <Pagination
                  current={pagination.services.page + 1}
                  pageSize={pagination.services.size}
                  total={pagination.services.total}
                  showSizeChanger
                  showQuickJumper
                  showTotal={(total, range) => 
                    `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
                  }
                  onChange={handlePageChange}
                />
              </div>
            </>
          )}
        </Spin>
      </Card>
    </div>
  );
}