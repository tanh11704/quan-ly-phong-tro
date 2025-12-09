import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Form, Input, message } from 'antd';
import { motion } from 'motion/react';
import { useNavigate } from 'react-router-dom';
import { setAuthenticated, setRole } from '../../../stores/authSlice';
import { useAppDispatch } from '../../../stores/hooks';
import { useLoginMutation } from '../api/authApi';
import { LoginSchema, type LoginFormData } from '../types/auth';
import { setToken } from '../utils/tokenUtils';

const Login = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [login, { isLoading }] = useLoginMutation();

  const handleSubmit = async (values: LoginFormData) => {
    try {
      // Validate với Zod
      const validatedData = LoginSchema.parse(values);
      const response = await login(validatedData).unwrap();

      // Lưu token từ response.result.token
      const token = response.result.token;
      const role = response.result.role;
      setToken(token);

      // Cập nhật state authentication và role
      dispatch(setAuthenticated(true));
      dispatch(setRole(role));

      message.success(response.message || 'Đăng nhập thành công!');
      // Redirect sẽ được xử lý ở đây
      navigate('/dashboard', { replace: true });
    } catch (error) {
      message.error('Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.');
      console.error('Login error:', error);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-purple-50 p-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-md"
      >
        <motion.div
          initial={{ scale: 0.95 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.3, delay: 0.2 }}
          className="bg-white rounded-2xl shadow-2xl p-8 border border-gray-100"
        >
          {/* Header */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            className="text-center mb-8"
          >
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: 'spring', delay: 0.4 }}
              className="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg"
            >
              <LockOutlined className="text-2xl text-white" />
            </motion.div>
            <h1 className="text-3xl font-bold text-gray-800 mb-2">Chào mừng trở lại</h1>
            <p className="text-gray-500">Đăng nhập để tiếp tục quản lý hệ thống</p>
          </motion.div>

          {/* Form */}
          <Form
            form={form}
            name="login"
            onFinish={handleSubmit}
            layout="vertical"
            size="large"
            className="space-y-4"
          >
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.5 }}
            >
              <Form.Item
                name="username"
                rules={[{ required: true, message: 'Vui lòng nhập tên đăng nhập!' }]}
              >
                <Input
                  prefix={<UserOutlined className="text-gray-400" />}
                  placeholder="Tên đăng nhập"
                  className="rounded-lg h-12"
                />
              </Form.Item>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.6 }}
            >
              <Form.Item
                name="password"
                rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
              >
                <Input.Password
                  prefix={<LockOutlined className="text-gray-400" />}
                  placeholder="Mật khẩu"
                  className="rounded-lg h-12"
                />
              </Form.Item>
            </motion.div>

            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.7 }}
              className="flex justify-end mb-4"
            >
              <a
                href="#forgot"
                className="text-sm text-blue-600 hover:text-blue-700 transition-colors"
              >
                Quên mật khẩu?
              </a>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.8 }}
            >
              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={isLoading}
                  block
                  className="h-12 rounded-lg text-base font-semibold bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 border-0 shadow-lg hover:shadow-xl transition-all duration-300"
                >
                  Đăng nhập
                </Button>
              </Form.Item>
            </motion.div>
          </Form>

          {/* Footer */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.9 }}
            className="mt-6 text-center text-sm text-gray-500"
          >
            Chưa có tài khoản?{' '}
            <a
              href="#register"
              className="text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              Đăng ký ngay
            </a>
          </motion.div>
        </motion.div>
      </motion.div>
    </div>
  );
};

export default Login;
