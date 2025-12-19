import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { Descriptions, Empty, Modal, Spin, Tag } from 'antd';
import { useInvoiceDetail } from '../api/invoicesApi';
import type { InvoiceStatus } from '../types/invoices';
import { InvoiceStatus as InvoiceStatusEnum } from '../types/invoices';

interface InvoiceDetailProps {
  invoiceId: number | null;
  open: boolean;
  onClose: () => void;
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

const formatDate = (dateString: string | null | undefined): string => {
  if (!dateString) return 'Chưa có';
  try {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(date);
  } catch {
    return dateString;
  }
};

const formatPeriod = (period: string): string => {
  try {
    const [year, month] = period.split('-');
    const date = new Date(parseInt(year), parseInt(month) - 1);
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: 'long',
    }).format(date);
  } catch {
    return period;
  }
};

const getStatusColor = (status: InvoiceStatus) => {
  switch (status) {
    case InvoiceStatusEnum.PAID:
      return 'green';
    case InvoiceStatusEnum.UNPAID:
      return 'red';
    case InvoiceStatusEnum.DRAFT:
      return 'default';
    default:
      return 'default';
  }
};

const getStatusLabel = (status: InvoiceStatus) => {
  switch (status) {
    case InvoiceStatusEnum.PAID:
      return 'Đã thanh toán';
    case InvoiceStatusEnum.UNPAID:
      return 'Chưa thanh toán';
    case InvoiceStatusEnum.DRAFT:
      return 'Nháp';
    default:
      return status;
  }
};

const getStatusIcon = (status: InvoiceStatus) => {
  switch (status) {
    case InvoiceStatusEnum.PAID:
      return <CheckCircleOutlined />;
    case InvoiceStatusEnum.UNPAID:
      return <ClockCircleOutlined />;
    default:
      return null;
  }
};

export const InvoiceDetail = ({ invoiceId, open, onClose }: InvoiceDetailProps) => {
  const { data: invoice, isLoading } = useInvoiceDetail(invoiceId);

  if (!invoiceId) return null;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={700}
      title={
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white">
            <DollarOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isLoading ? 'Đang tải...' : invoice?.roomNo || 'Chi tiết hóa đơn'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Thông tin chi tiết hóa đơn</p>
          </div>
        </div>
      }
    >
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Spin size="large" />
        </div>
      ) : !invoice ? (
        <Empty description="Không tìm thấy thông tin hóa đơn" />
      ) : (
        <div className="mt-4">
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Số phòng">
              <span className="font-semibold">{invoice.roomNo}</span>
            </Descriptions.Item>
            <Descriptions.Item label="Khách thuê">
              <span className="font-semibold">{invoice.tenantName}</span>
            </Descriptions.Item>
            <Descriptions.Item label="Kỳ thanh toán">
              {formatPeriod(invoice.period)}
            </Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <Tag color={getStatusColor(invoice.status)} className="text-base py-1 px-3">
                {getStatusIcon(invoice.status)}
                <span className="ml-1">{getStatusLabel(invoice.status)}</span>
              </Tag>
            </Descriptions.Item>

            {invoice.oldElectricIndex !== undefined && invoice.newElectricIndex !== undefined && (
              <>
                <Descriptions.Item label="Chỉ số điện cũ">
                  {invoice.oldElectricIndex !== null ? `${invoice.oldElectricIndex} kWh` : 'N/A'}
                </Descriptions.Item>
                <Descriptions.Item label="Chỉ số điện mới">
                  {invoice.newElectricIndex !== null ? `${invoice.newElectricIndex} kWh` : 'N/A'}
                </Descriptions.Item>
                {invoice.electricUsage !== undefined && invoice.electricUsage !== null && (
                  <Descriptions.Item label="Lượng điện sử dụng">
                    <div className="flex items-center gap-2">
                      <ThunderboltOutlined className="text-yellow-500" />
                      <span className="font-semibold">{invoice.electricUsage} kWh</span>
                    </div>
                  </Descriptions.Item>
                )}
                {invoice.elecUnitPrice !== undefined && invoice.elecUnitPrice !== null && (
                  <Descriptions.Item label="Đơn giá điện">
                    {formatPrice(invoice.elecUnitPrice)}/kWh
                  </Descriptions.Item>
                )}
              </>
            )}

            {invoice.oldWaterIndex !== undefined && invoice.newWaterIndex !== undefined && (
              <>
                <Descriptions.Item label="Chỉ số nước cũ">
                  {invoice.oldWaterIndex !== null ? `${invoice.oldWaterIndex} m³` : 'N/A'}
                </Descriptions.Item>
                <Descriptions.Item label="Chỉ số nước mới">
                  {invoice.newWaterIndex !== null ? `${invoice.newWaterIndex} m³` : 'N/A'}
                </Descriptions.Item>
                {invoice.waterUsage !== undefined && invoice.waterUsage !== null && (
                  <Descriptions.Item label="Lượng nước sử dụng">
                    {invoice.waterUsage} m³
                  </Descriptions.Item>
                )}
                {invoice.waterUnitPrice !== undefined && invoice.waterUnitPrice !== null && (
                  <Descriptions.Item label="Đơn giá nước">
                    {formatPrice(invoice.waterUnitPrice)}/m³
                  </Descriptions.Item>
                )}
              </>
            )}

            <Descriptions.Item label="Tiền phòng">
              <span className="font-semibold">{formatPrice(invoice.roomPrice)}</span>
            </Descriptions.Item>
            <Descriptions.Item label="Tiền điện">
              <span className="font-semibold">{formatPrice(invoice.elecAmount)}</span>
            </Descriptions.Item>
            <Descriptions.Item label="Tiền nước">
              <span className="font-semibold">{formatPrice(invoice.waterAmount)}</span>
            </Descriptions.Item>
            <Descriptions.Item label="Tổng tiền">
              <span className="font-bold text-lg text-blue-600">
                {formatPrice(invoice.totalAmount)}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="Hạn thanh toán">
              {formatDate(invoice.dueDate)}
            </Descriptions.Item>
            {invoice.createdAt && (
              <Descriptions.Item label="Ngày tạo">
                {formatDate(invoice.createdAt)}
              </Descriptions.Item>
            )}
            {invoice.paidAt && (
              <Descriptions.Item label="Ngày thanh toán">
                {formatDate(invoice.paidAt)}
              </Descriptions.Item>
            )}
            <Descriptions.Item label="ID">{invoice.id}</Descriptions.Item>
          </Descriptions>
        </div>
      )}
    </Modal>
  );
};
