import { BuildOutlined, DollarOutlined, HomeOutlined } from '@ant-design/icons';
import { Descriptions, Empty, Modal, Spin, Tag } from 'antd';
import { useRoom } from '../api/roomsApi';
import { RoomStatus } from '../types/rooms';

interface RoomDetailProps {
  roomId: number | null;
  open: boolean;
  onClose: () => void;
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

export const RoomDetail = ({ roomId, open, onClose }: RoomDetailProps) => {
  const { data: room, isLoading } = useRoom(roomId);

  if (!roomId) return null;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={700}
      title={
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white">
            <HomeOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isLoading ? 'Đang tải...' : room?.roomNo || 'Chi tiết phòng'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Thông tin chi tiết phòng</p>
          </div>
        </div>
      }
    >
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Spin size="large" />
        </div>
      ) : !room ? (
        <Empty description="Không tìm thấy thông tin phòng" />
      ) : (
        <div className="mt-4">
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Số phòng">
              <div className="flex items-center gap-2">
                <HomeOutlined className="text-blue-500" />
                <span className="font-semibold">{room.roomNo}</span>
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Tòa nhà">
              <div className="flex items-center gap-2">
                <BuildOutlined className="text-gray-400" />
                {room.buildingName}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Giá thuê">
              <div className="flex items-center gap-2">
                <DollarOutlined className="text-green-500" />
                <span className="font-semibold">{formatPrice(room.price)}/tháng</span>
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <Tag color={getStatusColor(room.status)} className="text-base py-1 px-3">
                {getStatusLabel(room.status)}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="ID">{room.id}</Descriptions.Item>
          </Descriptions>
        </div>
      )}
    </Modal>
  );
};
