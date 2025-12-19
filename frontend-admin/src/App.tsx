import { ConfigProvider } from 'antd';
import viVN from 'antd/locale/vi_VN';
import dayjs from 'dayjs';
import 'dayjs/locale/vi';
import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import { themeConfig } from './config/themeConfig';
import { AuthInit } from './features/auth/components/AuthInit';
import { ProtectedRoute } from './features/auth/components/ProtectedRoute';
import { BentoLayout } from './layouts';

// Cấu hình dayjs locale tiếng Việt
dayjs.locale('vi');

const Login = lazy(() => import('./features/auth/pages/Login'));
const Register = lazy(() => import('./features/auth/pages/Register'));
const Activate = lazy(() => import('./features/auth/pages/Activate'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Buildings = lazy(() => import('./features/buildings/pages/Buildings'));
const Rooms = lazy(() => import('./features/rooms/pages/Rooms'));
const Invoices = lazy(() => import('./features/invoices/pages/Invoices'));

const App = () => {
  return (
    <ConfigProvider theme={themeConfig} locale={viVN}>
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
            <Route
              path="/invoices"
              element={
                <ProtectedRoute>
                  <BentoLayout>
                    <Invoices />
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
