import { useState } from 'react'
import { Button, Card, Form, Input, Typography, message } from 'antd'
import { authApi } from '../../services/api/auth'
import { useNavigate, Link } from 'react-router-dom'

export default function RegisterPage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const onFinish = async (values: { username: string; email: string; password: string }) => {
    setLoading(true)
    try {
      await authApi.register(values)
      message.success('注册成功，请登录')
      navigate('/login')
    } catch (e: any) {
      message.error(e?.response?.data?.message || '注册失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
      <Card style={{ width: 380 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>
          注册账号
        </Typography.Title>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="用户名" autoFocus />
          </Form.Item>
          <Form.Item name="email" label="邮箱" rules={[{ required: true, type: 'email', message: '请输入有效邮箱' }]}>
            <Input placeholder="邮箱" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="密码" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>
            注册
          </Button>
        </Form>
        <div style={{ marginTop: 12, textAlign: 'center' }}>
          已有账号？<Link to="/login">去登录</Link>
        </div>
      </Card>
    </div>
  )
}


