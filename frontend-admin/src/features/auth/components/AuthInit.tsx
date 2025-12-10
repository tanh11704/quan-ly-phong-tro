import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context';
import { useTokenValidation } from '../hooks/useTokenValidation';
import { getRoleFromToken, getToken, removeToken } from '../utils/tokenUtils';

interface AuthInitProps {
  children: React.ReactNode;
}

export const AuthInit = ({ children }: AuthInitProps) => {
  const navigate = useNavigate();
  const { isInitialized, setAuthenticated, setInitialized, setRole } = useAuth();
  const { validateToken } = useTokenValidation();

  useEffect(() => {
    const initAuth = async () => {
      // Chỉ init 1 lần duy nhất
      if (isInitialized) {
        return;
      }

      const token = getToken();

      // Nếu không có token → chưa đăng nhập
      if (!token) {
        setAuthenticated(false);
        setInitialized(true);
        return;
      }

      // Có token → validate với server
      try {
        const isValid = await validateToken(token);

        if (isValid) {
          // Lấy role từ token
          const role = getRoleFromToken(token);
          setAuthenticated(true);
          if (role) {
            setRole(role);
          }
        } else {
          // Token không hợp lệ → xóa và redirect
          removeToken();
          setAuthenticated(false);
          navigate('/login', { replace: true });
        }
      } catch (error) {
        // Lỗi khi validate → xóa token và redirect
        console.error('Auth init error:', error);
        removeToken();
        setAuthenticated(false);
        navigate('/login', { replace: true });
      } finally {
        setInitialized(true);
      }
    };

    initAuth();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Chờ init xong mới render children
  if (!isInitialized) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  return <>{children}</>;
};
