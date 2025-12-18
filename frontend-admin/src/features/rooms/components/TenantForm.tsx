import { PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, Modal, Switch, message } from 'antd';
import dayjs from 'dayjs';
import { useEffect } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useCreateTenant } from '../api/tenantsApi';
import type { TenantCreationRequest } from '../types/tenants';
import { TenantCreationRequestSchema } from '../types/tenants';

interface TenantFormProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: TenantCreationRequest) => Promise<void>;
  roomId: number | null;
  initialValues?: Partial<TenantCreationRequest>;
}

export const TenantForm = ({ open, onClose, onSubmit, roomId, initialValues }: TenantFormProps) => {
  const [form] = Form.useForm();
  const { mutateAsync: createTenant, isPending: isCreating } = useCreateTenant();

  useEffect(() => {
    if (open) {
      if (initialValues) {
        form.setFieldsValue({
          ...initialValues,
          startDate: initialValues.startDate ? dayjs(initialValues.startDate) : undefined,
        });
      } else {
        form.resetFields();
        // Set default roomId if provided
        if (roomId) {
          form.setFieldsValue({ roomId, isContractHolder: false });
        }
      }
    }
  }, [open, initialValues, roomId, form]);

  const handleSubmit = async (values: unknown) => {
    try {
      const formValues = values as {
        roomId: number;
        name: string;
        phone?: string;
        isContractHolder?: boolean;
        startDate?: dayjs.Dayjs;
      };

      const requestData: TenantCreationRequest = {
        roomId: formValues.roomId,
        name: formValues.name,
        phone: formValues.phone || undefined,
        isContractHolder: formValues.isContractHolder || false,
        startDate: formValues.startDate ? formValues.startDate.format('YYYY-MM-DD') : undefined,
      };

      const validatedData = TenantCreationRequestSchema.parse(requestData);

      // Call API
      const response = await createTenant(validatedData);
      message.success(response.message || 'Thêm khách thuê thành công!');

      form.resetFields();
      onClose();

      // Call parent onSubmit if provided (after closing form)
      if (onSubmit) {
        await onSubmit(validatedData);
      }
    } catch (error) {
      console.error('Form validation error:', error);
      message.error(getErrorMessage(error, 'Thêm khách thuê thất bại. Vui lòng thử lại.'));
    }
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={600}
      title={
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white">
            <UserOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">Thêm khách thuê mới</h3>
            <p className="text-sm text-gray-500 mb-0">Nhập thông tin khách thuê</p>
          </div>
        </div>
      }
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-4">
        <Form.Item
          name="roomId"
          label="ID Phòng"
          rules={[{ required: true, message: 'ID phòng không được để trống!' }]}
          hidden
        >
          <Input type="hidden" />
        </Form.Item>

        <Form.Item
          name="name"
          label="Tên khách thuê"
          rules={[{ required: true, message: 'Vui lòng nhập tên khách thuê!' }]}
        >
          <Input
            prefix={<UserOutlined className="text-gray-400" />}
            placeholder="Nguyễn Văn A"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        <Form.Item name="phone" label="Số điện thoại">
          <Input
            prefix={<PhoneOutlined className="text-gray-400" />}
            placeholder="0901234567"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        <Form.Item
          name="startDate"
          label="Ngày bắt đầu hợp đồng"
          getValueFromEvent={(date) => date}
          getValueProps={(value) => ({
            value: value ? dayjs(value) : undefined,
          })}
        >
          <DatePicker
            className="w-full rounded-lg"
            size="large"
            format="DD/MM/YYYY"
            placeholder="Chọn ngày bắt đầu"
            prefixCls="ant-picker"
          />
        </Form.Item>

        <Form.Item
          name="isContractHolder"
          label="Người đại diện hợp đồng"
          valuePropName="checked"
          initialValue={false}
        >
          <Switch checkedChildren="Có" unCheckedChildren="Không" />
        </Form.Item>

        <Form.Item className="mb-0 mt-6">
          <div className="flex justify-end gap-3">
            <Button onClick={onClose} size="large">
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              loading={isCreating}
              className="bg-blue-500 hover:bg-blue-600 border-0"
            >
              Thêm khách thuê
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Modal>
  );
};
