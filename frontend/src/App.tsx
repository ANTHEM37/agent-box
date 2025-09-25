import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './stores/auth'
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
          <Route path="knowledge/:knowledgeBaseId/documents" element={<DocumentManagement />} />
          <Route path="search" element={<SearchInterface />} />
          <Route path="agent" element={<AgentDashboard />} />
          <Route path="agent/definitions" element={<AgentDefinitionList />} />
          <Route path="agent/instances" element={<AgentInstanceList />} />
          <Route path="agent/tasks" element={<TaskList />} />
          <Route path="agent/messages" element={<MessageList />} />
        </Route>
        
        {/* 404 重定向 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  )
}

export default App