import React, { useCallback, useEffect, useState } from 'react';
import ReactFlow, {
  Node,
  Edge,
  addEdge,
  Connection,
  useNodesState,
  useEdgesState,
  Controls,
  MiniMap,
  Background,
  BackgroundVariant,
  Panel,
} from 'reactflow';
import 'reactflow/dist/style.css';
import {
  Card,
  Button,
  Space,
  Drawer,
  Form,
  Input,
  Select,
  Switch,
  message,
  Modal,
  Tabs,
  Collapse,
  Tag,
} from 'antd';
import {
  SaveOutlined,
  PlayCircleOutlined,
  SettingOutlined,
  PlusOutlined,
  DeleteOutlined,
  CopyOutlined,
} from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workflowApi } from '../../services/api/workflow';
import { NODE_TYPES } from '../../types/workflow';
import type { WorkflowNode, WorkflowEdge, WorkflowDefinition } from '../../types/workflow';

const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;
const { Panel: CollapsePanel } = Collapse;

// 自定义节点组件
const CustomNode = ({ data, selected }: { data: any; selected: boolean }) => {
  const nodeType = NODE_TYPES.find(type => type.type === data.type);
  
  return (
    <div
      className={`px-4 py-2 shadow-md rounded-md bg-white border-2 ${
        selected ? 'border-blue-500' : 'border-gray-200'
      }`}
      style={{ minWidth: 150 }}
    >
      <div className="flex items-center">
        <div className="w-3 h-3 rounded-full bg-blue-500 mr-2" />
        <div className="font-medium">{data.name || nodeType?.displayName}</div>
      </div>
      {data.description && (
        <div className="text-xs text-gray-500 mt-1">{data.description}</div>
      )}
    </div>
  );
};

const nodeTypes = {
  custom: CustomNode,
};

