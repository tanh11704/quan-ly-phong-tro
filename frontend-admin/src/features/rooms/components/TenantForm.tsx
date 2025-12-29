import { MailOutlined, PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, Modal, Switch, message } from 'antd';
import dayjs from 'dayjs';
import { useEffect } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useCreateTenant, useUpdateTenant } from '../api/tenantsApi';
import type { TenantCreationRequest, TenantUpdateRequest } from '../types/tenants';
import { TenantCreationRequestSchema, TenantUpdateRequestSchema } from '../types/tenants';

interface TenantFormProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: TenantCreationRequest | TenantUpdateRequest) => Promise<void>;
  roomId: number | null;
  initialValues?: Partial<TenantCreationRequest | TenantUpdateRequest>;
  isEdit?: boolean;
  tenantId?: number;
}

export const TenantForm = ({
  open,
  onClose,
  onSubmit,
  roomId,
  initialValues,
  isEdit = false,
  tenantId,
}: TenantFormProps) => {
  const [form] = Form.useForm();
  const { mutateAsync: createTenant, isPending: isCreating } = useCreateTenant();
  const { mutateAsync: updateTenant, isPending: isUpdating } = useUpdateTenant();

  useEffect(() => {
    if (open) {
      if (initialValues) {
        const values = initialValues as Partial<TenantCreationRequest>;
        form.setFieldsValue({
          ...initialValues,
          startDate: values.startDate ? dayjs(values.startDate) : undefined,
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
        roomId?: number;
        name?: string;
        phone?: string;
        email?: string;
        isContractHolder?: boolean;
        startDate?: dayjs.Dayjs;
      };

      if (isEdit && tenantId) {
        const requestData: TenantUpdateRequest = {
          name: formValues.name,
          phone: formValues.phone || undefined,
          email: formValues.email || null,
          isContractHolder: formValues.isContractHolder,
        };

        const validatedData = TenantUpdateRequestSchema.parse(requestData);
        const response = await updateTenant({ id: tenantId, request: validatedData });
        message.success(response.message || 'Cập nhật thông tin khách thuê thành công!');

        form.resetFields();
        onClose();

        if (onSubmit) {
          await onSubmit(validatedData);
        }
      } else {
        const requestData: TenantCreationRequest = {
          roomId: formValues.roomId!,
          name: formValues.name!,
          phone: formValues.phone || undefined,
          email: formValues.email || null,
          isContractHolder: formValues.isContractHolder || false,
          startDate: formValues.startDate ? formValues.startDate.format('YYYY-MM-DD') : undefined,
        };

        const validatedData = TenantCreationRequestSchema.parse(requestData);
        const response = await createTenant(validatedData);
        message.success(response.message || 'Thêm khách thuê thành công!');

        form.resetFields();
        onClose();

        if (onSubmit) {
          await onSubmit(validatedData);
        }
      }
    } catch (error) {
      console.error('Form validation error:', error);
      message.error(
        getErrorMessage(
          error,
          isEdit
            ? 'Cập nhật thông tin khách thuê thất bại'
            : 'Thêm khách thuê thất bại. Vui lòng thử lại.',
        ),
      );
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
            <h3 className="text-lg font-semibold mb-0">
              {isEdit ? 'Cập nhật thông tin khách thuê' : 'Thêm khách thuê mới'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Nhập thông tin khách thuê</p>
          </div>
        </div>
      }
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-4">
        {!isEdit && (
          <Form.Item
            name="roomId"
            label="ID Phòng"
            rules={[{ required: true, message: 'ID phòng không được để trống!' }]}
            hidden
          >
            <Input type="hidden" />
          </Form.Item>
        )}

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
          name="email"
          label="Email"
          rules={[
            {
              type: 'email',
              message: 'Email không hợp lệ',
            },
          ]}
        >
          <Input
            prefix={<MailOutlined className="text-gray-400" />}
            placeholder="tenant@example.com"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        {!isEdit && (
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
        )}

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
              loading={isCreating || isUpdating}
              className="bg-blue-500 hover:bg-blue-600 border-0"
            >
              {isEdit ? 'Cập nhật' : 'Thêm khách thuê'}
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Modal>
  );
};
