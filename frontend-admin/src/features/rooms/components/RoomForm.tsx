import { CheckCircleOutlined, DollarOutlined, HomeOutlined } from '@ant-design/icons';
import { Button, Form, Input, InputNumber, Modal, Select, message } from 'antd';
import { motion } from 'motion/react';
import { useEffect } from 'react';
import { useBuildings } from '../../buildings/api/buildingsApi';
import type { RoomCreationRequest, RoomUpdateRequest } from '../types/rooms';
import { RoomCreationRequestSchema, RoomStatus, RoomUpdateRequestSchema } from '../types/rooms';

interface RoomFormProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: RoomCreationRequest | RoomUpdateRequest) => Promise<void>;
  initialValues?: Partial<RoomCreationRequest | RoomUpdateRequest>;
  isEdit?: boolean;
  defaultBuildingId?: number;
}

const { Option } = Select;

export const RoomForm = ({
  open,
  onClose,
  onSubmit,
  initialValues,
  isEdit = false,
  defaultBuildingId,
}: RoomFormProps) => {
  const [form] = Form.useForm();
  const { data: buildingsData } = useBuildings(0, 100); // Get all buildings for select
  const buildings = buildingsData?.content || [];

  useEffect(() => {
    if (open) {
      if (initialValues) {
        form.setFieldsValue(initialValues);
      } else if (defaultBuildingId && !isEdit) {
        form.setFieldsValue({ buildingId: defaultBuildingId });
      } else {
        form.resetFields();
      }
    }
  }, [open, initialValues, defaultBuildingId, isEdit, form]);

  const handleSubmit = async (values: RoomCreationRequest | RoomUpdateRequest) => {
    try {
      const validatedData = isEdit
        ? RoomUpdateRequestSchema.parse(values)
        : RoomCreationRequestSchema.parse(values);
      await onSubmit(validatedData);
      form.resetFields();
      onClose();
    } catch (error) {
      console.error('Form validation error:', error);
      message.error('Vui lòng kiểm tra lại thông tin đã nhập');
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
            <HomeOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isEdit ? 'Chỉnh sửa phòng' : 'Tạo phòng mới'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Nhập thông tin phòng</p>
          </div>
        </div>
      }
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-4">
        {!isEdit && (
          <Form.Item
            name="buildingId"
            label="Tòa nhà"
            rules={[{ required: true, message: 'Vui lòng chọn tòa nhà!' }]}
          >
            <Select
              size="large"
              className="rounded-lg"
              placeholder="Chọn tòa nhà"
              disabled={!!defaultBuildingId}
            >
              {buildings.map((building) => (
                <Option key={building.id} value={building.id}>
                  {building.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
        )}

        <Form.Item
          name="roomNo"
          label="Số phòng"
          rules={isEdit ? [] : [{ required: true, message: 'Vui lòng nhập số phòng!' }]}
        >
          <Input
            prefix={<HomeOutlined className="text-gray-400" />}
            placeholder="P.101"
            size="large"
            className="rounded-lg"
          />
        </Form.Item>

        <Form.Item
          name="price"
          label="Giá thuê (VNĐ/tháng)"
          rules={[
            { type: 'number', min: 0, message: 'Giá thuê phải >= 0' },
            ...(isEdit ? [] : [{ required: true, message: 'Vui lòng nhập giá thuê!' }]),
          ]}
        >
          <InputNumber
            prefix={<DollarOutlined className="text-gray-400" />}
            placeholder="3000000"
            size="large"
            className="w-full rounded-lg"
            min={0}
          />
        </Form.Item>

        <Form.Item name="status" label="Trạng thái">
          <Select size="large" className="rounded-lg" placeholder="Chọn trạng thái">
            <Option value={RoomStatus.VACANT}>
              <CheckCircleOutlined className="text-green-500 mr-2" />
              Trống
            </Option>
            <Option value={RoomStatus.OCCUPIED}>
              <CheckCircleOutlined className="text-red-500 mr-2" />
              Đã thuê
            </Option>
            <Option value={RoomStatus.MAINTENANCE}>
              <CheckCircleOutlined className="text-orange-500 mr-2" />
              Bảo trì
            </Option>
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
              className="bg-blue-500 hover:bg-blue-600 border-0"
            >
              {isEdit ? 'Cập nhật' : 'Tạo mới'}
            </Button>
          </motion.div>
        </div>
      </Form>
    </Modal>
  );
};
