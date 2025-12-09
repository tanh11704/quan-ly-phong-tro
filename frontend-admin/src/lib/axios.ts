import axios from 'axios';
import { removeToken } from '../features/auth/utils/tokenUtils';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Nếu API trả về 401 → Token không hợp lệ hoặc đã hết hạn
    if (error.response?.status === 401) {
      // Xóa token và redirect về Login
      removeToken();
      // Dùng window.location để đảm bảo redirect ngay cả khi đang ở bất kỳ route nào
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

export default axiosInstance;
