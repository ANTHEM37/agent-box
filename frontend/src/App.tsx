import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './stores/auth'
import { useAuthInit } from './hooks/useAuthInit'
import MainLayout from './components/common/Layout/MainLayout'
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import Dashboard from './pages/dashboard/Dashboard'
import KnowledgeBaseList from './pages/knowledge/KnowledgeBaseList'
import DocumentManagement from './pages/knowledge/DocumentManagement'
import SearchInterface from './pages/knowledge/SearchInterface'
import AgentDashboard from './pages/agent/AgentDashboard'
import AgentDefinitionList from './pages/agent/AgentDefinitionList'
import AgentInstanceList from './pages/agent/AgentInstanceList'
import TaskList from './pages/agent/TaskList'
import MessageList from './pages/agent/MessageList'

function App() {
  const { isAuthenticated } = useAuthStore()
  const { isInitialized } = useAuthInit()

  // 等待认证状态初始化完成
  if (!isInitialized) {
    return <div>Loading...</div>
  }

  return (
    <Router>
      <Routes>
        {/* 公开路由 */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* 受保护的路由 */}
        <Route path="/" element={
          isAuthenticated ? <MainLayout /> : <Navigate to="/login" replace />
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="knowledge" element={<KnowledgeBaseList />} />
          <Route path="knowledge/:id/documents" element={<DocumentManagement />} />
          <Route path="search" element={<SearchInterface />} />
          <Route path="agents" element={<AgentDashboard />} />
          <Route path="agents/definitions" element={<AgentDefinitionList />} />
          <Route path="agents/instances" element={<AgentInstanceList />} />
          <Route path="agents/tasks" element={<TaskList />} />
          <Route path="agents/messages" element={<MessageList />} />
          
          {/* MCP 路由 */}
          <Route path="mcp" element={<Navigate to="/mcp/services" replace />} />
          
          {/* 工作流路由 */}
          <Route path="workflows" element={<Navigate to="/workflows/list" replace />} />
        </Route>
      </Routes>
    </Router>
  )
}

export default App