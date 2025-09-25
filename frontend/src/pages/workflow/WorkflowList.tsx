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
  Select
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { 
  getUserWorkflows, 
  createWorkflow, 
  deleteWorkflow,
  publishWorkflow
} from '../../services/api/workflow';
import { WorkflowResponse, WorkflowCreateRequest } from '../../types/workflow';
import { formatDateTime } from '../../utils/format';

const { Title } = Typography;
const { confirm } = Modal;
const { Option } = Select;

const WorkflowList: React.FC = () => {
  const [workflows, setWorkflows] = useState<WorkflowResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [createForm] = Form.useForm();
  const navigate = useNavigate();

  const fetchWorkflows = async () => {
    setLoading(true);
    try {
      const response = await getUserWorkflows();
      if (response.code === 200) {
        setWorkflows(response.data.content);
      } else {
        message.error(response.message || '获取工作流列表失败');
      }
    } catch (error) {
      message.error('获取工作流列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkflows();
  }, []);

  const handleCreate = async (values: WorkflowCreateRequest) => {
    try {
      const response = await createWorkflow(values);
      if (response.code === 200) {
        message.success('创建工作流成功');
        setCreateModalVisible(false);
        createForm.resetFields();
        fetchWorkflows();
      } else {
        message.error(response.message || '创建工作流失败');
      }
    } catch (error) {
      message.error('创建工作流失败');
    }
  };

  const handleDelete = (id: number, name: string) => {
    confirm({
      title: '确认删除',
      content: `确定要删除工作流 "${name}" 吗？此操作不可恢复。`,
      onOk: async () => {
        try {
          const response = await deleteWorkflow(id);
          if (response.code === 200) {
            message.success('删除工作流成功');
            fetchWorkflows();
          } else {
            message.error(response.message || '删除工作流失败');
          }
        } catch (error) {
          message.error('删除工作流失败');
        }
      },
    });
  };

  const handlePublish = (id: number, name: string) => {
    confirm({
      title: '确认发布',
      content: `确定要发布工作流 "${name}" 吗？发布后将可供其他用户使用。`,
      onOk: async () => {
        try {
          const response = await publishWorkflow(id);
          if (response.code === 200) {
            message.success('发布工作流成功');
            fetchWorkflows();
          } else {
            message.error(response.message || '发布工作流失败');
          }
        } catch (error) {
          message.error('发布工作流失败');
        }
      },
    });
  };

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      render: (text: string) => text || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={
          status === 'DRAFT' ? 'default' : 
          status === 'PUBLISHED' ? 'green' : 
          'red'
        }>
          {status === 'DRAFT' ? '草稿' : 
           status === 'PUBLISHED' ? '已发布' : 
           '已归档'}
        </Tag>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (text: string) => text || '-',
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
      render: (_: any, record: WorkflowResponse) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EyeOutlined />} 
            size="small"
            onClick={() => navigate(`/workflows/editor/${record.id}`)}
          >
            编辑
          </Button>
          {record.status === 'DRAFT' && (
            <Button 
              icon={<PlayCircleOutlined />} 
              size="small"
              onClick={() => handlePublish(record.id, record.name)}
            >
              发布
            </Button>
          )}
          <Button 
            danger 
            icon={<DeleteOutlined />} 
            size="small"
            onClick={() => handleDelete(record.id, record.name)}
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
        <Title level={2}>工作流管理</Title>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => setCreateModalVisible(true)}
        >
          创建工作流
        </Button>
      </div>

      <Card>
        <Table
          loading={loading}
          dataSource={workflows}
          columns={columns}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title="创建工作流"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          createForm.resetFields();
        }}
        onOk={() => createForm.submit()}
      >
        <Form
          form={createForm}
          layout="vertical"
          onFinish={handleCreate}
        >
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: '请输入工作流名称' }]}
          >
            <Input placeholder="请输入工作流名称" />
          </Form.Item>
          
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea placeholder="请输入工作流描述" rows={3} />
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
            name="isTemplate"
            label="是否为模板"
            valuePropName="checked"
          >
            <input type="checkbox" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WorkflowList;