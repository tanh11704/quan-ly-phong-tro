import { BugOutlined, FilterOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Empty, Input, Select, Space, Spin, message } from 'antd';
import { motion } from 'motion/react';
import { useState } from 'react';
import { useSentryIssues } from '../api/sentryApi';
import { LogCard } from '../components/LogCard';
import { LogDetailModal } from '../components/LogDetailModal';
import type { SentryIssue } from '../types/sentry';

const { Search } = Input;
const { Option } = Select;

const SentryLogs = () => {
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<string | undefined>(undefined);
  const [level, setLevel] = useState<string | undefined>(undefined);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedIssue, setSelectedIssue] = useState<SentryIssue | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  const {
    data: logsData,
    isLoading,
    isError,
    refetch,
  } = useSentryIssues({
    page,
    pageSize: 20,
    status: status as 'unresolved' | 'resolved' | 'ignored' | 'muted' | undefined,
    level: level as 'error' | 'warning' | 'info' | 'debug' | 'fatal' | undefined,
    query: searchQuery || undefined,
  });

  const handleCardClick = (issue: SentryIssue) => {
    setSelectedIssue(issue);
    setModalOpen(true);
  };

  const handleReset = () => {
    setPage(1);
    setStatus(undefined);
    setLevel(undefined);
    setSearchQuery('');
    message.success('Đã reset bộ lọc');
  };

  const errorCount =
    logsData?.issues.filter((i) => i.level === 'error' || i.level === 'fatal').length || 0;
  const warningCount = logsData?.issues.filter((i) => i.level === 'warning').length || 0;
  const totalCount = logsData?.total || 0;

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-purple-50 p-6">
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
                <div className="w-16 h-16 bg-gradient-to-br from-red-500 to-orange-500 rounded-2xl flex items-center justify-center shadow-lg">
                  <BugOutlined className="text-3xl text-white" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-gray-800 mb-1">Sentry Logs</h1>
                  <p className="text-gray-500">Quản lý và theo dõi lỗi hệ thống</p>
                </div>
              </div>
              <Button
                type="primary"
                icon={<ReloadOutlined />}
                onClick={() => refetch()}
                loading={isLoading}
                size="large"
                className="bg-gradient-to-r from-blue-500 to-purple-600 border-0"
              >
                Làm mới
              </Button>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.1 }}
                className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-4 border border-red-200"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-red-600 font-medium mb-1">Lỗi nghiêm trọng</p>
                    <p className="text-3xl font-bold text-red-700">{errorCount}</p>
                  </div>
                  <div className="w-12 h-12 bg-red-500 rounded-lg flex items-center justify-center">
                    <BugOutlined className="text-2xl text-white" />
                  </div>
                </div>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.2 }}
                className="bg-gradient-to-br from-orange-50 to-orange-100 rounded-xl p-4 border border-orange-200"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-orange-600 font-medium mb-1">Cảnh báo</p>
                    <p className="text-3xl font-bold text-orange-700">{warningCount}</p>
                  </div>
                  <div className="w-12 h-12 bg-orange-500 rounded-lg flex items-center justify-center">
                    <FilterOutlined className="text-2xl text-white" />
                  </div>
                </div>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.3 }}
                className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-4 border border-blue-200"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-blue-600 font-medium mb-1">Tổng số logs</p>
                    <p className="text-3xl font-bold text-blue-700">{totalCount}</p>
                  </div>
                  <div className="w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center">
                    <BugOutlined className="text-2xl text-white" />
                  </div>
                </div>
              </motion.div>
            </div>

            {/* Filters */}
            <div className="bg-gray-50 rounded-xl p-4">
              <Space direction="vertical" className="w-full" size="middle">
                <div className="flex flex-wrap items-center gap-3">
                  <Search
                    placeholder="Tìm kiếm logs..."
                    allowClear
                    enterButton={<SearchOutlined />}
                    size="large"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onSearch={() => refetch()}
                    className="flex-1 min-w-[200px]"
                  />
                  <Select
                    placeholder="Trạng thái"
                    allowClear
                    size="large"
                    value={status}
                    onChange={setStatus}
                    className="w-[150px]"
                  >
                    <Option value="unresolved">Chưa xử lý</Option>
                    <Option value="resolved">Đã xử lý</Option>
                    <Option value="ignored">Đã bỏ qua</Option>
                    <Option value="muted">Đã tắt tiếng</Option>
                  </Select>
                  <Select
                    placeholder="Mức độ"
                    allowClear
                    size="large"
                    value={level}
                    onChange={setLevel}
                    className="w-[150px]"
                  >
                    <Option value="fatal">Fatal</Option>
                    <Option value="error">Error</Option>
                    <Option value="warning">Warning</Option>
                    <Option value="info">Info</Option>
                    <Option value="debug">Debug</Option>
                  </Select>
                  <Button
                    onClick={handleReset}
                    size="large"
                    className="bg-gray-200 hover:bg-gray-300"
                  >
                    Reset
                  </Button>
                </div>
              </Space>
            </div>
          </div>
        </motion.div>

        {/* Logs List */}
        <div className="mt-6">
          {isLoading ? (
            <div className="flex justify-center items-center py-20">
              <Spin size="large" />
            </div>
          ) : isError ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty
                description="Không thể tải logs. Vui lòng thử lại sau."
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              >
                <Button type="primary" onClick={() => refetch()}>
                  Thử lại
                </Button>
              </Empty>
            </div>
          ) : !logsData || logsData.issues.length === 0 ? (
            <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
              <Empty description="Không có logs nào" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            </div>
          ) : (
            <div className="space-y-4">
              {logsData.issues.map((issue, index) => (
                <motion.div
                  key={issue.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05 }}
                >
                  <LogCard issue={issue} onClick={() => handleCardClick(issue)} />
                </motion.div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {logsData && logsData.total > logsData.pageSize && (
            <div className="mt-6 flex justify-center">
              <Space>
                <Button disabled={page === 1} onClick={() => setPage(page - 1)}>
                  Trước
                </Button>
                <span className="text-gray-600">
                  Trang {page} / {Math.ceil(logsData.total / logsData.pageSize)}
                </span>
                <Button
                  disabled={page >= Math.ceil(logsData.total / logsData.pageSize)}
                  onClick={() => setPage(page + 1)}
                >
                  Sau
                </Button>
              </Space>
            </div>
          )}
        </div>

        {/* Detail Modal */}
        <LogDetailModal
          issue={selectedIssue}
          open={modalOpen}
          onClose={() => {
            setModalOpen(false);
            setSelectedIssue(null);
          }}
        />
      </div>
    </div>
  );
};

export default SentryLogs;
