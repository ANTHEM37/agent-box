import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Dropdown,
  Modal,
  Form,
  Input,
  Select,
  message,
  Tooltip,
  Badge,
  Row,
  Col,
  Statistic,
} from 'antd';
import {
  PlusOutlined,
  PlayCircleOutlined,
  EditOutlined,
  CopyOutlined,
  DeleteOutlined,
  MoreOutlined,
  EyeOutlined,
  PublishedWithChangesOutlined,
  ArchiveBoxOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workflowApi } from '../../services/api/workflow';
import { useWorkflowStore } from '../../stores/workflow';
import type { Workflow } from '../../types/workflow';

const { Option } = Select;
const { Search } = Input;

export default function WorkflowList() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { 
    workflows, 
    stats, 
    categories,
    pagination,
    setWorkflows, 
    setStats,
    setCategories,
    setPagination,
    removeWorkflow 
  } = useWorkflowStore();

  const [filters, setFilters] = useState({
    keyword: '',
    status: '',
    category: '',
  });
  const [copyModalVisible, setCopyModalVisible] = useState(false);
  const [copyingWorkflow, setCopyingWorkflow] = useState<Workflow | null>(null);
  const [copyForm] = Form.useForm();

  // 获取工作流列表
  const { data: workflowsData, isLoading } = useQuery({
    queryKey: ['workflows', pagination.page, pagination.size, filters],
    queryFn: () => workflowApi.getUserWorkflows({
      page: pagination.page,
      size: pagination.size,
      ...filters,
    }),
  });

  // 获取统计信息
  const { data: statsData } = useQuery({
    queryKey: ['workflow-stats'],
    queryFn: workflowApi.getUserStats,
  });

  // 获取分类列表
  const { data: categoriesData } = useQuery({
    queryKey: ['workflow-categories'],
    queryFn: workflowApi.getUserCategories,
  });

  // 删除工作流
  const deleteMutation = useMutation({
    mutationFn: workflowApi.deleteWorkflow,
    onSuccess: (_, id) => {
      message.success('工作流删除成功');
      removeWorkflow(id);
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      queryClient.invalidateQueries({ queryKey: ['workflow-stats'] });
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '删除失败');
    },
  });

  // 复制工作流
  const copyMutation = useMutation({
    mutationFn: ({ id, newName }: { id: number; newName: string }) =>
      workflowApi.copyWorkflow(id, newName),
    onSuccess: () => {
      message.success('工作流复制成功');
      setCopyModalVisible(false);
      copyForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '复制失败');
    },
  });

  // 发布工作流
  const publishMutation = useMutation({
    mutationFn: workflowApi.publishWorkflow,
    onSuccess: () => {
      message.success('工作流发布成功');
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '发布失败');
    },
  });

  // 归档工作流
  const archiveMutation = useMutation({
    mutationFn: workflowApi.archiveWorkflow,
    onSuccess: () => {
      message.success('工作流归档成功');
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '归档失败');
    },
  });

  useEffect(() => {
    if (workflowsData) {
      setWorkflows(workflowsData.content);
      setPagination({
        total: workflowsData.totalElements,
        totalPages: workflowsData.totalPages,
      });
    }
  }, [workflowsData, setWorkflows, setPagination]);

  useEffect(() => {
    if (statsData) {
      setStats(statsData);
    }
  }, [statsData, setStats]);

  useEffect(() => {
    if (categoriesData) {
      setCategories(categoriesData);
    }
  }, [categoriesData, setCategories]);

  const handleSearch = (value: string) => {
    setFilters(prev => ({ ...prev, keyword: value }));
    setPagination({ page: 0 });
  };

  const handleFilterChange = (key: string, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setPagination({ page: 0 });
  };

  const handleTableChange = (pagination: any) => {
    setPagination({
      page: pagination.current - 1,
      size: pagination.pageSize,
    });
  };

  const handleDelete = (workflow: Workflow) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除工作流 "${workflow.name}" 吗？此操作不可恢复。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => deleteMutation.mutate(workflow.id),
    });
  };

  const handleCopy = (workflow: Workflow) => {
    setCopyingWorkflow(workflow);
    copyForm.setFieldsValue({
      newName: `${workflow.name} (副本)`,
    });
    setCopyModalVisible(true);
  };

  const handleCopySubmit = async () => {
    try {
      const values = await copyForm.validateFields();
      if (copyingWorkflow) {
        copyMutation.mutate({
          id: copyingWorkflow.id,
          newName: values.newName,
        });
      }
    } catch (error) {
      // 表单验证失败
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return 'green';
      case 'DRAFT':
        return 'orange';
      case 'ARCHIVED':
        return 'gray';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return '已发布';
      case 'DRAFT':
        return '草稿';
      case 'ARCHIVED':
        return '已归档';
      default:
        return status;
    }
  };

  const columns = [
    {
      title: '工作流名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: Workflow) => (
        <div>
          <div className="font-medium">{text}</div>
          {record.description && (
            <div className="text-sm text-gray-500 mt-1">
              {record.description}
            </div>
          )}
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => (
        <Tag color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 120,
      render: (category: string) => category || '-',
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      width: 200,
      render: (tags: string[]) => (
        <div>
          {tags?.map(tag => (
            <Tag key={tag} size="small">{tag}</Tag>
          ))}
        </div>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
      render: (version: number) => `v${version}`,
    },
    {
      title: '执行统计',
      key: 'stats',
      width: 120,
      render: (record: Workflow) => (
        <div className="text-sm">
          <div>执行: {record.executionCount || 0}</div>
          <div>成功率: {record.successRate ? `${(record.successRate * 100).toFixed(1)}%` : '-'}</div>
        </div>
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 150,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: '操作',
      key: 'actions',
      width: 200,
      render: (record: Workflow) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/workflow/${record.id}`)}
            />
          </Tooltip>
          
          <Tooltip title="执行工作流">
            <Button
              type="text"
              icon={<PlayCircleOutlined />}
              onClick={() => navigate(`/workflow/${record.id}/execute`)}
              disabled={record.status !== 'PUBLISHED'}
            />
          </Tooltip>
          
          <Dropdown
            menu={{
              items: [
                {
                  key: 'edit',
                  label: '编辑',
                  icon: <EditOutlined />,
                  onClick: () => navigate(`/workflow/${record.id}/edit`),
                },
                {
                  key: 'copy',
                  label: '复制',
                  icon: <CopyOutlined />,
                  onClick: () => handleCopy(record),
                },
                {
                  type: 'divider',
                },
                ...(record.status === 'DRAFT' ? [{
                  key: 'publish',
                  label: '发布',
                  icon: <PublishedWithChangesOutlined />,
                  onClick: () => publishMutation.mutate(record.id),
                }] : []),
                ...(record.status === 'PUBLISHED' ? [{
                  key: 'archive',
                  label: '归档',
                  icon: <ArchiveBoxOutlined />,
                  onClick: () => archiveMutation.mutate(record.id),
                }] : []),
                {
                  type: 'divider',
                },
                {
                  key: 'delete',
                  label: '删除',
                  icon: <DeleteOutlined />,
                  danger: true,
                  onClick: () => handleDelete(record),
                },
              ],
            }}
            trigger={['click']}
          >
            <Button type="text" icon={<MoreOutlined />} />
          </Dropdown>
        </Space>
      ),
    },
  ];

  return (
    <div className="p-6">
      {/* 统计卡片 */}
      {stats && (
        <Row gutter={16} className="mb-6">
          <Col span={6}>
            <Card>
              <Statistic
                title="总工作流"
                value={stats.total}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="草稿"
                value={stats.draft}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已发布"
                value={stats.published}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已归档"
                value={stats.archived}
                valueStyle={{ color: '#8c8c8c' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card>
        {/* 工具栏 */}
        <div className="flex justify-between items-center mb-4">
          <div className="flex items-center space-x-4">
            <Search
              placeholder="搜索工作流..."
              allowClear
              style={{ width: 300 }}
              onSearch={handleSearch}
            />
            
            <Select
              placeholder="状态"
              allowClear
              style={{ width: 120 }}
              onChange={(value) => handleFilterChange('status', value || '')}
            >
              <Option value="DRAFT">草稿</Option>
              <Option value="PUBLISHED">已发布</Option>
              <Option value="ARCHIVED">已归档</Option>
            </Select>
            
            <Select
              placeholder="分类"
              allowClear
              style={{ width: 150 }}
              onChange={(value) => handleFilterChange('category', value || '')}
            >
              {categories.map(category => (
                <Option key={category} value={category}>{category}</Option>
              ))}
            </Select>
          </div>
          
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/workflow/create')}
          >
            创建工作流
          </Button>
        </div>

        {/* 工作流表格 */}
        <Table
          columns={columns}
          dataSource={workflows}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: pagination.page + 1,
            pageSize: pagination.size,
            total: pagination.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
          }}
          onChange={handleTableChange}
        />
      </Card>

      {/* 复制工作流对话框 */}
      <Modal
        title="复制工作流"
        open={copyModalVisible}
        onOk={handleCopySubmit}
        onCancel={() => setCopyModalVisible(false)}
        confirmLoading={copyMutation.isPending}
      >
        <Form form={copyForm} layout="vertical">
          <Form.Item
            name="newName"
            label="新工作流名称"
            rules={[
              { required: true, message: '请输入工作流名称' },
              { max: 100, message: '名称长度不能超过100个字符' },
            ]}
          >
            <Input placeholder="请输入新的工作流名称" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}