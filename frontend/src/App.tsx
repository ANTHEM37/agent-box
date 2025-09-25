import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom'
import { ReactFlowProvider } from 'reactflow'
import AppLayout from './components/layout/AppLayout'
import Dashboard from './pages/Dashboard'
import KnowledgeBaseList from './pages/knowledge/KnowledgeBaseList'
import WorkflowList from './pages/workflow/WorkflowList'
import WorkflowExecutionList from './pages/workflow/WorkflowExecutionList'
import McpServiceList from './pages/mcp/McpServiceList'
import LoginPage from './pages/auth/Login'
import RegisterPage from './pages/auth/Register'
// 添加工作流设计器导入
import WorkflowDesigner from './pages/workflow/WorkflowDesigner'
import { useAuthStore } from './stores/authStore'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token)
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return children
}

// 创建一个包装组件来包含 ReactFlowProvider
function ReactFlowWrapper({ children }: { children: React.ReactNode }) {
  return (
    <ReactFlowProvider>
      {children}
    </ReactFlowProvider>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <AppLayout>
                <Dashboard />
              </AppLayout>
            </PrivateRoute>
          }
        />
        <Route
          path="/knowledge"
          element={
            <PrivateRoute>
              <AppLayout>
                <KnowledgeBaseList />
              </AppLayout>
            </PrivateRoute>
          }
        />
        <Route
          path="/workflows"
          element={
            <PrivateRoute>
              <AppLayout>
                <WorkflowList />
              </AppLayout>
            </PrivateRoute>
          }
        />
        {/* 添加工作流设计器路由 */}
        <Route
          path="/workflow/create"
          element={
            <PrivateRoute>
              <AppLayout>
                <ReactFlowWrapper>
                  <WorkflowDesigner />
                </ReactFlowWrapper>
              </AppLayout>
            </PrivateRoute>
          }
        />
        <Route
          path="/workflow/edit/:id"
          element={
            <PrivateRoute>
              <AppLayout>
                <ReactFlowWrapper>
                  <WorkflowDesigner />
                </ReactFlowWrapper>
              </AppLayout>
            </PrivateRoute>
          }
        />
        <Route
          path="/workflow/executions"
          element={
            <PrivateRoute>
              <AppLayout>
                <WorkflowExecutionList />
              </AppLayout>
            </PrivateRoute>
          }
        />
        <Route
          path="/mcp"
          element={
            <PrivateRoute>
              <AppLayout>
                <McpServiceList />
              </AppLayout>
            </PrivateRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}