import { Modal, Form, Input } from 'antd'
import { useEffect } from 'react'

export interface KnowledgeBaseModalProps {
  open: boolean
  initialValues?: { name?: string; description?: string }
  onOk: (values: { name: string; description?: string }) => Promise<void> | void
  onCancel: () => void
}

export default function KnowledgeBaseModal({ open, initialValues, onOk, onCancel }: KnowledgeBaseModalProps) {
  const [form] = Form.useForm()

  useEffect(() => {
    if (open) {
      form.setFieldsValue(initialValues || { name: '', description: '' })
    } else {
      form.resetFields()
    }
  }, [open])

  return (
    <Modal
      title={initialValues ? '编辑知识库' : '新建知识库'}
      open={open}
      onOk={() => form.submit()}
      onCancel={onCancel}
      destroyOnClose
    >
      <Form form={form} layout="vertical" onFinish={onOk}>
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
          <Input placeholder="名称" maxLength={100} />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="描述" rows={3} maxLength={500} />
        </Form.Item>
      </Form>
    </Modal>
  )
}