export default function WorkflowDesigner() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEdit = Boolean(id);

  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [selectedNode, setSelectedNode] = useState<Node | null>(null);
  const [nodeConfigDrawer, setNodeConfigDrawer] = useState(false);
  const [workflowSettingsDrawer, setWorkflowSettingsDrawer] = useState(false);
  const [saveModalVisible, setSaveModalVisible] = useState(false);

  const [workflowForm] = Form.useForm();
  const [nodeForm] = Form.useForm();
  const [saveForm] = Form.useForm();

  // 获取工作流详情（编辑模式）
  const { data: workflow } = useQuery({
    queryKey: ['workflow', id],
    queryFn: () => workflowApi.getWorkflow(Number(id)),
    enabled: isEdit,
  });

  // 保存工作流
  const saveMutation = useMutation({
    mutationFn: async (data: any) => {
      const definition: WorkflowDefinition = {
        nodes: nodes.map(node => ({
          id: node.id,
          type: node.data.type,
          name: node.data.name,
          data: node.data,
          position: node.position,
          config: node.data.config || {},
        })),
        edges: edges.map(edge => ({
          id: edge.id,
          source: edge.source,
          target: edge.target,
          sourceHandle: edge.sourceHandle,
          targetHandle: edge.targetHandle,
        })),
        variables: data.variables || {},
        settings: data.settings || {},
      };

      if (isEdit) {
        return workflowApi.updateWorkflow(Number(id), {
          ...data,
          definition,
        });
      } else {
        return workflowApi.createWorkflow({
          ...data,
          definition,
        });
      }
    },
    onSuccess: (result) => {
      message.success(isEdit ? '工作流更新成功' : '工作流创建成功');
      setSaveModalVisible(false);
      if (!isEdit) {
        navigate(`/workflow/${result.id}/edit`);
      }
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || '保存失败');
    },
  });

  // 初始化工作流数据
  useEffect(() => {
    if (workflow) {
      const definition = workflow.definition;
      
      // 设置节点
      const flowNodes = definition.nodes.map(node => ({
        id: node.id,
        type: 'custom',
        position: node.position,
        data: {
          ...node.data,
          type: node.type,
          name: node.name,
          config: node.config,
        },
      }));
      
      // 设置边
      const flowEdges = definition.edges.map(edge => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle,
        targetHandle: edge.targetHandle,
      }));
      
      setNodes(flowNodes);
      setEdges(flowEdges);
      
      // 设置表单数据
      workflowForm.setFieldsValue({
        name: workflow.name,
        description: workflow.description,
        category: workflow.category,
        tags: workflow.tags,
        variables: definition.variables,
        settings: definition.settings,
      });
    }
  }, [workflow, setNodes, setEdges, workflowForm]);

  // 连接节点
  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  // 节点选择
  const onNodeClick = useCallback((event: React.MouseEvent, node: Node) => {
    setSelectedNode(node);
  }, []);

  // 添加节点
  const addNode = (nodeType: string) => {
    const nodeTypeInfo = NODE_TYPES.find(type => type.type === nodeType);
    if (!nodeTypeInfo) return;

    const newNode: Node = {
      id: `${nodeType}_${Date.now()}`,
      type: 'custom',
      position: { x: Math.random() * 400, y: Math.random() * 400 },
      data: {
        type: nodeType,
        name: nodeTypeInfo.displayName,
        description: '',
        config: {},
      },
    };

    setNodes((nds) => [...nds, newNode]);
  };

  // 删除选中节点
  const deleteSelectedNode = () => {
    if (!selectedNode) return;
    
    setNodes((nds) => nds.filter((node) => node.id !== selectedNode.id));
    setEdges((eds) => eds.filter((edge) => 
      edge.source !== selectedNode.id && edge.target !== selectedNode.id
    ));
    setSelectedNode(null);
  };

  // 复制选中节点
  const copySelectedNode = () => {
    if (!selectedNode) return;
    
    const newNode: Node = {
      ...selectedNode,
      id: `${selectedNode.data.type}_${Date.now()}`,
      position: {
        x: selectedNode.position.x + 50,
        y: selectedNode.position.y + 50,
      },
      data: {
        ...selectedNode.data,
        name: `${selectedNode.data.name} (副本)`,
      },
    };
    
    setNodes((nds) => [...nds, newNode]);
  };

  // 打开节点配置
  const openNodeConfig = () => {
    if (!selectedNode) return;
    
    nodeForm.setFieldsValue({
      name: selectedNode.data.name,
      description: selectedNode.data.description,
      config: selectedNode.data.config,
    });
    setNodeConfigDrawer(true);
  };

  // 保存节点配置
  const saveNodeConfig = async () => {
    try {
      const values = await nodeForm.validateFields();
      
      setNodes((nds) =>
        nds.map((node) =>
          node.id === selectedNode?.id
            ? {
                ...node,
                data: {
                  ...node.data,
                  name: values.name,
                  description: values.description,
                  config: values.config || {},
                },
              }
            : node
        )
      );
      
      setNodeConfigDrawer(false);
      message.success('节点配置已保存');
    } catch (error) {
      // 表单验证失败
    }
  };

  // 打开保存对话框
  const openSaveModal = () => {
    if (isEdit && workflow) {
      saveForm.setFieldsValue({
        name: workflow.name,
        description: workflow.description,
        category: workflow.category,
        tags: workflow.tags,
      });
    }
    setSaveModalVisible(true);
  };

  // 保存工作流
  const handleSave = async () => {
    try {
      const values = await saveForm.validateFields();
      saveMutation.mutate(values);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 执行工作流
  const executeWorkflow = () => {
    if (!isEdit || !workflow) {
      message.warning('请先保存工作流');
      return;
    }
    
    navigate(`/workflow/${workflow.id}/execute`);
  };

  return (
    <div className="h-screen flex flex-col">
      {/* 顶部工具栏 */}
      <div className="bg-white border-b px-4 py-2 flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <h2 className="text-lg font-semibold">
            {isEdit ? `编辑工作流: ${workflow?.name}` : '创建工作流'}
          </h2>
          {selectedNode && (
            <Tag color="blue">
              已选择: {selectedNode.data.name}
            </Tag>
          )}
        </div>
        
        <Space>
          <Button
            icon={<SettingOutlined />}
            onClick={() => setWorkflowSettingsDrawer(true)}
          >
            工作流设置
          </Button>
          
          <Button
            icon={<SaveOutlined />}
            onClick={openSaveModal}
          >
            保存
          </Button>
          
          <Button
            type="primary"
            icon={<PlayCircleOutlined />}
            onClick={executeWorkflow}
            disabled={!isEdit}
          >
            执行
          </Button>
        </Space>
      </div>

      <div className="flex-1 flex">
        {/* 左侧节点面板 */}
        <div className="w-64 bg-white border-r p-4">
          <h3 className="font-medium mb-4">节点类型</h3>
          
          <Collapse defaultActiveKey={['control', 'ai']}>
            <CollapsePanel header="控制节点" key="control">
              <div className="space-y-2">
                {NODE_TYPES.filter(type => type.category === 'control').map(nodeType => (
                  <Button
                    key={nodeType.type}
                    block
                    size="small"
                    onClick={() => addNode(nodeType.type)}
                  >
                    {nodeType.displayName}
                  </Button>
                ))}
              </div>
            </CollapsePanel>
            
            <CollapsePanel header="AI节点" key="ai">
              <div className="space-y-2">
                {NODE_TYPES.filter(type => type.category === 'ai').map(nodeType => (
                  <Button
                    key={nodeType.type}
                    block
                    size="small"
                    onClick={() => addNode(nodeType.type)}
                  >
                    {nodeType.displayName}
                  </Button>
                ))}
              </div>
            </CollapsePanel>
            
            <CollapsePanel header="集成节点" key="integration">
              <div className="space-y-2">
                {NODE_TYPES.filter(type => type.category === 'integration').map(nodeType => (
                  <Button
                    key={nodeType.type}
                    block
                    size="small"
                    onClick={() => addNode(nodeType.type)}
                  >
                    {nodeType.displayName}
                  </Button>
                ))}
              </div>
            </CollapsePanel>
            
            <CollapsePanel header="数据节点" key="data">
              <div className="space-y-2">
                {NODE_TYPES.filter(type => type.category === 'data').map(nodeType => (
                  <Button
                    key={nodeType.type}
                    block
                    size="small"
                    onClick={() => addNode(nodeType.type)}
                  >
                    {nodeType.displayName}
                  </Button>
                ))}
              </div>
            </CollapsePanel>
            
            <CollapsePanel header="高级节点" key="advanced">
              <div className="space-y-2">
                {NODE_TYPES.filter(type => type.category === 'advanced').map(nodeType => (
                  <Button
                    key={nodeType.type}
                    block
                    size="small"
                    onClick={() => addNode(nodeType.type)}
                  >
                    {nodeType.displayName}
                  </Button>
                ))}
              </div>
            </CollapsePanel>
          </Collapse>
          
          {/* 选中节点操作 */}
          {selectedNode && (
            <div className="mt-6 pt-4 border-t">
              <h4 className="font-medium mb-2">节点操作</h4>
              <Space direction="vertical" className="w-full">
                <Button
                  block
                  size="small"
                  icon={<SettingOutlined />}
                  onClick={openNodeConfig}
                >
                  配置节点
                </Button>
                <Button
                  block
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={copySelectedNode}
                >
                  复制节点
                </Button>
                <Button
                  block
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={deleteSelectedNode}
                >
                  删除节点
                </Button>
              </Space>
            </div>
          )}
        </div>

        {/* 中间画布区域 */}
        <div className="flex-1 relative">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={onNodeClick}
            nodeTypes={nodeTypes}
            fitView
          >
            <Controls />
            <MiniMap />
            <Background variant={BackgroundVariant.Dots} gap={12} size={1} />
            
            <Panel position="top-right">
              <Card size="small" className="w-48">
                <div className="text-sm">
                  <div>节点数: {nodes.length}</div>
                  <div>连接数: {edges.length}</div>
                </div>
              </Card>
            </Panel>
          </ReactFlow>
        </div>
      </div>

      {/* 节点配置抽屉 */}
      <Drawer
        title="节点配置"
        placement="right"
        width={400}
        open={nodeConfigDrawer}
        onClose={() => setNodeConfigDrawer(false)}
        extra={
          <Space>
            <Button onClick={() => setNodeConfigDrawer(false)}>取消</Button>
            <Button type="primary" onClick={saveNodeConfig}>
              保存
            </Button>
          </Space>
        }
      >
        <Form form={nodeForm} layout="vertical">
          <Form.Item
            name="name"
            label="节点名称"
            rules={[{ required: true, message: '请输入节点名称' }]}
          >
            <Input placeholder="请输入节点名称" />
          </Form.Item>
          
          <Form.Item name="description" label="节点描述">
            <TextArea rows={3} placeholder="请输入节点描述" />
          </Form.Item>
          
          {/* 根据节点类型显示不同的配置项 */}
          {selectedNode?.data.type === 'llm_chat' && (
            <>
              <Form.Item name={['config', 'model']} label="模型">
                <Select placeholder="选择模型">
                  <Option value="gpt-3.5-turbo">GPT-3.5 Turbo</Option>
                  <Option value="gpt-4">GPT-4</Option>
                  <Option value="claude-3">Claude-3</Option>
                </Select>
              </Form.Item>
              
              <Form.Item name={['config', 'prompt']} label="提示词">
                <TextArea rows={4} placeholder="请输入提示词" />
              </Form.Item>
              
              <Form.Item name={['config', 'temperature']} label="温度">
                <Input type="number" min={0} max={2} step={0.1} />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data.type === 'knowledge_retrieval' && (
            <>
              <Form.Item name={['config', 'knowledgeBaseId']} label="知识库">
                <Select placeholder="选择知识库">
                  {/* 这里应该从API获取知识库列表 */}
                </Select>
              </Form.Item>
              
              <Form.Item name={['config', 'query']} label="查询内容">
                <TextArea rows={3} placeholder="请输入查询内容或变量" />
              </Form.Item>
              
              <Form.Item name={['config', 'topK']} label="返回数量">
                <Input type="number" min={1} max={20} />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data.type === 'http_request' && (
            <>
              <Form.Item name={['config', 'method']} label="请求方法">
                <Select>
                  <Option value="GET">GET</Option>
                  <Option value="POST">POST</Option>
                  <Option value="PUT">PUT</Option>
                  <Option value="DELETE">DELETE</Option>
                </Select>
              </Form.Item>
              
              <Form.Item name={['config', 'url']} label="请求URL">
                <Input placeholder="https://api.example.com/endpoint" />
              </Form.Item>
              
              <Form.Item name={['config', 'headers']} label="请求头">
                <TextArea rows={3} placeholder='{"Content-Type": "application/json"}' />
              </Form.Item>
              
              <Form.Item name={['config', 'body']} label="请求体">
                <TextArea rows={4} placeholder="请求体内容" />
              </Form.Item>
            </>
          )}
        </Form>
      </Drawer>

      {/* 工作流设置抽屉 */}
      <Drawer
        title="工作流设置"
        placement="right"
        width={400}
        open={workflowSettingsDrawer}
        onClose={() => setWorkflowSettingsDrawer(false)}
      >
        <Form form={workflowForm} layout="vertical">
          <Form.Item name="name" label="工作流名称">
            <Input placeholder="请输入工作流名称" />
          </Form.Item>
          
          <Form.Item name="description" label="工作流描述">
            <TextArea rows={3} placeholder="请输入工作流描述" />
          </Form.Item>
          
          <Form.Item name="category" label="分类">
            <Input placeholder="请输入分类" />
          </Form.Item>
          
          <Form.Item name="tags" label="标签">
            <Select mode="tags" placeholder="请输入标签">
            </Select>
          </Form.Item>
        </Form>
      </Drawer>

      {/* 保存工作流对话框 */}
      <Modal
        title={isEdit ? '更新工作流' : '保存工作流'}
        open={saveModalVisible}
        onOk={handleSave}
        onCancel={() => setSaveModalVisible(false)}
        confirmLoading={saveMutation.isPending}
      >
        <Form form={saveForm} layout="vertical">
          <Form.Item
            name="name"
            label="工作流名称"
            rules={[{ required: true, message: '请输入工作流名称' }]}
          >
            <Input placeholder="请输入工作流名称" />
          </Form.Item>
          
          <Form.Item name="description" label="工作流描述">
            <TextArea rows={3} placeholder="请输入工作流描述" />
          </Form.Item>
          
          <Form.Item name="category" label="分类">
            <Input placeholder="请输入分类" />
          </Form.Item>
          
          <Form.Item name="tags" label="标签">
            <Select mode="tags" placeholder="请输入标签" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}