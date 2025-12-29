import { FilterOutlined, PlusOutlined, SearchOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Empty, Input, Pagination, Select, Spin, Tag, message } from 'antd';
import { motion } from 'motion/react';
import { useMemo, useState } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useBuildingRooms, useBuildings } from '../../buildings/api/buildingsApi';
import { useEndTenantContract, useTenants, useUpdateTenant } from '../../rooms/api/tenantsApi';
import { TenantDetail } from '../../rooms/components/TenantDetail';
import { TenantForm } from '../../rooms/components/TenantForm';
import type {
  TenantCreationRequest,
  TenantResponse,
  TenantUpdateRequest,
} from '../../rooms/types/tenants';
import { TenantCard } from '../components/TenantCard';

const Tenants = () => {
  const [selectedBuildingId, setSelectedBuildingId] = useState<number | null>(null);
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null);
  const [activeFilter, setActiveFilter] = useState<boolean | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [searchQuery, setSearchQuery] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedTenantId, setSelectedTenantId] = useState<number | null>(null);
  const [editTenant, setEditTenant] = useState<TenantResponse | null>(null);

  const { data: tenantsData, isLoading } = useTenants(
    page,
    pageSize,
    selectedBuildingId,
    selectedRoomId,
    activeFilter,
  );
  const { data: buildingsData } = useBuildings(0, 100);
  const { data: roomsData } = useBuildingRooms(selectedBuildingId, 0, 100);
  const { mutateAsync: updateTenant } = useUpdateTenant();
  const { mutateAsync: endContract } = useEndTenantContract();

  const tenants = tenantsData?.content || [];
  const pageInfo = tenantsData?.page;
  const buildings = buildingsData?.content || [];
  const rooms = roomsData?.content || [];

  // Filter tenants by search query (client-side filtering)
  const filteredTenants = useMemo(() => {
    let result = tenants;
    if (searchQuery) {
      result = result.filter(
        (tenant) =>
          tenant.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          tenant.roomNo?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          tenant.phone?.toLowerCase().includes(searchQuery.toLowerCase()),
      );
    }
    return result;
  }, [tenants, searchQuery]);

  const handleAddTenant = () => {
    setEditTenant(null);
    setFormOpen(true);
  };

  const handleEditTenant = (tenant: TenantResponse) => {
    setEditTenant(tenant);
    setFormOpen(true);
  };

  const handleUpdateTenant = async (
    data: TenantUpdateRequest | TenantCreationRequest,
  ): Promise<void> => {
    if (editTenant) {
      try {
        const updateData = data as TenantUpdateRequest;
        const response = await updateTenant({ id: editTenant.id, request: updateData });
        message.success(response.message || 'Cập nhật thông tin khách thuê thành công!');
        setFormOpen(false);
        setEditTenant(null);
      } catch (error) {
        message.error(getErrorMessage(error, 'Cập nhật thông tin khách thuê thất bại.'));
      }
    }
  };

  const handleEndContract = async (id: number) => {
    try {
      const response = await endContract(id);
      message.success(response.message || 'Kết thúc hợp đồng thành công!');
    } catch (error) {
      message.error(getErrorMessage(error, 'Kết thúc hợp đồng thất bại. Vui lòng thử lại.'));
    }
  };

  const handleCardClick = (tenantId: number) => {
    setSelectedTenantId(tenantId);
    setDetailOpen(true);
  };

  const handleFormClose = () => {
    setFormOpen(false);
    setEditTenant(null);
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
                  <UserOutlined className="text-3xl text-white" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-gray-800 mb-1">Quản lý khách thuê</h1>
                  <p className="text-gray-500">Quản lý thông tin khách thuê</p>
                </div>
              </div>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleAddTenant}
                size="large"
                className="bg-blue-500 hover:bg-blue-600 border-0"
                disabled={!selectedRoomId}
              >
                Thêm khách thuê
              </Button>
            </div>

            {/* Filters */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Input
                placeholder="Tìm kiếm tên, số phòng, SĐT..."
                prefix={<SearchOutlined className="text-gray-400" />}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                size="large"
                className="rounded-lg"
              />

              <Select
                placeholder="Chọn tòa nhà"
                value={selectedBuildingId}
                onChange={(value) => {
                  setSelectedBuildingId(value);
                  setSelectedRoomId(null);
                }}
                size="large"
                className="rounded-lg"
                showSearch
                optionFilterProp="children"
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={buildings.map((building) => ({
                  value: building.id,
                  label: building.name,
                }))}
                allowClear
              />

              <Select
                placeholder="Chọn phòng"
                value={selectedRoomId}
                onChange={setSelectedRoomId}
                size="large"
                className="rounded-lg"
                disabled={!selectedBuildingId}
                showSearch
                optionFilterProp="children"
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={rooms.map((room) => ({
                  value: room.id,
                  label: room.roomNo,
                }))}
                allowClear
              />

              <Select
                placeholder="Chọn trạng thái"
                value={activeFilter}
                onChange={setActiveFilter}
                size="large"
                className="rounded-lg"
                allowClear
              >
                <Select.Option value={true}>
                  <Tag color="green">Đang thuê</Tag>
                </Select.Option>
                <Select.Option value={false}>
                  <Tag color="default">Đã kết thúc</Tag>
                </Select.Option>
              </Select>
            </div>

            {(selectedBuildingId || selectedRoomId || activeFilter !== null || searchQuery) && (
              <div className="mt-4 flex items-center gap-2">
                <Button
                  icon={<FilterOutlined />}
                  onClick={() => {
                    setSelectedBuildingId(null);
                    setSelectedRoomId(null);
                    setActiveFilter(null);
                    setSearchQuery('');
                  }}
                  size="small"
                >
                  Xóa bộ lọc
                </Button>
              </div>
            )}
          </div>
        </motion.div>

        {/* Tenants List */}
        <div className="mt-6">
          {isLoading ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Spin size="large" />
            </div>
          ) : filteredTenants.length === 0 ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description={
                  searchQuery || selectedBuildingId || selectedRoomId || activeFilter !== null
                    ? 'Không tìm thấy khách thuê nào'
                    : 'Chưa có khách thuê nào. Hãy thêm khách thuê đầu tiên!'
                }
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              >
                {!searchQuery &&
                  !selectedBuildingId &&
                  !selectedRoomId &&
                  activeFilter === null && (
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={handleAddTenant}
                      className="bg-blue-500 hover:bg-blue-600 border-0 mt-4"
                      disabled={!selectedRoomId}
                    >
                      Thêm khách thuê đầu tiên
                    </Button>
                  )}
              </Empty>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 gap-4">
                {filteredTenants.map((tenant) => (
                  <TenantCard
                    key={tenant.id}
                    tenant={tenant}
                    onClick={() => handleCardClick(tenant.id)}
                    onEdit={handleEditTenant}
                    onEndContract={handleEndContract}
                  />
                ))}
              </div>

              {pageInfo && pageInfo.totalPages > 1 && (
                <div className="flex justify-center mt-6">
                  <Pagination
                    current={page + 1}
                    total={pageInfo.totalElements}
                    pageSize={pageSize}
                    showSizeChanger
                    showTotal={(total: number, range: [number, number]) =>
                      `${range[0]}-${range[1]} của ${total} khách thuê`
                    }
                    onChange={(newPage: number, newPageSize: number) => {
                      setPage(newPage - 1);
                      setPageSize(newPageSize);
                    }}
                    onShowSizeChange={(_current: number, size: number) => {
                      setPage(0);
                      setPageSize(size);
                    }}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Tenant Form Modal */}
      <TenantForm
        open={formOpen}
        onClose={handleFormClose}
        onSubmit={async (data) => {
          await handleUpdateTenant(data);
        }}
        roomId={editTenant?.roomId || selectedRoomId}
        isEdit={!!editTenant}
        tenantId={editTenant?.id}
        initialValues={editTenant || undefined}
      />

      {/* Tenant Detail Modal */}
      <TenantDetail
        tenantId={selectedTenantId}
        open={detailOpen}
        onClose={() => {
          setDetailOpen(false);
          setSelectedTenantId(null);
        }}
      />
    </div>
  );
};

export default Tenants;
