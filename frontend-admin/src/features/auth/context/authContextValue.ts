import { createContext } from 'react';
import { Role } from '../types/auth';

export interface AuthState {
  isAuthenticated: boolean;
  isInitialized: boolean;
  role: Role | null;
}

export interface AuthContextType extends AuthState {
  setAuthenticated: (value: boolean) => void;
  setInitialized: (value: boolean) => void;
  setRole: (role: Role) => void;
  logout: () => void;
  login: (token: string, role: Role) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
