import { useCallback, useState, type ReactNode } from 'react';
import { Role } from '../types/auth';
import { removeToken, setToken } from '../utils/tokenUtils';
import { AuthContext, type AuthState } from './authContextValue';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: false,
    isInitialized: false,
    role: null,
  });

  const setAuthenticated = useCallback((value: boolean) => {
    setState((prev) => ({ ...prev, isAuthenticated: value }));
  }, []);

  const setInitialized = useCallback((value: boolean) => {
    setState((prev) => ({ ...prev, isInitialized: value }));
  }, []);

  const setRole = useCallback((role: Role) => {
    setState((prev) => ({ ...prev, role }));
  }, []);

  const logout = useCallback(() => {
    removeToken();
    setState({
      isAuthenticated: false,
      isInitialized: true,
      role: null,
    });
  }, []);

  const login = useCallback((token: string, role: Role) => {
    setToken(token);
    setState({
      isAuthenticated: true,
      isInitialized: true,
      role,
    });
  }, []);

  return (
    <AuthContext.Provider
      value={{
        ...state,
        setAuthenticated,
        setInitialized,
        setRole,
        logout,
        login,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
