import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds
});

// Request Interceptor: Tự động gắn Token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken'); // Hoặc lấy từ Store
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// Response Interceptor: Xử lý lỗi tập trung & Refresh Token
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Logic Refresh Token sẽ nằm ở đây
    // Nếu lỗi 401 -> Gọi refresh -> Retry request gốc
    return Promise.reject(error);
  },
);

export default axiosInstance;
