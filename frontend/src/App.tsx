import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Layout } from 'antd';
import Header from '@components/layout/Header';
import Sidebar from '@components/layout/Sidebar';
import AuthLayout from '@components/layout/AuthLayout';
import Dashboard from '@pages/Dashboard';
import LoginPage from '@pages/LoginPage';
import Register from '@pages/auth/Register';
import KnowledgeBaseList from '@pages/knowledge/KnowledgeBaseList';
import WorkflowList from '@pages/workflow/WorkflowList';
import McpServiceList from '@pages/mcp/McpServiceList';
import AgentList from '@pages/agent/AgentList';
import './App.css';

const { Content, Footer } = Layout;

const MainLayout: React.FC = () => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sidebar />
      <Layout>
        <Header />
        <Content style={{ margin: '24px 16px 0' }}>
          <div style={{ padding: 24, minHeight: 360 }}>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/knowledge" element={<KnowledgeBaseList />} />
              <Route path="/workflows" element={<WorkflowList />} />
              <Route path="/mcp" element={<McpServiceList />} />
              <Route path="/agents" element={<AgentList />} />
            </Routes>
          </div>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          AI Agent Platform ©{new Date().getFullYear()} Created by Agent Platform Team
        </Footer>
      </Layout>
    </Layout>
  );
};

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={
        <AuthLayout>
          <LoginPage />
        </AuthLayout>
      } />
      <Route path="/register" element={
        <AuthLayout>
          <Register />
        </AuthLayout>
      } />
      <Route path="/*" element={<MainLayout />} />
    </Routes>
  );
};

export default App;