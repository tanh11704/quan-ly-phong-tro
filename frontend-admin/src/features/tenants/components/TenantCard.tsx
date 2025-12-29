import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  EditOutlined,
  HomeOutlined,
  MailOutlined,
  PhoneOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Button, Card, Popconfirm, Tag, Tooltip } from 'antd';
import { motion } from 'motion/react';
import type { TenantResponse } from '../../rooms/types/tenants';

interface TenantCardProps {
  tenant: TenantResponse;
  onClick: () => void;
  onEdit?: (tenant: TenantResponse) => void;
  onEndContract?: (id: number) => void;
}

const formatDate = (dateString: string | null | undefined): string => {
  if (!dateString) return 'Đang thuê';
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

export const TenantCard = ({ tenant, onClick, onEdit, onEndContract }: TenantCardProps) => {
  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(tenant);
  };

  const handleEndContract = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    onEndContract?.(tenant.id);
  };

  const isActive = !tenant.endDate;

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
          borderLeftColor: isActive ? '#10b981' : '#6b7280',
        }}
        bodyStyle={{ padding: '20px' }}
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center text-white shrink-0">
                <UserOutlined className="text-xl" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-800 mb-1 truncate">{tenant.name}</h3>
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <HomeOutlined className="text-xs shrink-0" />
                  <span className="truncate">{tenant.roomNo || `Phòng ID: ${tenant.roomId}`}</span>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap gap-3 mb-3">
              <Tag color={isActive ? 'green' : 'default'} className="shrink-0">
                {isActive ? (
                  <>
                    <CheckCircleOutlined className="mr-1" />
                    Đang thuê
                  </>
                ) : (
                  <>
                    <ClockCircleOutlined className="mr-1" />
                    Đã kết thúc
                  </>
                )}
              </Tag>
              {tenant.isContractHolder && (
                <Tag color="blue" className="shrink-0">
                  Đại diện hợp đồng
                </Tag>
              )}
            </div>

            <div className="space-y-2 text-sm">
              {tenant.phone && (
                <div className="flex items-center gap-2 text-gray-600">
                  <PhoneOutlined className="text-gray-400 shrink-0" />
                  <span className="truncate">{tenant.phone}</span>
                </div>
              )}
              {tenant.email && (
                <div className="flex items-center gap-2 text-gray-600">
                  <MailOutlined className="text-gray-400 shrink-0" />
                  <span className="truncate">{tenant.email}</span>
                </div>
              )}
              <div className="flex items-center gap-2 text-gray-600">
                <span className="font-medium shrink-0">Bắt đầu:</span>
                <span className="text-gray-500">{formatDate(tenant.startDate)}</span>
              </div>
              {tenant.endDate && (
                <div className="flex items-center gap-2 text-gray-600">
                  <span className="font-medium shrink-0">Kết thúc:</span>
                  <span className="text-gray-500">{formatDate(tenant.endDate)}</span>
                </div>
              )}
            </div>
          </div>
          {(onEdit || onEndContract) && (
            <div className="flex flex-col gap-2 shrink-0" onClick={(e) => e.stopPropagation()}>
              {onEdit && (
                <Tooltip title="Chỉnh sửa">
                  <Button
                    type="primary"
                    icon={<EditOutlined />}
                    size="small"
                    onClick={handleEdit}
                    className="bg-blue-500 hover:bg-blue-600 border-0"
                  >
                    Chỉnh sửa
                  </Button>
                </Tooltip>
              )}
              {onEndContract && isActive && (
                <Popconfirm
                  title="Kết thúc hợp đồng"
                  description="Bạn có chắc chắn muốn kết thúc hợp đồng của khách thuê này?"
                  onConfirm={handleEndContract}
                  okText="Kết thúc"
                  cancelText="Hủy"
                  okButtonProps={{ danger: true }}
                >
                  <Tooltip title="Kết thúc hợp đồng">
                    <Button type="link" danger size="small" onClick={(e) => e.stopPropagation()}>
                      Kết thúc hợp đồng
                    </Button>
                  </Tooltip>
                </Popconfirm>
              )}
            </div>
          )}
        </div>
      </Card>
    </motion.div>
  );
};
