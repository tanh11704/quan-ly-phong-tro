import { FilterOutlined, HomeOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Empty, Input, Pagination, Select, Spin, Tag, message } from 'antd';
import { motion } from 'motion/react';
import { useMemo, useState } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useBuildingRooms, useBuildings } from '../../buildings/api/buildingsApi';
import { useCreateRoom, useDeleteRoom, useUpdateRoom } from '../api/roomsApi';
import { RoomCard } from '../components/RoomCard';
import { RoomDetail } from '../components/RoomDetail';
import { RoomForm } from '../components/RoomForm';
import type { RoomCreationRequest, RoomResponse, RoomUpdateRequest } from '../types/rooms';
import { RoomStatus } from '../types/rooms';

const { Option } = Select;

const Rooms = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedBuildingId, setSelectedBuildingId] = useState<number | undefined>(undefined);
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editRoom, setEditRoom] = useState<RoomResponse | null>(null);
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);

  // Nếu không chọn building, không fetch rooms
  const { data: roomsData, isLoading } = useBuildingRooms(
    selectedBuildingId || null,
    page,
    pageSize,
    undefined,
    statusFilter || undefined,
  );
  const { data: buildingsData } = useBuildings(0, 100);
  const { mutateAsync: createRoom } = useCreateRoom();
  const { mutateAsync: updateRoom } = useUpdateRoom();
  const { mutateAsync: deleteRoom } = useDeleteRoom();

  const rooms = roomsData?.content || [];
  const pageInfo = roomsData?.page;
  const buildings = buildingsData?.content || [];

  // Filter rooms by search query (client-side filtering)
  const filteredRooms = useMemo(() => {
    let result = rooms;
    if (searchQuery) {
      result = result.filter((room) =>
        room.roomNo.toLowerCase().includes(searchQuery.toLowerCase()),
      );
    }
    // Status filter đã được xử lý ở API level
    return result;
  }, [rooms, searchQuery]);

  const handleCreateRoom = async (data: RoomCreationRequest | RoomUpdateRequest) => {
    try {
      if (editRoom) {
        const response = await updateRoom({ id: editRoom.id, request: data });
        message.success(response.message || 'Cập nhật phòng thành công!');
      } else {
        const response = await createRoom(data as RoomCreationRequest);
        message.success(response.message || 'Tạo phòng thành công!');
      }
      setFormOpen(false);
      setEditRoom(null);
    } catch (error) {
      message.error(
        getErrorMessage(
          error,
          editRoom ? 'Cập nhật phòng thất bại' : 'Tạo phòng thất bại. Vui lòng thử lại.',
        ),
      );
    }
  };

  const handleEditRoom = (room: RoomResponse) => {
    setEditRoom(room);
    setFormOpen(true);
  };

  const handleDeleteRoom = async (id: number) => {
    try {
      const response = await deleteRoom(id);
      message.success(response.message || 'Xóa phòng thành công!');
    } catch (error) {
      message.error(getErrorMessage(error, 'Xóa phòng thất bại. Vui lòng thử lại.'));
    }
  };

  const handleCardClick = (room: RoomResponse) => {
    setSelectedRoomId(room.id);
    setDetailOpen(true);
  };

  const handleFormClose = () => {
    setFormOpen(false);
    setEditRoom(null);
  };

  const handleFilterReset = () => {
    setSelectedBuildingId(undefined);
    setStatusFilter(undefined);
    setSearchQuery('');
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <div className="bg-white rounded-2xl shadow-lg p-6 border border-gray-100">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 bg-blue-500 rounded-2xl flex items-center justify-center shadow-lg">
                  <HomeOutlined className="text-3xl text-white" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-gray-800 mb-1">Quản lý phòng</h1>
                  <p className="text-gray-500">Quản lý thông tin các phòng</p>
                </div>
              </div>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setFormOpen(true)}
                size="large"
                className="bg-blue-500 hover:bg-blue-600 border-0"
              >
                Tạo phòng mới
              </Button>
            </div>

            {/* Filters */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Input
                placeholder="Tìm kiếm số phòng..."
                prefix={<SearchOutlined className="text-gray-400" />}
                size="large"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="rounded-lg"
              />
              <Select
                placeholder="Chọn tòa nhà"
                size="large"
                className="rounded-lg"
                value={selectedBuildingId}
                onChange={setSelectedBuildingId}
                allowClear
              >
                {buildings.map((building) => (
                  <Option key={building.id} value={building.id}>
                    {building.name}
                  </Option>
                ))}
              </Select>
              <Select
                placeholder="Chọn trạng thái"
                size="large"
                className="rounded-lg"
                value={statusFilter}
                onChange={setStatusFilter}
                allowClear
              >
                <Option value={RoomStatus.VACANT}>
                  <Tag color="green">Trống</Tag>
                </Option>
                <Option value={RoomStatus.OCCUPIED}>
                  <Tag color="red">Đã thuê</Tag>
                </Option>
                <Option value={RoomStatus.MAINTENANCE}>
                  <Tag color="orange">Bảo trì</Tag>
                </Option>
              </Select>
              {(selectedBuildingId || statusFilter || searchQuery) && (
                <Button
                  icon={<FilterOutlined />}
                  onClick={handleFilterReset}
                  size="large"
                  className="rounded-lg"
                >
                  Xóa bộ lọc
                </Button>
              )}
            </div>
          </div>
        </motion.div>

        {/* Rooms List */}
        <div className="mt-6">
          {!selectedBuildingId ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description="Vui lòng chọn tòa nhà để xem danh sách phòng"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            </div>
          ) : isLoading ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Spin size="large" />
            </div>
          ) : filteredRooms.length === 0 ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description={
                  searchQuery || statusFilter
                    ? 'Không tìm thấy phòng nào'
                    : 'Chưa có phòng nào trong tòa nhà này. Hãy tạo phòng đầu tiên!'
                }
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              >
                {!searchQuery && !statusFilter && (
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setFormOpen(true)}
                    size="large"
                    className="bg-blue-500 hover:bg-blue-600 border-0"
                  >
                    Tạo phòng đầu tiên
                  </Button>
                )}
              </Empty>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredRooms.map((room, index) => (
                  <motion.div
                    key={room.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.05 }}
                  >
                    <RoomCard
                      room={room}
                      onClick={() => handleCardClick(room)}
                      onEdit={handleEditRoom}
                      onDelete={handleDeleteRoom}
                    />
                  </motion.div>
                ))}
              </div>
              {pageInfo && pageInfo.totalPages > 1 && (
                <div className="flex justify-center mt-6">
                  <Pagination
                    current={pageInfo.page + 1}
                    total={pageInfo.totalElements}
                    pageSize={pageInfo.size}
                    onChange={(newPage, newSize) => {
                      setPage(newPage - 1);
                      setPageSize(newSize);
                    }}
                    showSizeChanger
                    showTotal={(total) => `Tổng ${total} phòng`}
                  />
                </div>
              )}
            </>
          )}
        </div>

        {/* Forms & Modals */}
        <RoomForm
          open={formOpen}
          onClose={handleFormClose}
          onSubmit={handleCreateRoom}
          initialValues={
            editRoom
              ? {
                  roomNo: editRoom.roomNo,
                  price: editRoom.price,
                  status: editRoom.status as RoomStatus,
                }
              : undefined
          }
          isEdit={!!editRoom}
          defaultBuildingId={selectedBuildingId || undefined}
        />

        <RoomDetail
          roomId={selectedRoomId}
          open={detailOpen}
          onClose={() => {
            setDetailOpen(false);
            setSelectedRoomId(null);
          }}
        />
      </div>
    </div>
  );
};

export default Rooms;
