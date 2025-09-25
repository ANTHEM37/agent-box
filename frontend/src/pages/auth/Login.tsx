import { useState } from 'react'
import { Button, Card, Form, Input, Typography, message } from 'antd'
import { authApi } from '../../services/api/auth'
import { useAuthStore } from '../../stores/authStore'
import { useNavigate, Link } from 'react-router-dom'

export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const setToken = useAuthStore((s) => s.setToken)
  const navigate = useNavigate()

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const res = await authApi.login(values)
      setToken(res.token)
      message.success('登录成功')
      navigate('/')
    } catch (e: any) {
      message.error(e?.response?.data?.message || '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
      <Card style={{ width: 360 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>
          登录
        </Typography.Title>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="用户名" autoFocus />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="密码" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>
            登录
          </Button>
        </Form>
        <div style={{ marginTop: 12, textAlign: 'center' }}>
          还没有账号？<Link to="/register">去注册</Link>
        </div>
      </Card>
    </div>
  )
}


