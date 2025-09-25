import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Table, 
  Button, 
  Card, 
  Typography, 
  Space, 
  Tag, 
  message,
  Modal,
  Form,
  Input,
  Select,
  InputNumber
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { 
  getPublishedServices, 
  createService, 
  deleteService 
} from '../../services/api/mcp';
import { McpServiceResponse, McpServiceCreateRequest, PriceModel } from '../../types/mcp';
import { formatDateTime } from '../../utils/format';

const { Title } = Typography;
const { confirm } = Modal;
const { Option } = Select;

const McpServiceList: React.FC = () => {
  const [services, setServices] = useState<McpServiceResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [createForm] = Form.useForm();
  const navigate = useNavigate();

  const fetchServices = async () => {
    setLoading(true);
    try {
      const response = await getPublishedServices();
      if (response.code === 200) {
        setServices(response.data.content);
      } else {
        message.error(response.message || '获取服务列表失败');
      }
    } catch (error) {
      message.error('获取服务列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServices();
  }, []);

  const handleCreate = async (values: McpServiceCreateRequest) => {
    try {
      const response = await createService(values);
      if (response.code === 200) {
        message.success('创建服务成功');
        setCreateModalVisible(false);
        createForm.resetFields();
        fetchServices();
      } else {
        message.error(response.message || '创建服务失败');
      }
    } catch (error) {
      message.error('创建服务失败');
    }
  };

  const handleDelete = (id: number, name: string) => {
    confirm({
      title: '确认删除',
      content: `确定要删除服务 "${name}" 吗？此操作不可恢复。`,
      onOk: async () => {
        try {
          const response = await deleteService(id);
          if (response.code === 200) {
            message.success('删除服务成功');
            fetchServices();
          } else {
            message.error(response.message || '删除服务失败');
          }
        } catch (error) {
          message.error('删除服务失败');
        }
      },
    });
  };

  const columns = [
    {
      title: '名称',
      dataIndex: 'displayName',
      key: 'displayName',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      render: (text: string) => text || '-',
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (text: string) => text || '-',
    },
    {
      title: '价格模式',
      dataIndex: 'priceModel',
      key: 'priceModel',
      render: (model: string) => (
        <Tag color={model === 'FREE' ? 'green' : model === 'PER_REQUEST' ? 'blue' : 'orange'}>
          {model === 'FREE' ? '免费' : model === 'PER_REQUEST' ? '按次计费' : '订阅'}
        </Tag>
      ),
    },
    {
      title: '价格',
      dataIndex: 'pricePerRequest',
      key: 'pricePerRequest',
      render: (price: number, record: McpServiceResponse) => 
        record.priceModel === PriceModel.FREE ? '免费' : `¥${price?.toFixed(2) || '0.00'}`,
    },
    {
      title: '评分',
      dataIndex: 'ratingAverage',
      key: 'ratingAverage',
      render: (rating: number) => rating ? rating.toFixed(1) : '-',
    },
    {
      title: '下载次数',
      dataIndex: 'downloadsCount',
      key: 'downloadsCount',
      render: (count: number) => count || 0,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => formatDateTime(date),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: McpServiceResponse) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EyeOutlined />} 
            size="small"
            onClick={() => navigate(`/mcp/${record.id}`)}
          >
            查看
          </Button>
          <Button 
            icon={<EditOutlined />} 
            size="small"
            onClick={() => navigate(`/mcp/${record.id}/edit`)}
          >
            编辑
          </Button>
          <Button 
            danger 
            icon={<DeleteOutlined />} 
            size="small"
            onClick={() => handleDelete(record.id, record.displayName)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <Title level={2}>MCP服务市场</Title>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => setCreateModalVisible(true)}
        >
          创建服务
        </Button>
      </div>

      <Card>
        <Table
          loading={loading}
          dataSource={services}
          columns={columns}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title="创建MCP服务"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          createForm.resetFields();
        }}
        onOk={() => createForm.submit()}
        width={600}
      >
        <Form
          form={createForm}
          layout="vertical"
          onFinish={handleCreate}
        >
          <Form.Item
            name="name"
            label="服务名称"
            rules={[{ required: true, message: '请输入服务名称' }]}
          >
            <Input placeholder="请输入服务名称" />
          </Form.Item>
          
          <Form.Item
            name="displayName"
            label="显示名称"
            rules={[{ required: true, message: '请输入显示名称' }]}
          >
            <Input placeholder="请输入显示名称" />
          </Form.Item>
          
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea placeholder="请输入服务描述" rows={3} />
          </Form.Item>
          
          <Form.Item
            name="category"
            label="分类"
          >
            <Select placeholder="请选择分类">
              <Option value="data_processing">数据处理</Option>
              <Option value="ai_analysis">AI分析</Option>
              <Option value="automation">自动化</Option>
              <Option value="integration">集成</Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name="priceModel"
            label="价格模式"
            rules={[{ required: true, message: '请选择价格模式' }]}
          >
            <Select placeholder="请选择价格模式">
              <Option value="FREE">免费</Option>
              <Option value="PER_REQUEST">按次计费</Option>
              <Option value="SUBSCRIPTION">订阅</Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name="pricePerRequest"
            label="单次价格"
          >
            <InputNumber 
              min={0} 
              step={0.01} 
              precision={2} 
              style={{ width: '100%' }} 
              formatter={value => `¥ ${value}`}
              parser={value => value!.replace(/¥\s?|(,*)/g, '') as any}
            />
          </Form.Item>
          
          <Form.Item
            name="dockerImage"
            label="Docker镜像"
            rules={[{ required: true, message: '请输入Docker镜像地址' }]}
          >
            <Input placeholder="请输入Docker镜像地址" />
          </Form.Item>
          
          <Form.Item
            name="version"
            label="版本号"
            rules={[{ required: true, message: '请输入版本号' }]}
          >
            <Input placeholder="请输入版本号" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default McpServiceList;