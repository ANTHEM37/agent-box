import { useState, useCallback, useEffect, useRef, useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button, message, Space, Input, Select, Card, Form, Modal, Typography, Tag, Divider, Switch, Tooltip } from 'antd'
import { 
  ReactFlow, 
  addEdge, 
  applyEdgeChanges, 
  applyNodeChanges, 
  Background, 
  Controls, 
  MiniMap, 
  Panel, 
  type Node, 
  type Edge, 
  type Connection, 
  type NodeChange, 
  type EdgeChange
} from 'reactflow'
import 'reactflow/dist/style.css'
import { workflowApi } from '../../services/api/workflow'
import { useWorkflowStore } from '../../stores/workflow'
import type { WorkflowItem, WorkflowNode, WorkflowEdge } from '../../types/workflow'

// 定义节点类型
const nodeTypes = [
  { value: 'start', label: '开始节点', color: '#52c41a', icon: '▶️' },
  { value: 'end', label: '结束节点', color: '#ff4d4f', icon: '⏹️' },
  { value: 'llm_chat', label: 'LLM对话节点', color: '#1890ff', icon: '💬' },
  { value: 'knowledge_retrieval', label: '知识库检索节点', color: '#722ed1', icon: '📚' },
  { value: 'http_request', label: 'HTTP请求节点', color: '#fa8c16', icon: '🌐' },
  { value: 'condition', label: '条件判断节点', color: '#13c2c2', icon: '🔀' },
  { value: 'variable_set', label: '变量设置节点', color: '#eb2f96', icon: '⚙️' },
  { value: 'code_execution', label: '代码执行节点', color: '#52c41a', icon: '💻' },
  { value: 'user_input', label: '用户输入节点', color: '#722ed1', icon: '👤' }
]

// 节点颜色映射
const nodeColorMap: Record<string, string> = {}
nodeTypes.forEach(type => {
  nodeColorMap[type.value] = type.color
})

