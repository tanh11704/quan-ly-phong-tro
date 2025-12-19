import {
  CameraOutlined,
  DropboxOutlined,
  EditOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { Button, Empty, Spin, Table, Tooltip } from 'antd';
import { useState } from 'react';
import { useUtilityReadingsByRoom } from '../api/utilityReadingsApi';
import type { UtilityReadingResponse } from '../types/utility-readings';
import { UtilityReadingForm } from './UtilityReadingForm';

interface UtilityReadingListProps {
  roomId: number | null;
}

const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  } catch {
    return dateString;
  }
};

const formatMonth = (month: string): string => {
  try {
    const [year, monthNum] = month.split('-');
    const date = new Date(parseInt(year), parseInt(monthNum) - 1);
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: 'long',
    }).format(date);
  } catch {
    return month;
  }
};

export const UtilityReadingList = ({ roomId }: UtilityReadingListProps) => {
  const [formOpen, setFormOpen] = useState(false);
  const [editReading, setEditReading] = useState<UtilityReadingResponse | null>(null);

  const { data: readings, isLoading } = useUtilityReadingsByRoom(roomId);

  const handleAddReading = () => {
    setEditReading(null);
    setFormOpen(true);
  };

  const handleEditReading = (reading: UtilityReadingResponse) => {
    setEditReading(reading);
    setFormOpen(true);
  };

  const handleFormClose = () => {
    setFormOpen(false);
    setEditReading(null);
  };

  if (!roomId) {
    return (
      <div className="py-8 text-center">
        <Empty description="Vui lòng chọn phòng để xem chỉ số điện nước" />
      </div>
    );
  }

  const columns = [
    {
      title: 'Tháng',
      key: 'month',
      render: (_: unknown, record: UtilityReadingResponse) => (
        <div className="font-semibold text-gray-800">{formatMonth(record.month)}</div>
      ),
    },
    {
      title: 'Chỉ số điện',
      key: 'electricIndex',
      render: (_: unknown, record: UtilityReadingResponse) => (
        <div className="flex items-center gap-2">
          <ThunderboltOutlined className="text-yellow-500" />
          <span>{record.electricIndex !== null ? `${record.electricIndex} kWh` : 'Chưa ghi'}</span>
        </div>
      ),
    },
    {
      title: 'Chỉ số nước',
      key: 'waterIndex',
      render: (_: unknown, record: UtilityReadingResponse) => (
        <div className="flex items-center gap-2">
          <DropboxOutlined className="text-blue-500" />
          <span>{record.waterIndex !== null ? `${record.waterIndex} m³` : 'Chưa ghi'}</span>
        </div>
      ),
    },
    {
      title: 'Ảnh chứng cứ',
      key: 'imageEvidence',
      render: (_: unknown, record: UtilityReadingResponse) =>
        record.imageEvidence ? (
          <Tooltip title="Xem ảnh">
            <a
              href={record.imageEvidence}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-500 hover:text-blue-600"
            >
              <CameraOutlined className="text-lg" />
            </a>
          </Tooltip>
        ) : (
          <span className="text-gray-400">-</span>
        ),
    },
    {
      title: 'Thời gian tạo',
      key: 'createdAt',
      render: (_: unknown, record: UtilityReadingResponse) => (
        <span className="text-sm text-gray-600">{formatDate(record.createdAt)}</span>
      ),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_: unknown, record: UtilityReadingResponse) => (
        <Button
          type="link"
          icon={<EditOutlined />}
          onClick={() => handleEditReading(record)}
          size="small"
        >
          Chỉnh sửa
        </Button>
      ),
    },
  ];

  return (
    <div className="mt-4">
      <div className="mb-4 flex items-center justify-between">
        <div className="text-sm text-gray-600">
          Tổng số bản ghi: <span className="font-semibold">{readings?.length || 0}</span>
        </div>
        <Button
          type="primary"
          icon={<ThunderboltOutlined />}
          onClick={handleAddReading}
          className="bg-blue-500 hover:bg-blue-600 border-0"
        >
          Ghi chỉ số mới
        </Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Spin size="large" />
        </div>
      ) : !readings || readings.length === 0 ? (
        <Empty
          description="Chưa có bản ghi chỉ số điện nước"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          className="py-8"
        >
          <Button
            type="primary"
            icon={<ThunderboltOutlined />}
            onClick={handleAddReading}
            className="bg-blue-500 hover:bg-blue-600 border-0 mt-4"
          >
            Ghi chỉ số đầu tiên
          </Button>
        </Empty>
      ) : (
        <div className="overflow-x-auto">
          <Table
            dataSource={readings}
            columns={columns}
            rowKey="id"
            pagination={false}
            className="utility-reading-table"
          />
        </div>
      )}

      <UtilityReadingForm
        open={formOpen}
        onClose={handleFormClose}
        roomId={roomId}
        initialValues={editReading || undefined}
        isEdit={!!editReading}
        readingId={editReading?.id}
      />
    </div>
  );
};
