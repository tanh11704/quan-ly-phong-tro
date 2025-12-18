import { BuildOutlined, HomeOutlined, PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { Descriptions, Empty, Modal, Pagination, Spin, Tag } from 'antd';
import { motion } from 'motion/react';
import { useState } from 'react';
import { useBuilding, useBuildingRooms } from '../api/buildingsApi';
import { WaterCalcMethod } from '../types/buildings';

interface BuildingDetailProps {
  buildingId: number | null;
  open: boolean;
  onClose: () => void;
}

const formatPrice = (price?: number) => {
  if (!price) return 'Chưa cập nhật';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

export const BuildingDetail = ({ buildingId, open, onClose }: BuildingDetailProps) => {
  const [roomsPage, setRoomsPage] = useState(0);
  const [roomsPageSize, setRoomsPageSize] = useState(20);

  const { data: building, isLoading: isLoadingBuilding } = useBuilding(buildingId);
  const { data: roomsData, isLoading: isLoadingRooms } = useBuildingRooms(
    buildingId,
    roomsPage,
    roomsPageSize,
  );

  const rooms = roomsData?.content || [];
  const roomsPageInfo = roomsData?.page;

  if (!buildingId) return null;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={800}
      title={
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white">
            <BuildOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isLoadingBuilding ? 'Đang tải...' : building?.name || 'Chi tiết tòa nhà'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Thông tin chi tiết và danh sách phòng</p>
          </div>
        </div>
      }
    >
      {isLoadingBuilding ? (
        <div className="flex justify-center items-center py-12">
          <Spin size="large" />
        </div>
      ) : !building ? (
        <Empty description="Không tìm thấy thông tin tòa nhà" />
      ) : (
        <div className="mt-4 space-y-6">
          {/* Building Info */}
          <div>
            <h4 className="text-base font-semibold text-gray-800 mb-3">Thông tin tòa nhà</h4>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="Tên tòa nhà">{building.name}</Descriptions.Item>
              <Descriptions.Item label="ID">{building.id}</Descriptions.Item>
              {building.ownerName && (
                <Descriptions.Item label="Chủ tòa nhà">
                  <div className="flex items-center gap-2">
                    <UserOutlined className="text-gray-400" />
                    {building.ownerName}
                  </div>
                </Descriptions.Item>
              )}
              {building.ownerPhone && (
                <Descriptions.Item label="Số điện thoại">
                  <div className="flex items-center gap-2">
                    <PhoneOutlined className="text-gray-400" />
                    {building.ownerPhone}
                  </div>
                </Descriptions.Item>
              )}
              <Descriptions.Item label="Đơn giá điện">
                {building.elecUnitPrice
                  ? `${formatPrice(building.elecUnitPrice)}/kWh`
                  : 'Chưa cập nhật'}
              </Descriptions.Item>
              <Descriptions.Item label="Đơn giá nước">
                {building.waterUnitPrice
                  ? `${formatPrice(building.waterUnitPrice)}/${
                      building.waterCalcMethod === WaterCalcMethod.BY_METER ? 'm³' : 'người'
                    }`
                  : 'Chưa cập nhật'}
              </Descriptions.Item>
              {building.waterCalcMethod && (
                <Descriptions.Item label="Phương pháp tính nước" span={2}>
                  <Tag color="blue">
                    {building.waterCalcMethod === WaterCalcMethod.BY_METER
                      ? 'Theo đồng hồ'
                      : 'Theo người'}
                  </Tag>
                </Descriptions.Item>
              )}
            </Descriptions>
          </div>

          {/* Rooms List */}
          <div>
            <h4 className="text-base font-semibold text-gray-800 mb-3">
              Danh sách phòng ({roomsPageInfo?.totalElements || 0})
            </h4>
            {isLoadingRooms ? (
              <div className="flex justify-center items-center py-8">
                <Spin />
              </div>
            ) : !rooms || rooms.length === 0 ? (
              <Empty description="Chưa có phòng nào" />
            ) : (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
                  {rooms.map((room, index) => (
                    <motion.div
                      key={room.id}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.05 }}
                      className="border rounded-lg p-4 bg-gray-50 hover:bg-gray-100 transition-colors"
                    >
                      <div className="flex items-center gap-2 mb-2">
                        <HomeOutlined className="text-blue-500" />
                        <span className="font-semibold text-gray-800">{room.roomNo}</span>
                        <Tag
                          color={
                            room.status === 'OCCUPIED'
                              ? 'red'
                              : room.status === 'AVAILABLE'
                                ? 'green'
                                : 'default'
                          }
                          className="ml-auto"
                        >
                          {room.status === 'OCCUPIED'
                            ? 'Đã thuê'
                            : room.status === 'AVAILABLE'
                              ? 'Trống'
                              : room.status}
                        </Tag>
                      </div>
                      <p className="text-sm text-gray-600">
                        Giá thuê: <span className="font-semibold">{formatPrice(room.price)}</span>
                      </p>
                    </motion.div>
                  ))}
                </div>
                {roomsPageInfo && roomsPageInfo.totalPages > 1 && (
                  <div className="flex justify-center mt-4">
                    <Pagination
                      current={roomsPageInfo.page + 1}
                      total={roomsPageInfo.totalElements}
                      pageSize={roomsPageInfo.size}
                      onChange={(page: number, size: number) => {
                        setRoomsPage(page - 1);
                        setRoomsPageSize(size);
                      }}
                      showSizeChanger
                      showTotal={(total: number) => `Tổng ${total} phòng`}
                    />
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </Modal>
  );
};
