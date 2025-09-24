import React from 'react'
import { Form, Input, Button, Card, Typography, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi, LoginRequest } from '@/services/api/auth'
import { useAuthStore } from '@/stores/auth'

const { Title, Text } = Typography

const Login: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  
  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (response) => {
      login(response.data.user, response.data.token)
      message.success('登录成功')
      navigate('/dashboard')
    },
    onError: (error: any) => {
      message.error(error.message || '登录失败')
    },
  })

  const onFinish = (values: LoginRequest) => {
    loginMutation.mutate(values)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <div className="text-center mb-8">
          <Title level={2}>AI Agent Platform</Title>
          <Text type="secondary">登录您的账户</Text>
        </div>
        
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名!' }]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="用户名" 
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码!' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              className="w-full"
              loading={loginMutation.isPending}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
        
        <div className="text-center">
          <Text type="secondary">
            还没有账户？ <Link to="/register">立即注册</Link>
          </Text>
        </div>
      </Card>
    </div>
  )
}

export default Login