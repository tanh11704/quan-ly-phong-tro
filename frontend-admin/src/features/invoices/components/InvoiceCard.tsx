import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  MailOutlined,
} from '@ant-design/icons';
import { Button, Card, Popconfirm, Tag, Tooltip } from 'antd';
import { motion } from 'motion/react';
import type { InvoiceResponse } from '../types/invoices';
import { InvoiceStatus } from '../types/invoices';

interface InvoiceCardProps {
  invoice: InvoiceResponse;
  onClick: () => void;
  onPay?: (id: number) => void;
  onSendEmail?: (id: number) => void;
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

const formatDate = (dateString: string): string => {
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
    case InvoiceStatus.PAID:
      return 'green';
    case InvoiceStatus.UNPAID:
      return 'red';
    case InvoiceStatus.DRAFT:
      return 'default';
    default:
      return 'default';
  }
};

const getStatusLabel = (status: InvoiceStatus) => {
  switch (status) {
    case InvoiceStatus.PAID:
      return 'Đã thanh toán';
    case InvoiceStatus.UNPAID:
      return 'Chưa thanh toán';
    case InvoiceStatus.DRAFT:
      return 'Nháp';
    default:
      return status;
  }
};

const getStatusIcon = (status: InvoiceStatus) => {
  switch (status) {
    case InvoiceStatus.PAID:
      return <CheckCircleOutlined />;
    case InvoiceStatus.UNPAID:
      return <ClockCircleOutlined />;
    default:
      return null;
  }
};

export const InvoiceCard = ({ invoice, onClick, onPay, onSendEmail }: InvoiceCardProps) => {
  const handlePay = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    onPay?.(invoice.id);
  };

  const handleSendEmail = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    onSendEmail?.(invoice.id);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ scale: 1.02 }}
      className="cursor-pointer"
      onClick={onClick}
    >
      <Card
        className="mb-4 hover:shadow-lg transition-all duration-300 border-l-4"
        style={{
          borderLeftColor:
            invoice.status === InvoiceStatus.PAID
              ? '#10b981'
              : invoice.status === InvoiceStatus.UNPAID
                ? '#ef4444'
                : '#6b7280',
        }}
        bodyStyle={{ padding: '20px' }}
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center text-white shrink-0">
                <DollarOutlined className="text-xl" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-800 mb-1 truncate">
                  {invoice.roomNo}
                </h3>
                <p className="text-sm text-gray-500 truncate">{invoice.tenantName}</p>
              </div>
            </div>

            <div className="flex flex-wrap gap-3 mb-3">
              <Tag color={getStatusColor(invoice.status)} className="shrink-0">
                {getStatusIcon(invoice.status)}
                <span className="ml-1">{getStatusLabel(invoice.status)}</span>
              </Tag>
              <div className="text-sm text-gray-600">
                Kỳ: <span className="font-medium">{formatPeriod(invoice.period)}</span>
              </div>
            </div>

            <div className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-600">Tiền phòng:</span>
                <span className="font-semibold text-gray-800">
                  {formatPrice(invoice.roomPrice)}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600">Tiền điện:</span>
                <span className="font-semibold text-gray-800">
                  {formatPrice(invoice.elecAmount)}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600">Tiền nước:</span>
                <span className="font-semibold text-gray-800">
                  {formatPrice(invoice.waterAmount)}
                </span>
              </div>
              <div className="flex items-center justify-between pt-2 border-t border-gray-200">
                <span className="font-semibold text-gray-800">Tổng tiền:</span>
                <span className="font-bold text-lg text-blue-600">
                  {formatPrice(invoice.totalAmount)}
                </span>
              </div>
              <div className="text-xs text-gray-500">
                Hạn thanh toán: {formatDate(invoice.dueDate)}
              </div>
            </div>
          </div>
          {(onPay || onSendEmail) && (
            <div className="flex flex-col gap-2 shrink-0" onClick={(e) => e.stopPropagation()}>
              {onPay && invoice.status === InvoiceStatus.UNPAID && (
                <Popconfirm
                  title="Xác nhận thanh toán"
                  description="Bạn có chắc chắn khách đã thanh toán hóa đơn này?"
                  onConfirm={handlePay}
                  okText="Xác nhận"
                  cancelText="Hủy"
                  okButtonProps={{ type: 'primary' }}
                >
                  <Tooltip title="Xác nhận thanh toán">
                    <Button
                      type="primary"
                      icon={<CheckCircleOutlined />}
                      size="small"
                      className="bg-green-500 hover:bg-green-600 border-0"
                    >
                      Thanh toán
                    </Button>
                  </Tooltip>
                </Popconfirm>
              )}
              {onSendEmail && (
                <Tooltip title="Gửi email hóa đơn">
                  <Button icon={<MailOutlined />} size="small" onClick={handleSendEmail}>
                    Gửi email
                  </Button>
                </Tooltip>
              )}
            </div>
          )}
        </div>
      </Card>
    </motion.div>
  );
};
