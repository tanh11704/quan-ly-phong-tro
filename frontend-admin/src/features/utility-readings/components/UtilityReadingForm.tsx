import { CameraOutlined, DropboxOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, InputNumber, Modal, Switch, message } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useCreateUtilityReading, useUpdateUtilityReading } from '../api/utilityReadingsApi';
import type {
  UtilityReadingCreationRequest,
  UtilityReadingUpdateRequest,
} from '../types/utility-readings';
import {
  UtilityReadingCreationRequestSchema,
  UtilityReadingUpdateRequestSchema,
} from '../types/utility-readings';

interface UtilityReadingFormProps {
  open: boolean;
  onClose: () => void;
  roomId: number | null;
  initialValues?: Partial<UtilityReadingCreationRequest | UtilityReadingUpdateRequest>;
  isEdit?: boolean;
  readingId?: number;
}

export const UtilityReadingForm = ({
  open,
  onClose,
  roomId,
  initialValues,
  isEdit = false,
  readingId,
}: UtilityReadingFormProps) => {
  const [form] = Form.useForm();
  const { mutateAsync: createReading, isPending: isCreating } = useCreateUtilityReading();
  const { mutateAsync: updateReading, isPending: isUpdating } = useUpdateUtilityReading();

  useEffect(() => {
    if (open) {
      if (initialValues) {
        const values = initialValues as Partial<UtilityReadingCreationRequest>;
        form.setFieldsValue({
          ...initialValues,
          month: values.month ? dayjs(values.month + '-01') : undefined,
        });
      } else {
        form.resetFields();
        // Set default values
        if (roomId) {
          form.setFieldsValue({
            roomId,
            month: dayjs(),
            isMeterReset: false,
          });
        }
      }
    }
  }, [open, initialValues, roomId, form]);

  const handleSubmit = async (values: unknown) => {
    try {
      const formValues = values as {
        roomId: number;
        month: Dayjs;
        electricIndex?: number | null;
        waterIndex?: number | null;
        isMeterReset?: boolean;
        imageEvidence?: string | null;
      };

      if (isEdit && readingId) {
        const requestData: UtilityReadingUpdateRequest = {
          electricIndex: formValues.electricIndex ?? null,
          waterIndex: formValues.waterIndex ?? null,
          isMeterReset: formValues.isMeterReset ?? false,
          imageEvidence: formValues.imageEvidence || null,
        };

        const validatedData = UtilityReadingUpdateRequestSchema.parse(requestData);
        const response = await updateReading({ id: readingId, request: validatedData });
        message.success(response.message || 'Cập nhật chỉ số điện nước thành công!');
      } else {
        const requestData: UtilityReadingCreationRequest = {
          roomId: formValues.roomId,
          month: formValues.month.format('YYYY-MM'),
          electricIndex: formValues.electricIndex ?? null,
          waterIndex: formValues.waterIndex ?? null,
          isMeterReset: formValues.isMeterReset ?? false,
          imageEvidence: formValues.imageEvidence || null,
        };

        const validatedData = UtilityReadingCreationRequestSchema.parse(requestData);
        const response = await createReading(validatedData);
        message.success(response.message || 'Ghi chỉ số điện nước thành công!');
      }

      form.resetFields();
      onClose();
    } catch (error) {
      console.error('Form validation error:', error);
      message.error(
        getErrorMessage(
          error,
          isEdit
            ? 'Cập nhật chỉ số điện nước thất bại'
            : 'Ghi chỉ số điện nước thất bại. Vui lòng thử lại.',
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
            <ThunderboltOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isEdit ? 'Cập nhật chỉ số điện nước' : 'Ghi chỉ số điện nước'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Nhập thông tin chỉ số điện nước</p>
          </div>
        </div>
      }
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-4">
        {!isEdit && (
          <>
            <Form.Item
              name="roomId"
              label="ID Phòng"
              rules={[{ required: true, message: 'ID phòng không được để trống!' }]}
              hidden
            >
              <Input type="hidden" />
            </Form.Item>

            <Form.Item
              name="month"
              label="Tháng"
              rules={[{ required: true, message: 'Vui lòng chọn tháng!' }]}
            >
              <DatePicker
                picker="month"
                className="w-full rounded-lg"
                size="large"
                format="MM/YYYY"
                placeholder="Chọn tháng"
              />
            </Form.Item>
          </>
        )}

        <Form.Item
          name="electricIndex"
          label={
            <span className="flex items-center gap-2">
              <ThunderboltOutlined className="text-yellow-500" />
              Chỉ số điện (kWh)
            </span>
          }
        >
          <InputNumber
            className="w-full rounded-lg"
            size="large"
            min={0}
            placeholder="Nhập chỉ số điện"
            prefix="kWh"
          />
        </Form.Item>

        <Form.Item
          name="waterIndex"
          label={
            <span className="flex items-center gap-2">
              <DropboxOutlined className="text-blue-500" />
              Chỉ số nước (m³)
            </span>
          }
        >
          <InputNumber
            className="w-full rounded-lg"
            size="large"
            min={0}
            placeholder="Nhập chỉ số nước"
            prefix="m³"
          />
        </Form.Item>

        <Form.Item
          name="isMeterReset"
          label="Đồng hồ đã thay mới/quay vòng"
          valuePropName="checked"
          tooltip="Tick nếu đồng hồ điện/nước đã thay mới hoặc quay vòng (ví dụ 99999 -> 00000)"
        >
          <Switch checkedChildren="Có" unCheckedChildren="Không" />
        </Form.Item>

        <Form.Item
          name="imageEvidence"
          label={
            <span className="flex items-center gap-2">
              <CameraOutlined />
              URL ảnh chứng cứ
            </span>
          }
          rules={[
            {
              type: 'url',
              message: 'URL không hợp lệ',
            },
          ]}
        >
          <Input placeholder="https://example.com/image.jpg" size="large" className="rounded-lg" />
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
              {isEdit ? 'Cập nhật' : 'Ghi chỉ số'}
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Modal>
  );
};
