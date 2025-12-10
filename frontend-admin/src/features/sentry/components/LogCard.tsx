import {
  BugOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
  UserOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import { Badge, Card, Tag, Tooltip } from 'antd';
import { formatDistanceToNow } from 'date-fns';
import { motion } from 'motion/react';
import type { SentryIssue } from '../types/sentry';

interface LogCardProps {
  issue: SentryIssue;
  onClick: () => void;
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
    case 'debug':
      return 'default';
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
    case 'info':
      return <CheckCircleOutlined />;
    default:
      return <BugOutlined />;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'resolved':
      return 'success';
    case 'ignored':
      return 'default';
    case 'muted':
      return 'default';
    default:
      return 'error';
  }
};

export const LogCard = ({ issue, onClick }: LogCardProps) => {
  const levelColor = getLevelColor(issue.level);
  const statusColor = getStatusColor(issue.status);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      whileHover={{ scale: 1.02 }}
      className="cursor-pointer"
      onClick={onClick}
    >
      <Card
        className="mb-4 hover:shadow-lg transition-all duration-300 border-l-4"
        style={{
          borderLeftColor:
            issue.level === 'error' || issue.level === 'fatal'
              ? '#ef4444'
              : issue.level === 'warning'
                ? '#f59e0b'
                : '#3b82f6',
        }}
        bodyStyle={{ padding: '20px' }}
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-3">
              <Badge
                count={issue.count}
                overflowCount={999}
                style={{ backgroundColor: levelColor }}
              >
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
              </Badge>
              <div className="flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-800 mb-1 truncate">{issue.title}</h3>
                <p className="text-sm text-gray-500 truncate">{issue.culprit}</p>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2 mb-3">
              <Tag color={levelColor} icon={getLevelIcon(issue.level)}>
                {issue.level.toUpperCase()}
              </Tag>
              <Tag color={statusColor}>
                {issue.status === 'unresolved' ? 'Chưa xử lý' : 'Đã xử lý'}
              </Tag>
              {issue.metadata?.type && <Tag color="blue">{issue.metadata.type}</Tag>}
            </div>

            <div className="flex items-center gap-4 text-sm text-gray-500">
              <Tooltip title={new Date(issue.firstSeen).toLocaleString('vi-VN')}>
                <span className="flex items-center gap-1">
                  <ClockCircleOutlined />
                  {formatDistanceToNow(new Date(issue.firstSeen), {
                    addSuffix: true,
                  })}
                </span>
              </Tooltip>
              <span className="flex items-center gap-1">
                <UserOutlined />
                {issue.userCount} người dùng
              </span>
            </div>
          </div>

          <div className="flex flex-col items-end gap-2">
            <motion.div
              whileHover={{ scale: 1.1 }}
              className="p-2 rounded-lg bg-blue-50 hover:bg-blue-100 transition-colors"
            >
              <EyeOutlined className="text-blue-600 text-lg" />
            </motion.div>
            <div className="text-xs text-gray-400">ID: {issue.shortId}</div>
          </div>
        </div>
      </Card>
    </motion.div>
  );
};
