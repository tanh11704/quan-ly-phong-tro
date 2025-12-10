import { Navigate } from 'react-router-dom';
import { useAuth } from '../context';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isAuthenticated, isInitialized } = useAuth();

  // Chờ init xong mới check
  if (!isInitialized) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  // Chỉ check state ở client, không gọi API
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};
