import { CalendarOutlined, PhoneOutlined, PlusOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Empty, Pagination, Popconfirm, Spin, Table, Tag, Tooltip, message } from 'antd';
import { useState } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useRoomTenants } from '../api/roomsApi';
import { useEndTenantContract } from '../api/tenantsApi';
import type { TenantResponse } from '../types/tenants';
import { TenantDetail } from './TenantDetail';
import { TenantForm } from './TenantForm';

interface RentalHistoryProps {
  roomId: number | null;
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

const calculateDuration = (startDate: string, endDate: string | null): string => {
  try {
    const start = new Date(startDate);
    const end = endDate ? new Date(endDate) : new Date();
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    const months = Math.floor(diffDays / 30);
    const days = diffDays % 30;

    if (months > 0 && days > 0) {
      return `${months} tháng ${days} ngày`;
    } else if (months > 0) {
      return `${months} tháng`;
    } else {
      return `${days} ngày`;
    }
  } catch {
    return 'N/A';
  }
};

export const RentalHistory = ({ roomId }: RentalHistoryProps) => {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [formOpen, setFormOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedTenantId, setSelectedTenantId] = useState<number | null>(null);
  const [editTenant, setEditTenant] = useState<TenantResponse | null>(null);

  const { data: tenantsData, isLoading } = useRoomTenants(roomId, page, pageSize);
  const { mutateAsync: endContract, isPending: isEndingContract } = useEndTenantContract();

  const tenants = tenantsData?.content || [];
  const pageInfo = tenantsData?.page;

  if (!roomId) {
    return (
      <div className="py-8 text-center">
        <Empty description="Vui lòng chọn phòng để xem lịch sử thuê" />
      </div>
    );
  }

  const handleAddTenant = () => {
    setEditTenant(null);
    setFormOpen(true);
  };

  const handleCreateTenant = async () => {
    // Form đã tự gọi API và invalidate queries
    // Chỉ cần đóng form
    setFormOpen(false);
    setEditTenant(null);
  };

  const handleEndContract = async (id: number) => {
    try {
      const response = await endContract(id);
      message.success(response.message || 'Kết thúc hợp đồng thành công!');
    } catch (error) {
      message.error(getErrorMessage(error, 'Kết thúc hợp đồng thất bại. Vui lòng thử lại.'));
    }
  };

  const handleRowClick = (record: TenantResponse) => {
    setSelectedTenantId(record.id);
    setDetailOpen(true);
  };

  const columns = [
    {
      title: 'Khách thuê',
      key: 'name',
      render: (_: unknown, record: TenantResponse) => (
        <div
          className="flex items-center gap-2 cursor-pointer hover:opacity-80 transition-opacity"
          onClick={() => handleRowClick(record)}
        >
          <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-xs font-semibold shrink-0">
            {record.name.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-800 truncate">{record.name}</span>
              {record.isContractHolder && (
                <Tooltip title="Người đại diện hợp đồng">
                  <Tag color="blue" className="shrink-0">
                    Đại diện
                  </Tag>
                </Tooltip>
              )}
            </div>
            <div className="flex items-center gap-1 text-sm text-gray-500">
              <PhoneOutlined className="text-xs" />
              <span className="truncate">{record.phone}</span>
            </div>
          </div>
        </div>
      ),
    },
    {
      title: 'Thời gian thuê',
      key: 'period',
      render: (_: unknown, record: TenantResponse) => (
        <div className="space-y-1">
          <div className="flex items-center gap-2 text-sm">
            <CalendarOutlined className="text-blue-500 text-xs" />
            <span className="text-gray-600">
              <span className="font-medium">Từ:</span> {formatDate(record.startDate)}
            </span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <CalendarOutlined className="text-gray-400 text-xs" />
            <span className="text-gray-600">
              <span className="font-medium">Đến:</span>{' '}
              {record.endDate ? formatDate(record.endDate) : 'Đang thuê'}
            </span>
          </div>
          {record.endDate && (
            <div className="text-xs text-gray-500 mt-1">
              Thời gian: {calculateDuration(record.startDate, record.endDate)}
            </div>
          )}
        </div>
      ),
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: (_: unknown, record: TenantResponse) => (
        <Tag color={record.endDate ? 'default' : 'green'} className="text-sm">
          {record.endDate ? 'Đã kết thúc' : 'Đang thuê'}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_: unknown, record: TenantResponse) => (
        <div className="flex gap-2">
          <Button
            type="link"
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              setEditTenant(record);
              setFormOpen(true);
            }}
          >
            Chỉnh sửa
          </Button>
          {!record.endDate && (
            <Popconfirm
              title="Kết thúc hợp đồng"
              description="Bạn có chắc chắn muốn kết thúc hợp đồng của khách thuê này?"
              onConfirm={(e) => {
                e?.stopPropagation();
                handleEndContract(record.id);
              }}
              onCancel={(e) => e?.stopPropagation()}
              okText="Kết thúc"
              cancelText="Hủy"
              okButtonProps={{ danger: true, loading: isEndingContract }}
            >
              <Button
                type="link"
                danger
                size="small"
                loading={isEndingContract}
                onClick={(e) => e.stopPropagation()}
              >
                Kết thúc hợp đồng
              </Button>
            </Popconfirm>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="mt-4">
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Spin size="large" />
        </div>
      ) : (
        <>
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <UserOutlined />
              <span>
                Tổng số khách thuê:{' '}
                <span className="font-semibold">{pageInfo?.totalElements || 0}</span>
              </span>
            </div>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleAddTenant}
              className="bg-blue-500 hover:bg-blue-600 border-0"
            >
              Thêm khách thuê
            </Button>
          </div>

          {tenants.length === 0 ? (
            <Empty
              description="Chưa có lịch sử thuê"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              className="py-8"
            >
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleAddTenant}
                className="bg-blue-500 hover:bg-blue-600 border-0 mt-4"
              >
                Thêm khách thuê đầu tiên
              </Button>
            </Empty>
          ) : (
            <>
              <div className="overflow-x-auto">
                <Table
                  dataSource={tenants}
                  columns={columns}
                  rowKey="id"
                  pagination={false}
                  className="rental-history-table"
                />
              </div>

              {pageInfo && pageInfo.totalPages > 1 && (
                <div className="flex justify-center mt-4">
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
        </>
      )}

      {/* Tenant Form Modal */}
      <TenantForm
        open={formOpen}
        onClose={() => {
          setFormOpen(false);
          setEditTenant(null);
        }}
        onSubmit={handleCreateTenant}
        roomId={roomId}
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
