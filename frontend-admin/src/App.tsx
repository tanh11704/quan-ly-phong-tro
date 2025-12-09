import { ConfigProvider } from 'antd';
import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import { themeConfig } from './config/themeConfig';

const Login = lazy(() => import('./features/auth/pages/Login'));
const Dashboard = lazy(() => import('./pages/Dashboard'));

const App = () => {
  return (
    <ConfigProvider theme={themeConfig}>
      <Suspense
        fallback={<div className="flex items-center justify-center min-h-screen">Loading...</div>}
      >
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/" element={<Login />} />
        </Routes>
      </Suspense>
    </ConfigProvider>
  );
};

export default App;
