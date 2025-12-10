import {
  BuildOutlined,
  DeleteOutlined,
  DollarOutlined,
  DropboxOutlined,
  EditOutlined,
  PhoneOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Card, Popconfirm, Tag, Tooltip } from 'antd';
import { motion } from 'motion/react';
import type { BuildingResponse } from '../types/buildings';
import { WaterCalcMethod } from '../types/buildings';

interface BuildingCardProps {
  building: BuildingResponse;
  onClick: () => void;
  onEdit?: (building: BuildingResponse) => void;
  onDelete?: (id: number) => void;
}

const formatPrice = (price?: number) => {
  if (!price) return 'Chưa cập nhật';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

export const BuildingCard = ({ building, onClick, onEdit, onDelete }: BuildingCardProps) => {
  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(building);
  };

  const handleDelete = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    onDelete?.(building.id);
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
          borderLeftColor: '#3b82f6',
        }}
        bodyStyle={{ padding: '20px' }}
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-12 h-12 bg-linear-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white">
                <BuildOutlined className="text-xl" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-800 mb-1 truncate">
                  {building.name}
                </h3>
                {building.ownerName && (
                  <p className="text-sm text-gray-500 truncate flex items-center gap-1">
                    <UserOutlined className="text-xs" />
                    {building.ownerName}
                  </p>
                )}
              </div>
            </div>

            <div className="flex flex-wrap gap-3 mb-3">
              {building.ownerPhone && (
                <div className="flex items-center gap-2 text-sm text-gray-600 min-w-0">
                  <PhoneOutlined className="text-gray-400 shrink-0" />
                  <span className="truncate">{building.ownerPhone}</span>
                </div>
              )}
              {building.waterCalcMethod && (
                <Tag color="blue" className="shrink-0">
                  {building.waterCalcMethod === WaterCalcMethod.BY_METER
                    ? 'Theo đồng hồ'
                    : 'Theo người'}
                </Tag>
              )}
            </div>

            <div className="space-y-2 text-sm">
              <div className="flex items-center gap-2 text-gray-600">
                <DollarOutlined className="text-green-500 shrink-0" />
                <span className="font-medium shrink-0">Điện:</span>
                <span className="text-gray-500 truncate">
                  {building.elecUnitPrice
                    ? `${formatPrice(building.elecUnitPrice)}/kWh`
                    : 'Chưa cập nhật'}
                </span>
              </div>
              <div className="flex items-center gap-2 text-gray-600">
                <DropboxOutlined className="text-blue-500 shrink-0" />
                <span className="font-medium shrink-0">Nước:</span>
                <span className="text-gray-500 truncate">
                  {building.waterUnitPrice
                    ? `${formatPrice(building.waterUnitPrice)}/${
                        building.waterCalcMethod === WaterCalcMethod.BY_METER ? 'm³' : 'người'
                      }`
                    : 'Chưa cập nhật'}
                </span>
              </div>
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
                  title="Xóa tòa nhà"
                  description="Bạn có chắc chắn muốn xóa tòa nhà này?"
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
