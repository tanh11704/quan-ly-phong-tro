import { BuildOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Empty, Input, Pagination, Spin, message } from 'antd';
import { motion } from 'motion/react';
import { useMemo, useState } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import {
  useBuildings,
  useCreateBuilding,
  useDeleteBuilding,
  useUpdateBuilding,
} from '../api/buildingsApi';
import { BuildingCard } from '../components/BuildingCard';
import { BuildingDetail } from '../components/BuildingDetail';
import { BuildingForm } from '../components/BuildingForm';
import type {
  BuildingCreationRequest,
  BuildingResponse,
  BuildingUpdateRequest,
} from '../types/buildings';

const Buildings = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editBuilding, setEditBuilding] = useState<BuildingResponse | null>(null);
  const [selectedBuildingId, setSelectedBuildingId] = useState<number | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const { data: buildingsData, isLoading } = useBuildings(page, pageSize);
  const { mutateAsync: createBuilding } = useCreateBuilding();
  const { mutateAsync: updateBuilding } = useUpdateBuilding();
  const { mutateAsync: deleteBuilding } = useDeleteBuilding();

  const buildings = buildingsData?.content || [];
  const pageInfo = buildingsData?.page;

  // Filter buildings by search query (client-side filtering)
  const filteredBuildings = useMemo(() => {
    if (!searchQuery) return buildings;
    return buildings.filter((building) =>
      building.name.toLowerCase().includes(searchQuery.toLowerCase()),
    );
  }, [buildings, searchQuery]);

  const handleCreateBuilding = async (data: BuildingCreationRequest | BuildingUpdateRequest) => {
    try {
      if (editBuilding) {
        const response = await updateBuilding({ id: editBuilding.id, request: data });
        message.success(response.message || 'Cập nhật tòa nhà thành công!');
      } else {
        const response = await createBuilding(data as BuildingCreationRequest);
        message.success(response.message || 'Tạo tòa nhà thành công!');
      }
      setFormOpen(false);
      setEditBuilding(null);
    } catch (error) {
      message.error(
        getErrorMessage(
          error,
          editBuilding ? 'Cập nhật tòa nhà thất bại' : 'Tạo tòa nhà thất bại. Vui lòng thử lại.',
        ),
      );
    }
  };

  const handleEditBuilding = (building: BuildingResponse) => {
    setEditBuilding(building);
    setFormOpen(true);
  };

  const handleDeleteBuilding = async (id: number) => {
    try {
      const response = await deleteBuilding(id);
      message.success(response.message || 'Xóa tòa nhà thành công!');
    } catch (error) {
      message.error(getErrorMessage(error, 'Xóa tòa nhà thất bại. Vui lòng thử lại.'));
    }
  };

  const handleCardClick = (building: BuildingResponse) => {
    setSelectedBuildingId(building.id);
    setDetailOpen(true);
  };

  const handleFormClose = () => {
    setFormOpen(false);
    setEditBuilding(null);
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
                  <BuildOutlined className="text-3xl text-white" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-gray-800 mb-1">Quản lý tòa nhà</h1>
                  <p className="text-gray-500">Quản lý thông tin các tòa nhà</p>
                </div>
              </div>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setFormOpen(true)}
                size="large"
                className="bg-blue-500 hover:bg-blue-600 border-0"
              >
                Tạo tòa nhà mới
              </Button>
            </div>

            {/* Search */}
            <Input
              placeholder="Tìm kiếm tòa nhà..."
              prefix={<SearchOutlined className="text-gray-400" />}
              size="large"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="rounded-lg"
            />
          </div>
        </motion.div>

        {/* Buildings List */}
        <div className="mt-6">
          {isLoading ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Spin size="large" />
            </div>
          ) : filteredBuildings.length === 0 ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description={
                  searchQuery
                    ? 'Không tìm thấy tòa nhà nào'
                    : 'Chưa có tòa nhà nào. Hãy tạo tòa nhà đầu tiên!'
                }
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              >
                {!searchQuery && (
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setFormOpen(true)}
                    size="large"
                    className="bg-blue-500 hover:bg-blue-600 border-0"
                  >
                    Tạo tòa nhà đầu tiên
                  </Button>
                )}
              </Empty>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredBuildings.map((building, index) => (
                  <motion.div
                    key={building.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.05 }}
                  >
                    <BuildingCard
                      building={building}
                      onClick={() => handleCardClick(building)}
                      onEdit={handleEditBuilding}
                      onDelete={handleDeleteBuilding}
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
                    showTotal={(total) => `Tổng ${total} tòa nhà`}
                  />
                </div>
              )}
            </>
          )}
        </div>

        {/* Forms & Modals */}
        <BuildingForm
          open={formOpen}
          onClose={handleFormClose}
          onSubmit={handleCreateBuilding}
          initialValues={editBuilding || undefined}
          isEdit={!!editBuilding}
        />

        <BuildingDetail
          buildingId={selectedBuildingId}
          open={detailOpen}
          onClose={() => {
            setDetailOpen(false);
            setSelectedBuildingId(null);
          }}
        />
      </div>
    </div>
  );
};

export default Buildings;
