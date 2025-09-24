import React from 'react'
import { Form, Input, Button, Card, Typography, message } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi, RegisterRequest } from '@/services/api/auth'
import { useAuthStore } from '@/stores/auth'

const { Title, Text } = Typography

const Register: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  
  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (response) => {
      login(response.data.user, response.data.token)
      message.success('注册成功')
      navigate('/dashboard')
    },
    onError: (error: any) => {
      message.error(error.message || '注册失败')
    },
  })

  const onFinish = (values: RegisterRequest) => {
    registerMutation.mutate(values)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <div className="text-center mb-8">
          <Title level={2}>AI Agent Platform</Title>
          <Text type="secondary">创建您的账户</Text>
        </div>
        
        <Form
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名!' },
              { min: 3, message: '用户名至少3个字符!' },
            ]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="用户名" 
            />
          </Form.Item>

          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱!' },
              { type: 'email', message: '请输入有效的邮箱地址!' },
            ]}
          >
            <Input 
              prefix={<MailOutlined />} 
              placeholder="邮箱" 
            />
          </Form.Item>

          <Form.Item
            name="fullName"
          >
            <Input 
              placeholder="姓名（可选）" 
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码!' },
              { min: 6, message: '密码至少6个字符!' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码!' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('两次输入的密码不一致!'))
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="确认密码"
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              className="w-full"
              loading={registerMutation.isPending}
            >
              注册
            </Button>
          </Form.Item>
        </Form>
        
        <div className="text-center">
          <Text type="secondary">
            已有账户？ <Link to="/login">立即登录</Link>
          </Text>
        </div>
      </Card>
    </div>
  )
}

export default Register