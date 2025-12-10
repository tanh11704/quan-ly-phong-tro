import { ConfigProvider } from 'antd';
import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import { themeConfig } from './config/themeConfig';
import { AuthInit } from './features/auth/components/AuthInit';
import { ProtectedRoute } from './features/auth/components/ProtectedRoute';
import { BentoLayout } from './layouts';

const Login = lazy(() => import('./features/auth/pages/Login'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const SentryLogs = lazy(() => import('./features/sentry/pages/SentryLogs'));

const App = () => {
  return (
    <ConfigProvider theme={themeConfig}>
      <AuthInit>
        <Suspense
          fallback={<div className="flex items-center justify-center min-h-screen">Loading...</div>}
        >
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/sentry-logs"
              element={
                <ProtectedRoute>
                  <BentoLayout>
                    <SentryLogs />
                  </BentoLayout>
                </ProtectedRoute>
              }
            />
            <Route path="/" element={<Login />} />
          </Routes>
        </Suspense>
      </AuthInit>
    </ConfigProvider>
  );
};

export default App;
