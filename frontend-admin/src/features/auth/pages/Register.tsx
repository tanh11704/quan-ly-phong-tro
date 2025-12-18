import { IdcardOutlined, LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Form, Input, message } from 'antd';
import { motion } from 'motion/react';
import { Link, useNavigate } from 'react-router-dom';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useRegisterMutation } from '../api/authApi';
import { RegistrationRequestSchema, type RegistrationRequest } from '../types/auth';

const Register = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { mutateAsync: register, isPending: isLoading } = useRegisterMutation();

  const handleSubmit = async (values: RegistrationRequest) => {
    try {
      // Validate với Zod
      const validatedData = RegistrationRequestSchema.parse(values);
      const response = await register(validatedData);

      message.success(
        response.message || 'Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.',
      );
      // Redirect về login sau 2 giây
      setTimeout(() => {
        navigate('/login', { replace: true });
      }, 2000);
    } catch (error) {
      message.error(getErrorMessage(error, 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.'));
      console.error('Register error:', error);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
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
              className="w-16 h-16 bg-blue-500 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg"
            >
              <IdcardOutlined className="text-2xl text-white" />
            </motion.div>
            <h1 className="text-3xl font-bold text-gray-800 mb-2">Đăng ký tài khoản</h1>
            <p className="text-gray-500">Tạo tài khoản Manager mới</p>
          </motion.div>

          {/* Form */}
          <Form
            form={form}
            name="register"
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
                name="fullName"
                label="Họ và tên"
                rules={[
                  { required: true, message: 'Vui lòng nhập họ và tên!' },
                  { max: 100, message: 'Họ và tên không được quá 100 ký tự' },
                ]}
              >
                <Input
                  prefix={<UserOutlined className="text-gray-400" />}
                  placeholder="Nguyễn Văn A"
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
                name="username"
                label="Tên đăng nhập"
                rules={[
                  { required: true, message: 'Vui lòng nhập tên đăng nhập!' },
                  { min: 3, message: 'Tên đăng nhập phải có ít nhất 3 ký tự' },
                  { max: 50, message: 'Tên đăng nhập không được quá 50 ký tự' },
                ]}
              >
                <Input
                  prefix={<UserOutlined className="text-gray-400" />}
                  placeholder="manager01"
                  className="rounded-lg h-12"
                />
              </Form.Item>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.7 }}
            >
              <Form.Item
                name="email"
                label="Email"
                rules={[
                  { required: true, message: 'Vui lòng nhập email!' },
                  { type: 'email', message: 'Email không hợp lệ' },
                ]}
              >
                <Input
                  prefix={<MailOutlined className="text-gray-400" />}
                  placeholder="manager@example.com"
                  className="rounded-lg h-12"
                />
              </Form.Item>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.8 }}
            >
              <Form.Item
                name="password"
                label="Mật khẩu"
                rules={[
                  { required: true, message: 'Vui lòng nhập mật khẩu!' },
                  { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' },
                  { max: 100, message: 'Mật khẩu không được quá 100 ký tự' },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined className="text-gray-400" />}
                  placeholder="Tối thiểu 6 ký tự"
                  className="rounded-lg h-12"
                />
              </Form.Item>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.9 }}
            >
              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={isLoading}
                  block
                  className="h-12 rounded-lg text-base font-semibold bg-blue-500 hover:bg-blue-600 border-0 shadow-lg hover:shadow-xl transition-all duration-300"
                >
                  Đăng ký
                </Button>
              </Form.Item>
            </motion.div>
          </Form>

          {/* Footer */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 1 }}
            className="mt-6 text-center text-sm text-gray-500"
          >
            Đã có tài khoản?{' '}
            <Link
              to="/login"
              className="text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              Đăng nhập ngay
            </Link>
          </motion.div>
        </motion.div>
      </motion.div>
    </div>
  );
};

export default Register;
