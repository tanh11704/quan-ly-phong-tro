import { DollarOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Modal, message } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useCreateInvoices } from '../api/invoicesApi';
import type { InvoiceCreationRequest } from '../types/invoices';
import { InvoiceCreationRequestSchema } from '../types/invoices';

interface InvoiceFormProps {
  open: boolean;
  onClose: () => void;
  buildingId: number | null;
}

export const InvoiceForm = ({ open, onClose, buildingId }: InvoiceFormProps) => {
  const [form] = Form.useForm();
  const { mutateAsync: createInvoices, isPending: isCreating } = useCreateInvoices();

  useEffect(() => {
    if (open) {
      form.resetFields();
      if (buildingId) {
        form.setFieldsValue({
          buildingId,
          period: dayjs(),
        });
      }
    }
  }, [open, buildingId, form]);

  const handleSubmit = async (values: unknown) => {
    try {
      const formValues = values as {
        buildingId: number;
        period: Dayjs;
      };

      const requestData: InvoiceCreationRequest = {
        buildingId: formValues.buildingId,
        period: formValues.period.format('YYYY-MM'),
      };

      const validatedData = InvoiceCreationRequestSchema.parse(requestData);
      const response = await createInvoices(validatedData);
      message.success(
        `Tạo ${response.result.length} hóa đơn thành công! ${response.message || ''}`,
      );

      form.resetFields();
      onClose();
    } catch (error) {
      console.error('Form validation error:', error);
      message.error(getErrorMessage(error, 'Tạo hóa đơn thất bại. Vui lòng thử lại.'));
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
            <DollarOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">Tạo hóa đơn cho tòa nhà</h3>
            <p className="text-sm text-gray-500 mb-0">Chọn kỳ thanh toán để tạo hóa đơn</p>
          </div>
        </div>
      }
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-4">
        <Form.Item
          name="buildingId"
          label="ID Tòa nhà"
          rules={[{ required: true, message: 'ID tòa nhà không được để trống!' }]}
          hidden
        >
          <input type="hidden" />
        </Form.Item>

        <Form.Item
          name="period"
          label="Kỳ thanh toán"
          rules={[{ required: true, message: 'Vui lòng chọn kỳ thanh toán!' }]}
        >
          <DatePicker
            picker="month"
            className="w-full rounded-lg"
            size="large"
            format="MM/YYYY"
            placeholder="Chọn kỳ thanh toán"
          />
        </Form.Item>

        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
          <div className="flex items-start gap-2 text-sm text-blue-800">
            <ThunderboltOutlined className="text-blue-500 mt-0.5" />
            <div>
              <p className="font-semibold mb-1">Lưu ý:</p>
              <ul className="list-disc list-inside space-y-1 text-blue-700">
                <li>Hệ thống sẽ tự động tạo hóa đơn cho tất cả phòng có khách thuê</li>
                <li>Các phòng đã có hóa đơn trong kỳ này sẽ được bỏ qua</li>
                <li>Hóa đơn sẽ tính toán: tiền phòng, tiền điện, tiền nước</li>
              </ul>
            </div>
          </div>
        </div>

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
              Tạo hóa đơn
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Modal>
  );
};
