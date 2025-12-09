import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { setAuthenticated, setInitialized, setRole } from '../../../stores/authSlice';
import { useAppDispatch, useAppSelector } from '../../../stores/hooks';
import { useTokenValidation } from '../hooks/useTokenValidation';
import { getRoleFromToken, getToken, removeToken } from '../utils/tokenUtils';

interface AuthInitProps {
  children: React.ReactNode;
}

export const AuthInit = ({ children }: AuthInitProps) => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const isInitialized = useAppSelector((state) => state.auth.isInitialized);
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
        dispatch(setAuthenticated(false));
        dispatch(setInitialized(true));
        return;
      }

      // Có token → validate với server
      try {
        const isValid = await validateToken(token);

        if (isValid) {
          // Lấy role từ token
          const role = getRoleFromToken(token);
          dispatch(setAuthenticated(true));
          if (role) {
            dispatch(setRole(role));
          }
        } else {
          // Token không hợp lệ → xóa và redirect
          removeToken();
          dispatch(setAuthenticated(false));
          navigate('/login', { replace: true });
        }
      } catch (error) {
        // Lỗi khi validate → xóa token và redirect
        console.error('Auth init error:', error);
        removeToken();
        dispatch(setAuthenticated(false));
        navigate('/login', { replace: true });
      } finally {
        dispatch(setInitialized(true));
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
