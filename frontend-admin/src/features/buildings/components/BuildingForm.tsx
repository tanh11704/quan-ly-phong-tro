import {
  BuildOutlined,
  DollarOutlined,
  DropboxOutlined,
  PhoneOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Button, Form, Input, InputNumber, Modal, Select } from 'antd';
import { motion } from 'motion/react';
import { useEffect } from 'react';
import type { BuildingCreationRequest, BuildingUpdateRequest } from '../types/buildings';
import {
  BuildingCreationRequestSchema,
  BuildingUpdateRequestSchema,
  WaterCalcMethod,
} from '../types/buildings';

interface BuildingFormProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: BuildingCreationRequest | BuildingUpdateRequest) => Promise<void>;
  initialValues?: Partial<BuildingCreationRequest | BuildingUpdateRequest>;
  isEdit?: boolean;
}

const { Option } = Select;

export const BuildingForm = ({
  open,
  onClose,
  onSubmit,
  initialValues,
  isEdit = false,
}: BuildingFormProps) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open && initialValues) {
      form.setFieldsValue(initialValues);
    } else if (open && !initialValues) {
      form.resetFields();
    }
  }, [open, initialValues, form]);

  const handleSubmit = async (values: BuildingCreationRequest | BuildingUpdateRequest) => {
    try {
      const validatedData = isEdit
        ? BuildingUpdateRequestSchema.parse(values)
        : BuildingCreationRequestSchema.parse(values);
      await onSubmit(validatedData);
      form.resetFields();
      onClose();
    } catch (error) {
      console.error('Form validation error:', error);
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
          <div className="w-10 h-10 bg-linear-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white">
            <BuildOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isEdit ? 'Chỉnh sửa tòa nhà' : 'Tạo tòa nhà mới'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Nhập thông tin tòa nhà</p>
          </div>
        </div>
      }
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={initialValues}
        className="mt-4"
      >
        <Form.Item
          name="name"
          label="Tên tòa nhà"
          rules={isEdit ? [] : [{ required: true, message: 'Vui lòng nhập tên tòa nhà!' }]}
        >
          <Input
            prefix={<BuildOutlined className="text-gray-400" />}
            placeholder="Trọ Xanh"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        <Form.Item name="ownerName" label="Tên chủ tòa nhà">
          <Input
            prefix={<UserOutlined className="text-gray-400" />}
            placeholder="Nguyễn Văn Chủ"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        <Form.Item name="ownerPhone" label="Số điện thoại chủ tòa nhà">
          <Input
            prefix={<PhoneOutlined className="text-gray-400" />}
            placeholder="0909123456"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        <Form.Item
          name="elecUnitPrice"
          label="Đơn giá điện (VNĐ/kWh)"
          rules={[{ type: 'number', min: 0, message: 'Đơn giá điện phải >= 0' }]}
        >
          <InputNumber
            prefix={<DollarOutlined className="text-gray-400" />}
            placeholder="3500"
            size="large"
            className="w-full rounded-lg"
            min={0}
          />
        </Form.Item>

        <Form.Item
          name="waterUnitPrice"
          label="Đơn giá nước (VNĐ/m³ hoặc VNĐ/người)"
          rules={[{ type: 'number', min: 0, message: 'Đơn giá nước phải >= 0' }]}
        >
          <InputNumber
            prefix={<DropboxOutlined className="text-gray-400" />}
            placeholder="20000"
            size="large"
            className="w-full rounded-lg"
            min={0}
          />
        </Form.Item>

        <Form.Item
          name="waterCalcMethod"
          label="Phương pháp tính nước"
          rules={
            isEdit ? [] : [{ required: true, message: 'Vui lòng chọn phương pháp tính nước!' }]
          }
        >
          <Select size="large" className="rounded-lg">
            <Option value={WaterCalcMethod.BY_METER}>Theo đồng hồ (BY_METER)</Option>
            <Option value={WaterCalcMethod.PER_CAPITA}>Theo người (PER_CAPITA)</Option>
          </Select>
        </Form.Item>

        <div className="flex justify-end gap-3 mt-6">
          <Button onClick={onClose} size="large">
            Hủy
          </Button>
          <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              className="bg-linear-to-r from-blue-500 to-purple-600 border-0"
            >
              {isEdit ? 'Cập nhật' : 'Tạo mới'}
            </Button>
          </motion.div>
        </div>
      </Form>
    </Modal>
  );
};
