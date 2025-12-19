import { BellOutlined, MenuOutlined, SearchOutlined, UserOutlined } from '@ant-design/icons';
import { Dropdown, Spin } from 'antd';
import { motion } from 'framer-motion';
import React from 'react';
import { useMyInfo } from '../../features/auth/api/usersApi';
import { useAuth } from '../../features/auth/context';

interface HeaderProps {
  onMenuClick?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  const { logout } = useAuth();
  const { data: user, isLoading } = useMyInfo();

  const getInitials = (name: string | null | undefined): string => {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    return name.charAt(0).toUpperCase();
  };

  const userMenuItems = [
    {
      key: 'profile',
      label: (
        <div className="py-2">
          <div className="font-semibold text-gray-800">{user?.fullName || 'Người dùng'}</div>
          <div className="text-sm text-gray-500">{user?.username || 'Chưa có username'}</div>
          <div className="text-xs text-gray-400 mt-1">
            {user?.role === 'ADMIN'
              ? 'Quản trị viên'
              : user?.role === 'MANAGER'
                ? 'Quản lý'
                : 'Khách thuê'}
          </div>
        </div>
      ),
      disabled: true,
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      label: 'Đăng xuất',
      danger: true,
      icon: <UserOutlined />,
      onClick: () => {
        logout();
        window.location.href = '/login';
      },
    },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      className="h-12 mb-2 bg-white rounded-full md:px-4 px-3 flex items-center justify-between shadow-sm md:mx-0 mx-3 mt-3 md:mt-0"
    >
      {/* Mobile Menu Button */}
      {onMenuClick && (
        <button
          onClick={onMenuClick}
          className="md:hidden w-10 h-10 flex items-center justify-center bg-slate-50 hover:bg-slate-100 rounded-full transition-colors mr-2"
        >
          <MenuOutlined style={{ fontSize: 18, color: '#475569' }} />
        </button>
      )}

      {/* Search Bar */}
      <div className="flex-1 max-w-md">
        <div className="relative">
          <SearchOutlined
            style={{ fontSize: 18, color: '#94a3b8' }}
            className="absolute left-4 top-1/2 -translate-y-1/2"
          />
          <input
            type="text"
            placeholder="Tìm kiếm... (⌘K)"
            className="w-full pl-11 pr-4 py-2 bg-slate-50 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition-all"
          />
        </div>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2">
        <button className="w-10 h-10 flex items-center justify-center bg-slate-50 hover:bg-slate-100 rounded-full transition-colors relative">
          <BellOutlined style={{ fontSize: 18, color: '#475569' }} />
          <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
        </button>

        {isLoading ? (
          <div className="w-10 h-10 flex items-center justify-center">
            <Spin size="small" />
          </div>
        ) : (
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight" trigger={['click']}>
            <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-semibold cursor-pointer hover:bg-blue-600 transition-colors">
              {getInitials(user?.fullName)}
            </div>
          </Dropdown>
        )}
      </div>
    </motion.div>
  );
};
