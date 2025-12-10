import { Role } from '../types/auth';

export const getToken = (): string | null => {
  return localStorage.getItem('accessToken');
};

export const setToken = (token: string): void => {
  localStorage.setItem('accessToken', token);
};

export const removeToken = (): void => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('userRole');
};

export const setRole = (role: Role): void => {
  localStorage.setItem('userRole', role);
};

export const getRole = (): Role | null => {
  const role = localStorage.getItem('userRole');
  if (role && Object.values(Role).includes(role as Role)) {
    return role as Role;
  }
  return null;
};

export const hasToken = (): boolean => {
  return !!getToken();
};

export const decodeToken = (token: string): Record<string, unknown> | null => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    // Decode base64 payload (phần thứ 2)
    const payload = parts[1];
    // Base64 URL decode
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch (error) {
    console.error('Error decoding token:', error);
    return null;
  }
};

/**
 * Lấy role từ JWT token
 */
export const getRoleFromToken = (token: string): Role | null => {
  const decoded = decodeToken(token);
  if (!decoded || !decoded.role) {
    return null;
  }

  // Kiểm tra role có hợp lệ không
  const role = decoded.role as string;
  if (Object.values(Role).includes(role as Role)) {
    return role as Role;
  }

  return null;
};
