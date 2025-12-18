import { ConfigProvider } from 'antd';
import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import { themeConfig } from './config/themeConfig';
import { AuthInit } from './features/auth/components/AuthInit';
import { ProtectedRoute } from './features/auth/components/ProtectedRoute';
import { BentoLayout } from './layouts';

const Login = lazy(() => import('./features/auth/pages/Login'));
const Register = lazy(() => import('./features/auth/pages/Register'));
const Activate = lazy(() => import('./features/auth/pages/Activate'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Buildings = lazy(() => import('./features/buildings/pages/Buildings'));
const Rooms = lazy(() => import('./features/rooms/pages/Rooms'));

const App = () => {
  return (
    <ConfigProvider theme={themeConfig}>
      <AuthInit>
        <Suspense
          fallback={<div className="flex items-center justify-center min-h-screen">Loading...</div>}
        >
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/activate" element={<Activate />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/buildings"
              element={
                <ProtectedRoute>
                  <BentoLayout>
                    <Buildings />
                  </BentoLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/rooms"
              element={
                <ProtectedRoute>
                  <BentoLayout>
                    <Rooms />
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
