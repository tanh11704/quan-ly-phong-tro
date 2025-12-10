import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { Button, Result, Spin } from 'antd';
import type { AxiosError } from 'axios';
import { motion } from 'motion/react';
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useActivateAccount } from '../api/authApi';

const Activate = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const { data, isLoading, isError, error } = useActivateAccount(token);

  useEffect(() => {
    if (data) {
      // Tự động redirect sau 3 giây
      const timer = setTimeout(() => {
        navigate('/login', { replace: true });
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [data, navigate]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-blue-50 via-white to-purple-50">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center"
        >
          <Spin size="large" />
          <p className="mt-4 text-gray-600">Đang kích hoạt tài khoản...</p>
        </motion.div>
      </div>
    );
  }

  if (isError || !token) {
    const errorMessage =
      error && 'response' in error
        ? (error as AxiosError<{ message?: string }>).response?.data?.message
        : undefined;

    return (
      <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-blue-50 via-white to-purple-50 p-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md"
        >
          <div className="bg-white rounded-2xl shadow-2xl p-8 border border-gray-100">
            <Result
              status="error"
              icon={<CloseCircleOutlined className="text-red-500" />}
              title="Kích hoạt thất bại"
              subTitle={
                errorMessage ||
                'Token không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại link kích hoạt.'
              }
              extra={[
                <Button type="primary" key="login" onClick={() => navigate('/login')}>
                  Đăng nhập
                </Button>,
                <Button key="register" onClick={() => navigate('/register')}>
                  Đăng ký lại
                </Button>,
              ]}
            />
          </div>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-blue-50 via-white to-purple-50 p-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        <div className="bg-white rounded-2xl shadow-2xl p-8 border border-gray-100">
          <Result
            status="success"
            icon={<CheckCircleOutlined className="text-green-500" />}
            title="Kích hoạt thành công!"
            subTitle={
              data?.message ||
              'Tài khoản của bạn đã được kích hoạt. Bạn có thể đăng nhập ngay bây giờ.'
            }
            extra={[
              <Button
                type="primary"
                key="login"
                onClick={() => navigate('/login')}
                className="bg-linear-to-r from-blue-500 to-purple-600 border-0"
              >
                Đăng nhập ngay
              </Button>,
            ]}
          />
        </div>
      </motion.div>
    </div>
  );
};

export default Activate;
