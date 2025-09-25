import React, { useEffect } from 'react'
import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { agentApi, AgentDefinition, AgentType, CreateAgentDefinitionRequest } from '@/services/api/agent'

const { TextArea } = Input
const { Option } = Select

interface CreateAgentDefinitionModalProps {
  visible: boolean
  editingDefinition?: AgentDefinition | null
  onClose: () => void
  onSuccess: () => void
}

const CreateAgentDefinitionModal: React.FC<CreateAgentDefinitionModalProps> = ({
  visible,
  editingDefinition,
  onClose,
  onSuccess
}) => {
  const [form] = Form.useForm()

  useEffect(() => {
    if (visible && editingDefinition) {
      form.setFieldsValue({
        ...editingDefinition,
        config: JSON.stringify(JSON.parse(editingDefinition.config || '{}'), null, 2)
      })
    } else if (visible) {
      form.resetFields()
      form.setFieldsValue({
        enabled: true,
        type: AgentType.TEXT_PROCESSING
      })
    }
  }, [visible, editingDefinition, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      // 验证配置是否为有效的JSON
      try {
        JSON.parse(values.config)
      } catch (error) {
        message.error('配置必须是有效的JSON格式')
        return
      }

      if (editingDefinition) {
        await agentApi.updateAgentDefinition(editingDefinition.id, values)
        message.success('更新成功')
      } else {
        const request: CreateAgentDefinitionRequest = {
          name: values.name,
          description: values.description,
          type: values.type,
          config: values.config
        }
        await agentApi.createAgentDefinition(request)
        message.success('创建成功')
      }
      
      onSuccess()
    } catch (error) {
      message.error(editingDefinition ? '更新失败' : '创建失败')
    }
  }

  const agentTypeOptions = [
    { value: AgentType.TEXT_PROCESSING, label: '文本处理' },
    { value: AgentType.DATA_ANALYSIS, label: '数据分析' },
    { value: AgentType.DECISION_MAKING, label: '决策制定' },
    { value: AgentType.COLLABORATIVE, label: '协作智能体' }
  ]

  return (
    <Modal
      title={editingDefinition ? '编辑智能体定义' : '新建智能体定义'}
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      width={600}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        requiredMark={false}
      >
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: '请输入名称' }]}
        >
          <Input placeholder="请输入智能体名称" />
        </Form.Item>

        <Form.Item
          name="description"
          label="描述"
          rules={[{ required: true, message: '请输入描述' }]}
        >
          <TextArea 
            placeholder="请输入智能体描述" 
            rows={3}
          />
        </Form.Item>

        <Form.Item
          name="type"
          label="类型"
          rules={[{ required: true, message: '请选择类型' }]}
        >
          <Select placeholder="请选择智能体类型">
            {agentTypeOptions.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="config"
          label="配置"
          rules={[{ required: true, message: '请输入配置' }]}
          help="请输入有效的JSON格式配置"
        >
          <TextArea 
            placeholder='{"model": "gpt-4", "temperature": 0.7}'
            rows={6}
          />
        </Form.Item>

        <Form.Item
          name="enabled"
          label="启用状态"
          valuePropName="checked"
        >
          <Switch checkedChildren="启用" unCheckedChildren="禁用" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default CreateAgentDefinitionModal