// 自定义节点组件
const CustomNode = ({ data, id }: { data: any; id: string }) => {
  const setSelectedNode = useWorkflowStore((state) => state.setSelectedNode);
  const setHasUnsavedChanges = useWorkflowStore((state) => state.setHasUnsavedChanges);
  const removeNode = useWorkflowStore((state) => state.removeNode);
  
  // 通过自定义事件来通知父组件打开配置模态框
  const handleConfigNode = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    console.log('配置节点按钮被点击，节点ID:', id);
    // 构造正确的节点对象
    const nodeData = {
      id,
      data,
      type: 'custom' as const,
      position: { x: 0, y: 0 } // 位置信息在实际节点中会有，这里只是构造一个对象
    };
    setSelectedNode(nodeData as any);
    // 触发自定义事件来打开配置模态框
    const event = new CustomEvent('openNodeConfig', { detail: { id, data } });
    window.dispatchEvent(event);
  };
  
  const handleDeleteNode = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    console.log('删除节点按钮被点击，节点ID:', id);
    // 使用store中的removeNode方法删除节点
    removeNode(id);
    // 设置未保存状态
    setHasUnsavedChanges(true);
  };

  return (
    <div style={{
      padding: '10px',
      border: '2px solid',
      borderRadius: '4px',
      backgroundColor: '#fff',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      borderColor: data.color || '#1890ff',
      minWidth: '150px',
      textAlign: 'center',
      position: 'relative'
    }}>
      {/* 节点操作按钮 */}
      <div style={{
        position: 'absolute',
        top: '-20px',
        right: '0',
        display: 'flex',
        gap: '4px'
      }}>
        <Button 
          size="small" 
          type="primary" 
          icon={<span>⚙️</span>}
          onClick={handleConfigNode}
          onMouseDown={(e) => e.stopPropagation()}
          style={{ width: '24px', height: '24px', padding: '0' }}
        />
        <Button 
          size="small" 
          danger 
          icon={<span>🗑️</span>}
          onClick={handleDeleteNode}
          onMouseDown={(e) => e.stopPropagation()}
          style={{ width: '24px', height: '24px', padding: '0' }}
        />
      </div>
      
      <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>
        {data.icon} {data.label}
      </div>
      {data.name && (
        <div style={{ fontSize: '12px', color: '#666', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {data.name}
        </div>
      )}
      {data.config && Object.keys(data.config).length > 0 && (
        <div style={{ marginTop: '5px', fontSize: '10px', color: '#999' }}>
          已配置
        </div>
      )}
    </div>
  )
}

const WorkflowDesigner = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const reactFlowRef = useRef<any>(null)
  const [nodes, setNodes] = useState<Node[]>([])
  const [edges, setEdges] = useState<Edge[]>([])
  const [selectedNode, setSelectedNode] = useState<Node | null>(null)
  const [workflowName, setWorkflowName] = useState('')
  const [workflowDescription, setWorkflowDescription] = useState('')
  const [workflowCategory, setWorkflowCategory] = useState('')
  const [workflowTags, setWorkflowTags] = useState<string[]>([])
  const [isTemplate, setIsTemplate] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [nodeModalVisible, setNodeModalVisible] = useState(false)
  const [tagInput, setTagInput] = useState('')
  const [categories, setCategories] = useState<string[]>([])
  const [nodeForm] = Form.useForm()
  const [debugMode, setDebugMode] = useState(false)
  const [autoSave, setAutoSave] = useState(true)
  
  // 从store获取状态
  const {
    currentWorkflow,
    workflowDefinition,
    setCurrentWorkflow,
    setWorkflowDefinition,
    addNode,
    updateNode,
    removeNode,
    addEdge: addStoreEdge,
    removeEdge: removeStoreEdge,
    setHasUnsavedChanges,
    reset
  } = useWorkflowStore()

  // 加载分类列表
  const loadCategories = useCallback(async () => {
    try {
      const response = await workflowApi.getUserCategories()
      setCategories(response.data.data)
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }, [])

  // 加载工作流数据
  const loadWorkflow = useCallback(async () => {
    if (!id) return
    
    try {
      // 重置store状态
      reset();
      
      const response = await workflowApi.get(Number(id))
      const workflow = response.data.data
      
      setCurrentWorkflow(workflow)
      setWorkflowName(workflow.name)
      setWorkflowDescription(workflow.description || '')
      setWorkflowCategory(workflow.category || '')
      setWorkflowTags(workflow.tags || [])
      setIsTemplate(workflow.isTemplate || false)
      
      // 转换节点数据
      const loadedNodes = workflow.definition.nodes.map((node: WorkflowNode) => ({
        id: node.id,
        type: 'custom',
        position: { x: node.position.x, y: node.position.y },
        data: { 
          ...node,
          label: node.name,
          color: nodeColorMap[node.type] || '#1890ff',
          icon: nodeTypes.find(t => t.value === node.type)?.icon || '⚙️'
        }
      }))
      
      // 转换连接数据
      const loadedEdges = workflow.definition.edges.map((edge: WorkflowEdge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle,
        targetHandle: edge.targetHandle,
        type: edge.type || 'default',
        data: edge.data,
        animated: true,
        style: { stroke: '#1890ff', strokeWidth: 2 }
      }))
      
      setNodes(loadedNodes)
      setEdges(loadedEdges)
    } catch (error: any) {
      message.error(error?.response?.data?.message || '加载工作流失败')
    }
  }, [id, setCurrentWorkflow, reset])

  // 监听store中的workflowDefinition变化，同步到ReactFlow组件
  useEffect(() => {
    // 只有当workflowDefinition中有实际数据且与当前工作流匹配时才更新ReactFlow组件
    // 避免初始化时用空数据或旧数据覆盖已加载的工作流数据
    if (workflowDefinition.nodes.length > 0 || workflowDefinition.edges.length > 0) {
      // 检查是否与当前加载的工作流匹配
      if (currentWorkflow && workflowDefinition.nodes.some(node => 
        currentWorkflow.definition.nodes.some((cn: any) => cn.id === node.id)
      )) {
        // 转换节点数据
        const loadedNodes = workflowDefinition.nodes.map((node: WorkflowNode) => ({
          id: node.id,
          type: 'custom',
          position: { x: node.position.x, y: node.position.y },
          data: { 
            ...node,
            label: node.name,
            color: nodeColorMap[node.type] || '#1890ff',
            icon: nodeTypes.find(t => t.value === node.type)?.icon || '⚙️'
          }
        }))
        
        // 转换连接数据
        const loadedEdges = workflowDefinition.edges.map((edge: WorkflowEdge) => ({
          id: edge.id,
          source: edge.source,
          target: edge.target,
          sourceHandle: edge.sourceHandle,
          targetHandle: edge.targetHandle,
          type: edge.type || 'default',
          data: edge.data,
          animated: true,
          style: { stroke: '#1890ff', strokeWidth: 2 }
        }))
        
        setNodes(loadedNodes)
        setEdges(loadedEdges)
      }
    }
  }, [workflowDefinition, currentWorkflow])

  useEffect(() => {
    loadCategories()
    if (id) {
      loadWorkflow()
    }
  }, [id, loadWorkflow, loadCategories])

  // 自动保存
  useEffect(() => {
    if (autoSave && currentWorkflow) {
      const timer = setTimeout(() => {
        if (currentWorkflow && workflowName.trim()) {
          onSave()
        }
      }, 30000) // 30秒自动保存一次
      
      return () => clearTimeout(timer)
    }
  }, [currentWorkflow, workflowName, autoSave])

  // 处理节点变化
  const onNodesChange = useCallback(
    (changes: NodeChange[]) => {
      setNodes((nds) => applyNodeChanges(changes, nds))
    },
    [setNodes]
  )

  // 处理连接变化
  const onEdgesChange = useCallback(
    (changes: EdgeChange[]) => {
      setEdges((eds) => applyEdgeChanges(changes, eds))
    },
    [setEdges]
  )

  // 处理连接添加
  const onConnect = useCallback(
    (connection: Connection) => {
      // 验证连接逻辑，防止循环连接
      const targetNodeId = connection.target;
      const sourceNodeId = connection.source;
      
      // 确保节点ID不为空
      if (!targetNodeId || !sourceNodeId) {
        return;
      }
      
      // 检查是否形成循环
      const hasCycle = (nodeId: string, visited: Set<string> = new Set()): boolean => {
        if (nodeId === sourceNodeId) return true;
        if (visited.has(nodeId)) return false;
        
        visited.add(nodeId);
        
        const outgoingEdges = edges.filter(edge => edge.source === nodeId);
        for (const edge of outgoingEdges) {
          // 确保目标节点ID不为空
          if (edge.target && hasCycle(edge.target, new Set(visited))) {
            return true;
          }
        }
        
        return false;
      }
      
      if (hasCycle(targetNodeId)) {
        message.warning('不能创建循环连接');
        return;
      }
      
      const newEdge = {
        ...connection,
        id: `edge-${Date.now()}`,
        type: 'default',
        animated: true,
        style: { stroke: '#1890ff', strokeWidth: 2 }
      };
      setEdges((eds) => addEdge(newEdge, eds));
      
      // 添加到store
      addStoreEdge({
        id: newEdge.id,
        source: newEdge.source,
        target: newEdge.target,
        sourceHandle: newEdge.sourceHandle || undefined,
        targetHandle: newEdge.targetHandle || undefined,
        type: newEdge.type
      } as WorkflowEdge);
    },
    [edges, addStoreEdge]
  )

  // 处理节点点击
  const onNodeClick = useCallback((_event: React.MouseEvent, node: Node) => {
    // 现在节点操作直接在节点上，不需要在这里设置选中节点
    // 但如果需要在右侧面板显示节点详情，可以保留这个逻辑
    setSelectedNode(node)
  }, [])

  // 添加自定义事件监听器来打开节点配置模态框
  useEffect(() => {
    const handleOpenNodeConfig = (e: Event) => {
      console.log('接收到openNodeConfig事件');
      const customEvent = e as CustomEvent;
      const { id, data } = customEvent.detail;
      console.log('事件详情:', { id, data });
      // 构造正确的节点对象
      const nodeData = {
        id,
        data,
        type: 'custom',
        position: { x: 0, y: 0 }
      };
      setSelectedNode(nodeData as any);
      setNodeModalVisible(true);
      console.log('已设置选中节点并显示模态框');
    };

    console.log('添加openNodeConfig事件监听器');
    window.addEventListener('openNodeConfig', handleOpenNodeConfig);
    return () => {
      console.log('移除openNodeConfig事件监听器');
      window.removeEventListener('openNodeConfig', handleOpenNodeConfig);
    };
  }, [setSelectedNode, setNodeModalVisible]);

  // 处理画布点击（取消节点选择）
  const onPaneClick = useCallback(() => {
    setSelectedNode(null)
  }, [])

  // 添加节点
  const onAddNode = useCallback((nodeType: string) => {
    const selectedType = nodeTypes.find(type => type.value === nodeType)
    if (!selectedType) return

    const newNode: Node = {
      id: `node-${Date.now()}`,
      type: 'custom',
      position: { x: Math.random() * 500, y: Math.random() * 500 },
      data: { 
        label: selectedType.label,
        type: nodeType,
        name: selectedType.label,
        color: selectedType.color,
        icon: selectedType.icon
      }
    }

    setNodes((nds) => nds.concat(newNode))
    
    // 添加到store
    addNode({
      id: newNode.id,
      type: nodeType,
      name: selectedType.label,
      position: newNode.position,
      data: {}
    } as WorkflowNode)
    
    // 自动选中新节点
    setSelectedNode(newNode)
  }, [addNode])

  // 添加标签
  const addTag = () => {
    if (tagInput.trim() && !workflowTags.includes(tagInput.trim())) {
      setWorkflowTags([...workflowTags, tagInput.trim()])
      setTagInput('')
    }
  }

  // 删除标签
  const removeTag = (tag: string) => {
    setWorkflowTags(workflowTags.filter(t => t !== tag))
  }

  // 保存工作流
  const onSave = useCallback(async () => {
    if (!workflowName.trim()) {
      message.error('请输入工作流名称')
      return
    }

    setIsSaving(true)
    
    try {
      // 转换节点数据
      const workflowNodes: WorkflowNode[] = nodes.map(node => ({
        id: node.id,
        type: node.data.type,
        name: node.data.name,
        position: node.position,
        data: node.data,
        config: node.data.config
      }))
      
      // 转换连接数据
      const workflowEdges: WorkflowEdge[] = edges.map(edge => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle || undefined,
        targetHandle: edge.targetHandle || undefined,
        type: edge.type,
        data: edge.data
      }))
      
      const workflowDefinition = {
        nodes: workflowNodes,
        edges: workflowEdges,
        variables: {},
        settings: {}
      }
      
      if (id) {
        // 更新现有工作流
        await workflowApi.update(Number(id), {
          name: workflowName,
          description: workflowDescription,
          definition: workflowDefinition,
          category: workflowCategory,
          tags: workflowTags,
          status: currentWorkflow?.status
        })
        message.success('工作流更新成功')
      } else {
        // 创建新工作流
        const response = await workflowApi.create({
          name: workflowName,
          description: workflowDescription,
          definition: workflowDefinition,
          category: workflowCategory,
          tags: workflowTags,
          isTemplate: isTemplate
        })
        // 重定向到编辑页面
        navigate(`/workflow/edit/${response.data.data.id}`)
        message.success('工作流创建成功')
      }
      
      setHasUnsavedChanges(false)
    } catch (error: any) {
      message.error(error?.response?.data?.message || '保存失败')
    } finally {
      setIsSaving(false)
    }
  }, [id, workflowName, workflowDescription, workflowCategory, workflowTags, isTemplate, nodes, edges, navigate, setHasUnsavedChanges, currentWorkflow?.status])

  // 发布工作流
  const onPublish = useCallback(async () => {
    if (!id) return
    
    try {
      await workflowApi.publish(Number(id))
      message.success('工作流发布成功')
      // 重新加载工作流数据
      loadWorkflow()
    } catch (error: any) {
      message.error(error?.response?.data?.message || '发布失败')
    }
  }, [id, loadWorkflow])

  // 归档工作流
  const onArchive = useCallback(async () => {
    if (!id) return
    
    try {
      await workflowApi.archive(Number(id))
      message.success('工作流归档成功')
      // 重新加载工作流数据
      loadWorkflow()
    } catch (error: any) {
      message.error(error?.response?.data?.message || '归档失败')
    }
  }, [id, loadWorkflow])

  // 执行工作流
  const onExecute = useCallback(async () => {
    if (!id) return
    
    try {
      await workflowApi.execute({ workflowId: Number(id) })
      message.success('工作流执行已启动')
    } catch (error: any) {
      message.error(error?.response?.data?.message || '执行失败')
    }
  }, [id])

  // 删除选中节点
  const onDeleteNode = useCallback(() => {
    if (!selectedNode) return
    
    setNodes((nds) => nds.filter((node) => node.id !== selectedNode.id))
    setEdges((eds) => eds.filter((edge) => edge.source !== selectedNode.id && edge.target !== selectedNode.id))
    
    // 从store中删除
    removeNode(selectedNode.id)
    setSelectedNode(null)
  }, [selectedNode, removeNode])

  // 保存节点配置
  const onSaveNodeConfig = useCallback(async () => {
    try {
      const values = await nodeForm.validateFields()
      
      if (selectedNode) {
        // 更新节点数据
        const updatedNode = {
          ...selectedNode,
          data: {
            ...selectedNode.data,
            config: values,
            name: values.nodeName || selectedNode.data.name
          }
        }
        
        setNodes((nds) => 
          nds.map((node) => 
            node.id === selectedNode.id ? updatedNode : node
          )
        )
        
        // 更新store
        updateNode(selectedNode.id, {
          config: values,
          name: values.nodeName || selectedNode.data.name
        } as Partial<WorkflowNode>)
        
        setNodeModalVisible(false)
        message.success('节点配置已保存')
      }
    } catch (error) {
      console.error('验证失败:', error)
    }
  }, [nodeForm, selectedNode, updateNode])

  // 居中视图
  const onFitView = useCallback(() => {
    if (reactFlowRef.current) {
      reactFlowRef.current.fitView();
    }
  }, [])

  // 导出为图片
  const onExportImage = useCallback(() => {
    // 这里可以实现导出为图片的功能
    message.info('导出功能待实现')
  }, [])

  // 使用useMemo来避免每次渲染时创建新的nodeTypes对象
  const customNodeTypes = useMemo(() => ({ custom: CustomNode }), []);

  return (
    <div style={{ height: '100%', width: '100%' }}>
      <ReactFlow
        ref={reactFlowRef}
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        onNodeClick={onNodeClick}
        onPaneClick={onPaneClick}
        fitView
        attributionPosition="bottom-left"
        nodeTypes={customNodeTypes}
        style={{ height: '100%', width: '100%' }}
      >
        <Background gap={12} size={1} />
        <Controls />
        <MiniMap 
          nodeColor={(n) => n.data?.color || '#1890ff'}
          maskColor="rgba(0, 0, 0, 0.1)"
        />
        
        <Panel position="top-left">
          <div style={{ maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>
            <Card size="small" title="工作流信息" style={{ width: 300, marginBottom: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Input 
                  placeholder="工作流名称" 
                  value={workflowName}
                  onChange={(e) => setWorkflowName(e.target.value)}
                />
                <Input.TextArea 
                  placeholder="工作流描述" 
                  value={workflowDescription}
                  onChange={(e) => setWorkflowDescription(e.target.value)}
                  rows={2}
                />
                <Input 
                  placeholder="分类" 
                  value={workflowCategory}
                  onChange={(e) => setWorkflowCategory(e.target.value)}
                />
                <div>
                  <div style={{ display: 'flex', marginBottom: 8 }}>
                    <Input 
                      placeholder="添加标签" 
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      onPressEnter={addTag}
                    />
                    <Button onClick={addTag} style={{ marginLeft: 8 }}>添加</Button>
                  </div>
                  <div>
                    {workflowTags.map(tag => (
                      <Tag 
                        key={tag} 
                        closable 
                        onClose={() => removeTag(tag)}
                        style={{ marginBottom: 4 }}
                      >
                        {tag}
                      </Tag>
                    ))}
                  </div>
                </div>
                <div>
                  <label>
                    <input 
                      type="checkbox" 
                      checked={isTemplate} 
                      onChange={(e) => setIsTemplate(e.target.checked)}
                      style={{ marginRight: 8 }}
                    />
                    保存为模板
                  </label>
                </div>
                <Divider style={{ margin: '8px 0' }} />
                <Space>
                  <Button type="primary" onClick={onSave} loading={isSaving}>
                    保存
                  </Button>
                  {id && currentWorkflow?.status === 'DRAFT' && (
                    <Button onClick={onPublish}>
                      发布
                    </Button>
                  )}
                  {id && currentWorkflow?.status === 'PUBLISHED' && (
                    <Button onClick={onArchive}>
                      归档
                    </Button>
                  )}
                  {id && (
                    <Button onClick={onExecute}>
                      执行
                    </Button>
                  )}
                  <Button onClick={() => navigate('/workflows')}>
                    返回列表
                  </Button>
                </Space>
              </Space>
            </Card>
            
            <Card size="small" title="节点库" style={{ width: 300, marginBottom: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                {nodeTypes.map((type) => (
                  <Button
                    key={type.value}
                    style={{ width: '100%', textAlign: 'left' }}
                    onClick={() => onAddNode(type.value)}
                  >
                    <span style={{ color: type.color, marginRight: 8 }}>{type.icon}</span> {type.label}
                  </Button>
                ))}
              </Space>
            </Card>
            
            <Card size="small" title="调试工具" style={{ width: 300 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>调试模式</span>
                  <Switch checked={debugMode} onChange={setDebugMode} />
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>自动保存</span>
                  <Switch checked={autoSave} onChange={setAutoSave} />
                </div>
                <Button 
                  style={{ width: '100%' }}
                  onClick={onFitView}
                >
                  居中视图
                </Button>
                <Button 
                  style={{ width: '100%' }}
                  onClick={onExportImage}
                >
                  导出图片
                </Button>
              </Space>
            </Card>
          </div>
        </Panel>
        
        <Panel position="top-right">
          <div style={{ maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>
            {selectedNode && (
              <Card size="small" title="节点详情" style={{ width: 300 }}>
                <div style={{ marginBottom: 12 }}>
                  <Typography.Text strong>节点类型:</Typography.Text>
                  <Typography.Text style={{ marginLeft: 8 }}>
                    {selectedNode.data?.label}
                  </Typography.Text>
                </div>
                <div style={{ marginBottom: 12 }}>
                  <Typography.Text strong>节点ID:</Typography.Text>
                  <Typography.Text style={{ marginLeft: 8 }} copyable>
                    {selectedNode.id}
                  </Typography.Text>
                </div>
                {selectedNode.data?.config && Object.keys(selectedNode.data.config).length > 0 && (
                  <div>
                    <Typography.Text strong>配置信息:</Typography.Text>
                    <div style={{ marginTop: 8, maxHeight: 200, overflowY: 'auto' }}>
                      {Object.entries(selectedNode.data.config).map(([key, value]) => (
                        <div key={key} style={{ marginBottom: 4 }}>
                          <Typography.Text type="secondary">{key}:</Typography.Text>
                          <Typography.Text style={{ marginLeft: 4 }}>
                            {typeof value === 'object' ? JSON.stringify(value) : String(value)}
                          </Typography.Text>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </Card>
            )}
          </div>
        </Panel>
      </ReactFlow>
      
      <Modal
        title={`${selectedNode?.data?.label || '节点'} 配置`}
        open={nodeModalVisible}
        onOk={onSaveNodeConfig}
        onCancel={() => setNodeModalVisible(false)}
        width={600}
        okText="保存"
        cancelText="取消"
      >
        <Form form={nodeForm} layout="vertical">
          <Form.Item 
            name="nodeName" 
            label="节点名称" 
            initialValue={selectedNode?.data?.name}
          >
            <Input placeholder="请输入节点名称" />
          </Form.Item>
          
          {selectedNode?.data?.type === 'llm_chat' && (
            <>
              <Form.Item 
                name="model" 
                label="模型" 
                initialValue="gpt-3.5-turbo"
                rules={[{ required: true, message: '请选择模型' }]}
              >
                <Select>
                  <Select.Option value="gpt-3.5-turbo">GPT-3.5 Turbo</Select.Option>
                  <Select.Option value="gpt-4">GPT-4</Select.Option>
                  <Select.Option value="claude-3-haiku">Claude 3 Haiku</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item 
                name="prompt" 
                label="提示词" 
                initialValue=""
                rules={[{ required: true, message: '请输入提示词' }]}
              >
                <Input.TextArea rows={4} placeholder="请输入提示词，支持 {{变量名}} 格式" />
              </Form.Item>
              <Form.Item 
                name="temperature" 
                label="温度" 
                initialValue={0.7}
                rules={[{ required: true, message: '请输入温度值' }]}
              >
                <Input type="number" min={0} max={1} step={0.1} />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data?.type === 'knowledge_retrieval' && (
            <>
              <Form.Item 
                name="knowledgeBaseId" 
                label="知识库" 
                initialValue=""
                rules={[{ required: true, message: '请选择知识库' }]}
              >
                <Select placeholder="请选择知识库">
                  <Select.Option value="1">默认知识库</Select.Option>
                  <Select.Option value="2">技术文档</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item 
                name="queryTemplate" 
                label="查询模板" 
                initialValue=""
                rules={[{ required: true, message: '请输入查询模板' }]}
              >
                <Input.TextArea rows={3} placeholder="请输入查询模板，支持 {{变量名}} 格式" />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data?.type === 'http_request' && (
            <>
              <Form.Item 
                name="url" 
                label="请求URL" 
                initialValue=""
                rules={[{ required: true, message: '请输入请求URL' }]}
              >
                <Input placeholder="请输入完整的URL" />
              </Form.Item>
              <Form.Item 
                name="method" 
                label="请求方法" 
                initialValue="GET"
                rules={[{ required: true, message: '请选择请求方法' }]}
              >
                <Select>
                  <Select.Option value="GET">GET</Select.Option>
                  <Select.Option value="POST">POST</Select.Option>
                  <Select.Option value="PUT">PUT</Select.Option>
                  <Select.Option value="DELETE">DELETE</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item name="headers" label="请求头" initialValue="">
                <Input.TextArea rows={3} placeholder='{"Content-Type": "application/json"}' />
              </Form.Item>
              <Form.Item name="body" label="请求体" initialValue="">
                <Input.TextArea rows={4} placeholder="请输入请求体内容" />
              </Form.Item>
            </>
          )}
          
          {(selectedNode?.data?.type === 'start' || selectedNode?.data?.type === 'end') && (
            <Form.Item name="description" label="描述" initialValue="">
              <Input.TextArea rows={3} placeholder="请输入节点描述" />
            </Form.Item>
          )}
          
          {selectedNode?.data?.type === 'condition' && (
            <>
              <Form.Item 
                name="condition" 
                label="条件表达式" 
                initialValue=""
                rules={[{ required: true, message: '请输入条件表达式' }]}
              >
                <Input placeholder="请输入条件表达式，如: {{score}} > 80" />
              </Form.Item>
              <Form.Item 
                name="trueBranch" 
                label="条件为真时的分支标签" 
                initialValue=""
                rules={[{ required: true, message: '请输入分支标签' }]}
              >
                <Input placeholder="如: 通过" />
              </Form.Item>
              <Form.Item 
                name="falseBranch" 
                label="条件为假时的分支标签" 
                initialValue=""
                rules={[{ required: true, message: '请输入分支标签' }]}
              >
                <Input placeholder="如: 不通过" />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data?.type === 'variable_set' && (
            <>
              <Form.Item 
                name="variableName" 
                label="变量名" 
                initialValue=""
                rules={[{ required: true, message: '请输入变量名' }]}
              >
                <Input placeholder="请输入变量名" />
              </Form.Item>
              <Form.Item 
                name="variableValue" 
                label="变量值" 
                initialValue=""
                rules={[{ required: true, message: '请输入变量值' }]}
              >
                <Input placeholder="请输入变量值，支持 {{变量名}} 格式" />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data?.type === 'code_execution' && (
            <>
              <Form.Item 
                name="language" 
                label="编程语言" 
                initialValue="python"
                rules={[{ required: true, message: '请选择编程语言' }]}
              >
                <Select>
                  <Select.Option value="python">Python</Select.Option>
                  <Select.Option value="javascript">JavaScript</Select.Option>
                  <Select.Option value="java">Java</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item 
                name="code" 
                label="代码" 
                initialValue=""
                rules={[{ required: true, message: '请输入代码' }]}
              >
                <Input.TextArea rows={6} placeholder="请输入要执行的代码" />
              </Form.Item>
            </>
          )}
          
          {selectedNode?.data?.type === 'user_input' && (
            <>
              <Form.Item 
                name="inputType" 
                label="输入类型" 
                initialValue="text"
                rules={[{ required: true, message: '请选择输入类型' }]}
              >
                <Select>
                  <Select.Option value="text">文本输入</Select.Option>
                  <Select.Option value="number">数字输入</Select.Option>
                  <Select.Option value="select">选择框</Select.Option>
                  <Select.Option value="checkbox">复选框</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item 
                name="inputLabel" 
                label="输入标签" 
                initialValue=""
                rules={[{ required: true, message: '请输入输入标签' }]}
              >
                <Input placeholder="请输入输入标签" />
              </Form.Item>
              <Form.Item name="inputDescription" label="输入描述" initialValue="">
                <Input.TextArea rows={2} placeholder="请输入输入描述" />
              </Form.Item>
              <Form.Item name="inputOptions" label="选项（选择框用逗号分隔）" initialValue="">
                <Input placeholder="选项1,选项2,选项3" />
              </Form.Item>
            </>
          )}
        </Form>
      </Modal>
    </div>
  )
}

export default WorkflowDesigner