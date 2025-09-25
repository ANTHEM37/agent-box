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
  InputNumber
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { 
  getKnowledgeBases, 
  createKnowledgeBase, 
  deleteKnowledgeBase 
} from '../../services/api/knowledge';
import { KnowledgeBaseResponse, KnowledgeBaseCreateRequest } from '../../types/knowledge';
import { formatDateTime } from '../../utils/format';

const { Title } = Typography;
const { confirm } = Modal;

const KnowledgeBaseList: React.FC = () => {
  const [knowledgeBases, setKnowledgeBases] = useState<KnowledgeBaseResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [createForm] = Form.useForm();
  const navigate = useNavigate();

  const fetchKnowledgeBases = async () => {
    setLoading(true);
    try {
      const response = await getKnowledgeBases();
      if (response.code === 200) {
        setKnowledgeBases(response.data.content);
      } else {
        message.error(response.message || '获取知识库列表失败');
      }
    } catch (error) {
      message.error('获取知识库列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchKnowledgeBases();
  }, []);

  const handleCreate = async (values: KnowledgeBaseCreateRequest) => {
    try {
      const response = await createKnowledgeBase(values);
      if (response.code === 200) {
        message.success('创建知识库成功');
        setCreateModalVisible(false);
        createForm.resetFields();
        fetchKnowledgeBases();
      } else {
        message.error(response.message || '创建知识库失败');
      }
    } catch (error) {
      message.error('创建知识库失败');
    }
  };

  const handleDelete = (id: number, name: string) => {
    confirm({
      title: '确认删除',
      content: `确定要删除知识库 "${name}" 吗？此操作不可恢复。`,
      onOk: async () => {
        try {
          const response = await deleteKnowledgeBase(id);
          if (response.code === 200) {
            message.success('删除知识库成功');
            fetchKnowledgeBases();
          } else {
            message.error(response.message || '删除知识库失败');
          }
        } catch (error) {
          message.error('删除知识库失败');
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
        <Tag color={status === 'ACTIVE' ? 'green' : status === 'INACTIVE' ? 'orange' : 'red'}>
          {status}
        </Tag>
      ),
    },
    {
      title: '文档数量',
      dataIndex: 'documentCount',
      key: 'documentCount',
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
      render: (_: any, record: KnowledgeBaseResponse) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EyeOutlined />} 
            size="small"
            onClick={() => navigate(`/knowledge/${record.id}`)}
          >
            查看
          </Button>
          <Button 
            icon={<EditOutlined />} 
            size="small"
            onClick={() => navigate(`/knowledge/${record.id}/edit`)}
          >
            编辑
          </Button>
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
        <Title level={2}>知识库管理</Title>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => setCreateModalVisible(true)}
        >
          创建知识库
        </Button>
      </div>

      <Card>
        <Table
          loading={loading}
          dataSource={knowledgeBases}
          columns={columns}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title="创建知识库"
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
            rules={[{ required: true, message: '请输入知识库名称' }]}
          >
            <Input placeholder="请输入知识库名称" />
          </Form.Item>
          
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea placeholder="请输入知识库描述" rows={3} />
          </Form.Item>
          
          <Form.Item
            name="embeddingModel"
            label="嵌入模型"
            initialValue="text-embedding-ada-002"
          >
            <Input placeholder="请输入嵌入模型" />
          </Form.Item>
          
          <Form.Item
            name="chunkSize"
            label="分块大小"
            initialValue={500}
          >
            <InputNumber min={100} max={2000} style={{ width: '100%' }} />
          </Form.Item>
          
          <Form.Item
            name="chunkOverlap"
            label="重叠大小"
            initialValue={50}
          >
            <InputNumber min={0} max={500} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default KnowledgeBaseList;