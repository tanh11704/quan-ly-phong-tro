import {
  BuildOutlined,
  DeleteOutlined,
  DollarOutlined,
  EditOutlined,
  HomeOutlined,
} from '@ant-design/icons';
import { Card, Popconfirm, Tag, Tooltip } from 'antd';
import { motion } from 'motion/react';
import type { RoomResponse } from '../types/rooms';
import { RoomStatus } from '../types/rooms';

interface RoomCardProps {
  room: RoomResponse;
  onClick: () => void;
  onEdit?: (room: RoomResponse) => void;
  onDelete?: (id: number) => void;
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

const getStatusColor = (status: string) => {
  switch (status) {
    case RoomStatus.VACANT:
      return 'green';
    case RoomStatus.OCCUPIED:
      return 'red';
    case RoomStatus.MAINTENANCE:
      return 'orange';
    default:
      return 'default';
  }
};

const getStatusLabel = (status: string) => {
  switch (status) {
    case RoomStatus.VACANT:
      return 'Trống';
    case RoomStatus.OCCUPIED:
      return 'Đã thuê';
    case RoomStatus.MAINTENANCE:
      return 'Bảo trì';
    default:
      return status;
  }
};

export const RoomCard = ({ room, onClick, onEdit, onDelete }: RoomCardProps) => {
  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(room);
  };

  const handleDelete = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    onDelete?.(room.id);
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
            room.status === RoomStatus.VACANT
              ? '#10b981'
              : room.status === RoomStatus.OCCUPIED
                ? '#ef4444'
                : '#f97316',
        }}
        bodyStyle={{ padding: '20px' }}
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center text-white shrink-0">
                <HomeOutlined className="text-xl" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-800 mb-1 truncate">{room.roomNo}</h3>
                <p className="text-sm text-gray-500 truncate flex items-center gap-1">
                  <BuildOutlined className="text-xs shrink-0" />
                  <span className="truncate">{room.buildingName}</span>
                </p>
              </div>
            </div>

            <div className="flex flex-wrap gap-3 mb-3">
              <Tag color={getStatusColor(room.status)} className="shrink-0">
                {getStatusLabel(room.status)}
              </Tag>
            </div>

            <div className="flex items-center gap-2 text-sm text-gray-600">
              <DollarOutlined className="text-green-500 shrink-0" />
              <span className="font-medium shrink-0">Giá thuê:</span>
              <span className="text-gray-500 font-semibold">{formatPrice(room.price)}/tháng</span>
            </div>
          </div>
          {(onEdit || onDelete) && (
            <div className="flex gap-2 shrink-0" onClick={(e) => e.stopPropagation()}>
              {onEdit && (
                <Tooltip title="Chỉnh sửa">
                  <button
                    onClick={handleEdit}
                    className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                  >
                    <EditOutlined />
                  </button>
                </Tooltip>
              )}
              {onDelete && (
                <Popconfirm
                  title="Xóa phòng"
                  description="Bạn có chắc chắn muốn xóa phòng này?"
                  onConfirm={handleDelete}
                  okText="Xóa"
                  cancelText="Hủy"
                  okButtonProps={{ danger: true }}
                >
                  <Tooltip title="Xóa">
                    <button
                      onClick={(e) => e.stopPropagation()}
                      className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <DeleteOutlined />
                    </button>
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
