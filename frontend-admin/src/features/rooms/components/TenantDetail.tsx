import { CalendarOutlined, PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { Descriptions, Empty, Modal, Spin, Tag } from 'antd';
import { useTenant } from '../api/tenantsApi';

interface TenantDetailProps {
  tenantId: number | null;
  open: boolean;
  onClose: () => void;
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

export const TenantDetail = ({ tenantId, open, onClose }: TenantDetailProps) => {
  const { data: tenant, isLoading } = useTenant(tenantId);

  if (!tenantId) return null;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={600}
      title={
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white">
            <UserOutlined className="text-xl" />
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">
              {isLoading ? 'Đang tải...' : tenant?.name || 'Chi tiết khách thuê'}
            </h3>
            <p className="text-sm text-gray-500 mb-0">Thông tin chi tiết khách thuê</p>
          </div>
        </div>
      }
    >
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Spin size="large" />
        </div>
      ) : !tenant ? (
        <Empty description="Không tìm thấy thông tin khách thuê" />
      ) : (
        <div className="mt-4">
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Tên khách thuê">
              <div className="flex items-center gap-2">
                <UserOutlined className="text-blue-500" />
                <span className="font-semibold">{tenant.name}</span>
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Số điện thoại">
              <div className="flex items-center gap-2">
                <PhoneOutlined className="text-gray-400" />
                {tenant.phone || 'Chưa cập nhật'}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Số phòng">
              {tenant.roomNo || `Phòng ID: ${tenant.roomId}`}
            </Descriptions.Item>
            <Descriptions.Item label="Người đại diện hợp đồng">
              <Tag color={tenant.isContractHolder ? 'blue' : 'default'}>
                {tenant.isContractHolder ? 'Có' : 'Không'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Ngày bắt đầu">
              <div className="flex items-center gap-2">
                <CalendarOutlined className="text-blue-500" />
                {formatDate(tenant.startDate)}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Ngày kết thúc">
              <div className="flex items-center gap-2">
                <CalendarOutlined className="text-gray-400" />
                {tenant.endDate ? formatDate(tenant.endDate) : 'Đang thuê'}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <Tag color={tenant.endDate ? 'default' : 'green'}>
                {tenant.endDate ? 'Đã kết thúc' : 'Đang thuê'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="ID">{tenant.id}</Descriptions.Item>
          </Descriptions>
        </div>
      )}
    </Modal>
  );
};
