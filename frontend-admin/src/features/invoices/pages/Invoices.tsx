import { DollarOutlined, FilterOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, DatePicker, Empty, Input, Pagination, Select, Spin, message } from 'antd';
import dayjs from 'dayjs';
import { motion } from 'motion/react';
import { useMemo, useState } from 'react';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useBuildings } from '../../buildings/api/buildingsApi';
import { useInvoices, usePayInvoice, useSendInvoiceEmail } from '../api/invoicesApi';
import { InvoiceCard } from '../components/InvoiceCard';
import { InvoiceDetail } from '../components/InvoiceDetail';
import { InvoiceForm } from '../components/InvoiceForm';
import type { InvoiceStatus } from '../types/invoices';
import { InvoiceStatus as InvoiceStatusEnum } from '../types/invoices';

const Invoices = () => {
  const [selectedBuildingId, setSelectedBuildingId] = useState<number | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [periodFilter, setPeriodFilter] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<number | null>(null);

  const { data: invoicesData, isLoading } = useInvoices(
    selectedBuildingId,
    page,
    pageSize,
    periodFilter,
    statusFilter,
  );
  const { data: buildingsData } = useBuildings(0, 100);
  const { mutateAsync: payInvoice } = usePayInvoice();
  const { mutateAsync: sendEmail } = useSendInvoiceEmail();

  const invoices = invoicesData?.content || [];
  const pageInfo = invoicesData?.page;
  const buildings = buildingsData?.content || [];

  // Filter invoices by search query (client-side filtering)
  const filteredInvoices = useMemo(() => {
    let result = invoices;
    if (searchQuery) {
      result = result.filter(
        (invoice) =>
          invoice.roomNo.toLowerCase().includes(searchQuery.toLowerCase()) ||
          invoice.tenantName.toLowerCase().includes(searchQuery.toLowerCase()),
      );
    }
    return result;
  }, [invoices, searchQuery]);

  const handlePayInvoice = async (id: number) => {
    try {
      const response = await payInvoice(id);
      message.success(response.message || 'Thanh toán hóa đơn thành công!');
    } catch (error) {
      message.error(getErrorMessage(error, 'Thanh toán hóa đơn thất bại. Vui lòng thử lại.'));
    }
  };

  const handleSendEmail = async (id: number) => {
    try {
      const response = await sendEmail(id);
      message.success(response.message || 'Gửi email hóa đơn thành công!');
    } catch (error) {
      message.error(getErrorMessage(error, 'Gửi email thất bại. Vui lòng thử lại.'));
    }
  };

  const handleCardClick = (invoiceId: number) => {
    setSelectedInvoiceId(invoiceId);
    setDetailOpen(true);
  };

  const handleFormClose = () => {
    setFormOpen(false);
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
                  <DollarOutlined className="text-3xl text-white" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-gray-800 mb-1">Quản lý hóa đơn</h1>
                  <p className="text-gray-500">Quản lý và theo dõi hóa đơn thanh toán</p>
                </div>
              </div>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setFormOpen(true)}
                size="large"
                className="bg-blue-500 hover:bg-blue-600 border-0"
                disabled={!selectedBuildingId}
              >
                Tạo hóa đơn
              </Button>
            </div>

            {/* Filters */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Input
                placeholder="Tìm kiếm số phòng, tên khách..."
                prefix={<SearchOutlined className="text-gray-400" />}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                size="large"
                className="rounded-lg"
              />

              <Select
                placeholder="Chọn tòa nhà"
                value={selectedBuildingId}
                onChange={setSelectedBuildingId}
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

              <DatePicker
                picker="month"
                placeholder="Chọn kỳ thanh toán"
                value={periodFilter ? dayjs(periodFilter + '-01') : null}
                onChange={(date) => setPeriodFilter(date ? date.format('YYYY-MM') : null)}
                size="large"
                className="w-full rounded-lg"
                allowClear
              />

              <Select
                placeholder="Chọn trạng thái"
                value={statusFilter}
                onChange={setStatusFilter}
                size="large"
                className="rounded-lg"
                allowClear
              >
                <Select.Option value={InvoiceStatusEnum.DRAFT}>
                  <span className="flex items-center gap-2">
                    <span>Nháp</span>
                  </span>
                </Select.Option>
                <Select.Option value={InvoiceStatusEnum.UNPAID}>
                  <span className="flex items-center gap-2">
                    <span>Chưa thanh toán</span>
                  </span>
                </Select.Option>
                <Select.Option value={InvoiceStatusEnum.PAID}>
                  <span className="flex items-center gap-2">
                    <span>Đã thanh toán</span>
                  </span>
                </Select.Option>
              </Select>
            </div>

            {(selectedBuildingId || periodFilter || statusFilter || searchQuery) && (
              <div className="mt-4 flex items-center gap-2">
                <Button
                  icon={<FilterOutlined />}
                  onClick={() => {
                    setPeriodFilter(null);
                    setStatusFilter(null);
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

        {/* Invoices List */}
        <div className="mt-6">
          {!selectedBuildingId ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description="Vui lòng chọn tòa nhà để xem danh sách hóa đơn"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            </div>
          ) : isLoading ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Spin size="large" />
            </div>
          ) : filteredInvoices.length === 0 ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description={
                  searchQuery || periodFilter || statusFilter
                    ? 'Không tìm thấy hóa đơn nào'
                    : 'Chưa có hóa đơn nào. Hãy tạo hóa đơn đầu tiên!'
                }
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              >
                {!searchQuery && !periodFilter && !statusFilter && (
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setFormOpen(true)}
                    className="bg-blue-500 hover:bg-blue-600 border-0 mt-4"
                  >
                    Tạo hóa đơn đầu tiên
                  </Button>
                )}
              </Empty>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 gap-4">
                {filteredInvoices.map((invoice) => (
                  <InvoiceCard
                    key={invoice.id}
                    invoice={invoice}
                    onClick={() => handleCardClick(invoice.id)}
                    onPay={handlePayInvoice}
                    onSendEmail={handleSendEmail}
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
                      `${range[0]}-${range[1]} của ${total} hóa đơn`
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

      {/* Invoice Form Modal */}
      <InvoiceForm open={formOpen} onClose={handleFormClose} buildingId={selectedBuildingId} />

      {/* Invoice Detail Modal */}
      <InvoiceDetail
        invoiceId={selectedInvoiceId}
        open={detailOpen}
        onClose={() => {
          setDetailOpen(false);
          setSelectedInvoiceId(null);
        }}
      />
    </div>
  );
};

export default Invoices;
