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
  InputNumber,
  Switch
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { 
  getAllAgentDefinitions, 
  createAgentDefinition, 
  deleteAgentDefinition 
} from '../../services/api/agent';
import { AgentDefinition, AgentType } from '../../types/agent';
import { formatDateTime } from '../../utils/format';

const { Title } = Typography;
const { confirm } = Modal;
const { Option } = Select;

const AgentList: React.FC = () => {
  const [agents, setAgents] = useState<AgentDefinition[]>([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [createForm] = Form.useForm();
  const navigate = useNavigate();

  const fetchAgents = async () => {
    setLoading(true);
    try {
      const response = await getAllAgentDefinitions();
      setAgents(response);
    } catch (error) {
      message.error('获取智能体列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAgents();
  }, []);

  const handleCreate = async (values: AgentDefinition) => {
    try {
      const response = await createAgentDefinition(values);
      message.success('创建智能体成功');
      setCreateModalVisible(false);
      createForm.resetFields();
      fetchAgents();
    } catch (error) {
      message.error('创建智能体失败');
    }
  };

  const handleDelete = (id: number, name: string) => {
    confirm({
      title: '确认删除',
      content: `确定要删除智能体 "${name}" 吗？此操作不可恢复。`,
      onOk: async () => {
        try {
          await deleteAgentDefinition(id);
          message.success('删除智能体成功');
          fetchAgents();
        } catch (error) {
          message.error('删除智能体失败');
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
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => (
        <Tag color={
          type === 'AGENT' ? 'blue' : 
          type === 'WORKFLOW' ? 'green' : 
          type === 'COORDINATOR' ? 'orange' : 
          'purple'
        }>
          {type === 'AGENT' ? '普通智能体' : 
           type === 'WORKFLOW' ? '工作流智能体' : 
           type === 'COORDINATOR' ? '协调器智能体' : 
           '专业智能体'}
        </Tag>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
    },
    {
      title: '启用状态',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'green' : 'red'}>
          {enabled ? '启用' : '禁用'}
        </Tag>
      ),
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
      render: (_: any, record: AgentDefinition) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EyeOutlined />} 
            size="small"
            onClick={() => navigate(`/agents/${record.id}`)}
          >
            查看
          </Button>
          <Button 
            icon={<EditOutlined />} 
            size="small"
            onClick={() => navigate(`/agents/${record.id}/edit`)}
          >
            编辑
          </Button>
          <Button 
            danger 
            icon={<DeleteOutlined />} 
            size="small"
            onClick={() => handleDelete(record.id!, record.name)}
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
        <Title level={2}>智能体管理</Title>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => setCreateModalVisible(true)}
        >
          创建智能体
        </Button>
      </div>

      <Card>
        <Table
          loading={loading}
          dataSource={agents}
          columns={columns}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title="创建智能体"
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
            label="名称"
            rules={[{ required: true, message: '请输入智能体名称' }]}
          >
            <Input placeholder="请输入智能体名称" />
          </Form.Item>
          
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea placeholder="请输入智能体描述" rows={3} />
          </Form.Item>
          
          <Form.Item
            name="type"
            label="类型"
            rules={[{ required: true, message: '请选择智能体类型' }]}
          >
            <Select placeholder="请选择智能体类型">
              <Option value="AGENT">普通智能体</Option>
              <Option value="WORKFLOW">工作流智能体</Option>
              <Option value="COORDINATOR">协调器智能体</Option>
              <Option value="SPECIALIST">专业智能体</Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name="roleDefinition"
            label="角色定义"
          >
            <Input.TextArea placeholder="请输入角色定义" rows={3} />
          </Form.Item>
          
          <Form.Item
            name="systemPrompt"
            label="系统提示词"
          >
            <Input.TextArea placeholder="请输入系统提示词" rows={4} />
          </Form.Item>
          
          <Form.Item
            name="temperature"
            label="温度参数"
            initialValue={0.7}
          >
            <InputNumber min={0} max={1} step={0.1} style={{ width: '100%' }} />
          </Form.Item>
          
          <Form.Item
            name="maxTokens"
            label="最大Token数"
            initialValue={2000}
          >
            <InputNumber min={1} max={8000} style={{ width: '100%' }} />
          </Form.Item>
          
          <Form.Item
            name="enabled"
            label="启用状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AgentList;