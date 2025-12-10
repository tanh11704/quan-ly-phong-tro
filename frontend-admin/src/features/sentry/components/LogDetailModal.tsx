import {
  BugOutlined,
  CloseCircleOutlined,
  CopyOutlined,
  DesktopOutlined,
  GlobalOutlined,
  MobileOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import { Button, Descriptions, Modal, Tabs, Tag, message } from 'antd';
import { motion } from 'motion/react';
import { useSentryIssueEvents } from '../api/sentryApi';
import type { SentryIssue } from '../types/sentry';

interface LogDetailModalProps {
  issue: SentryIssue | null;
  open: boolean;
  onClose: () => void;
}

const getLevelColor = (level: string) => {
  switch (level) {
    case 'fatal':
    case 'error':
      return 'red';
    case 'warning':
      return 'orange';
    case 'info':
      return 'blue';
    default:
      return 'default';
  }
};

const getLevelIcon = (level: string) => {
  switch (level) {
    case 'fatal':
    case 'error':
      return <CloseCircleOutlined />;
    case 'warning':
      return <WarningOutlined />;
    default:
      return <BugOutlined />;
  }
};

export const LogDetailModal = ({ issue, open, onClose }: LogDetailModalProps) => {
  const { data: events, isLoading } = useSentryIssueEvents(issue?.id || '', 5);

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('Đã sao chép!');
  };

  if (!issue) return null;

  const levelColor = getLevelColor(issue.level);

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={900}
      title={
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-lg flex items-center justify-center text-white"
            style={{
              backgroundColor:
                issue.level === 'error' || issue.level === 'fatal'
                  ? '#ef4444'
                  : issue.level === 'warning'
                    ? '#f59e0b'
                    : '#3b82f6',
            }}
          >
            {getLevelIcon(issue.level)}
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-0">{issue.title}</h3>
            <p className="text-sm text-gray-500 mb-0">{issue.culprit}</p>
          </div>
        </div>
      }
    >
      <div className="mt-4">
        <Tabs
          items={[
            {
              key: 'details',
              label: 'Chi tiết',
              children: (
                <div className="space-y-4">
                  <Descriptions bordered column={2} size="small">
                    <Descriptions.Item label="Mức độ">
                      <Tag color={levelColor} icon={getLevelIcon(issue.level)}>
                        {issue.level.toUpperCase()}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="Trạng thái">
                      <Tag color={issue.status === 'unresolved' ? 'error' : 'success'}>
                        {issue.status === 'unresolved' ? 'Chưa xử lý' : 'Đã xử lý'}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="Số lần xảy ra">
                      <span className="font-semibold">{issue.count}</span>
                    </Descriptions.Item>
                    <Descriptions.Item label="Số người dùng bị ảnh hưởng">
                      <span className="font-semibold">{issue.userCount}</span>
                    </Descriptions.Item>
                    <Descriptions.Item label="Lần đầu xuất hiện">
                      {new Date(issue.firstSeen).toLocaleString('vi-VN')}
                    </Descriptions.Item>
                    <Descriptions.Item label="Lần cuối xuất hiện">
                      {new Date(issue.lastSeen).toLocaleString('vi-VN')}
                    </Descriptions.Item>
                    {issue.metadata?.type && (
                      <Descriptions.Item label="Loại lỗi">{issue.metadata.type}</Descriptions.Item>
                    )}
                    {issue.metadata?.filename && (
                      <Descriptions.Item label="File">
                        <div className="flex items-center gap-2">
                          <code className="text-xs bg-gray-100 px-2 py-1 rounded">
                            {issue.metadata.filename}
                          </code>
                          <Button
                            type="text"
                            size="small"
                            icon={<CopyOutlined />}
                            onClick={() => handleCopy(issue.metadata!.filename!)}
                          />
                        </div>
                      </Descriptions.Item>
                    )}
                    {issue.metadata?.function && (
                      <Descriptions.Item label="Function" span={2}>
                        <code className="text-xs bg-gray-100 px-2 py-1 rounded">
                          {issue.metadata.function}
                        </code>
                      </Descriptions.Item>
                    )}
                  </Descriptions>

                  {issue.permalink && (
                    <div className="mt-4">
                      <Button
                        type="primary"
                        href={issue.permalink}
                        target="_blank"
                        icon={<GlobalOutlined />}
                      >
                        Xem trên Sentry
                      </Button>
                    </div>
                  )}
                </div>
              ),
            },
            {
              key: 'events',
              label: `Sự kiện gần đây (${events?.length || 0})`,
              children: (
                <div className="space-y-3">
                  {isLoading ? (
                    <div className="text-center py-8 text-gray-500">Đang tải...</div>
                  ) : events && events.length > 0 ? (
                    events.map((eventData, index) => {
                      const event = eventData.event;
                      return (
                        <motion.div
                          key={event.id}
                          initial={{ opacity: 0, x: -20 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: index * 0.1 }}
                          className="border rounded-lg p-4 bg-gray-50"
                        >
                          <div className="flex items-start justify-between mb-2">
                            <div>
                              <p className="font-semibold text-gray-800">
                                {event.message || 'Không có message'}
                              </p>
                              <p className="text-xs text-gray-500 mt-1">
                                {new Date(event.timestamp).toLocaleString('vi-VN')}
                              </p>
                            </div>
                            <Tag color={getLevelColor(event.level)}>
                              {event.level.toUpperCase()}
                            </Tag>
                          </div>

                          {event.user && (
                            <div className="mt-2 pt-2 border-t border-gray-200">
                              <p className="text-xs text-gray-600">
                                <strong>User:</strong>{' '}
                                {event.user.email || event.user.username || event.user.id || 'N/A'}
                              </p>
                            </div>
                          )}

                          {event.contexts?.browser && (
                            <div className="mt-2 flex items-center gap-2">
                              <DesktopOutlined className="text-gray-400" />
                              <span className="text-xs text-gray-600">
                                {event.contexts.browser.name} {event.contexts.browser.version}
                              </span>
                            </div>
                          )}

                          {event.contexts?.os && (
                            <div className="mt-1 flex items-center gap-2">
                              <MobileOutlined className="text-gray-400" />
                              <span className="text-xs text-gray-600">
                                {event.contexts.os.name} {event.contexts.os.version}
                              </span>
                            </div>
                          )}

                          {event.exception && (
                            <div className="mt-3 pt-3 border-t border-gray-200">
                              <p className="text-xs font-semibold text-red-600 mb-1">Exception:</p>
                              <code className="text-xs bg-red-50 text-red-800 p-2 rounded block">
                                {event.exception.values[0]?.type}:{' '}
                                {event.exception.values[0]?.value}
                              </code>
                            </div>
                          )}
                        </motion.div>
                      );
                    })
                  ) : (
                    <div className="text-center py-8 text-gray-500">Không có sự kiện nào</div>
                  )}
                </div>
              ),
            },
          ]}
        />
      </div>
    </Modal>
  );
};
