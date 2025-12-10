import { HomeOutlined, MenuOutlined } from '@ant-design/icons';
import { AnimatePresence, motion } from 'framer-motion';
import React, { useMemo } from 'react';
import { useAuth } from '../../features/auth/context';
import { MENU_ITEM_VARIANTS, SIDEBAR_VARIANTS, getMenuItemsByRole } from './constants';
import type { MenuItem } from './types';

interface SidebarProps {
  collapsed: boolean;
  selectedKey: string;
  onToggleCollapse: () => void;
  onSelectMenu: (key: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  collapsed,
  selectedKey,
  onToggleCollapse,
  onSelectMenu,
}) => {
  const { role } = useAuth();

  // Filter menu items theo role
  const menuItems = useMemo(() => getMenuItemsByRole(role), [role]);

  // Helper để hiển thị role label
  const getRoleLabel = (role: string | null): string => {
    switch (role) {
      case 'ADMIN':
        return 'Quản trị viên';
      case 'MANAGER':
        return 'Quản lý';
      case 'TENANT':
        return 'Khách thuê';
      default:
        return 'Người dùng';
    }
  };

  return (
    <motion.div
      initial={false}
      animate={collapsed ? 'collapsed' : 'expanded'}
      variants={SIDEBAR_VARIANTS}
      transition={{ duration: 0.3, ease: 'easeInOut' }}
      className="h-full bg-slate-900 md:rounded-3xl rounded-none shadow-sm flex flex-col overflow-hidden"
    >
      {/* Logo & Toggle */}
      <div className="p-6 flex items-center justify-between">
        <AnimatePresence mode="wait">
          {!collapsed && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex items-center gap-3"
            >
              <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                <HomeOutlined style={{ fontSize: 18, color: 'white' }} />
              </div>
              <span className="text-white font-semibold text-lg">Bento</span>
            </motion.div>
          )}
        </AnimatePresence>

        <button
          onClick={onToggleCollapse}
          className="p-2 hover:bg-white/10 rounded-lg transition-colors"
        >
          <MenuOutlined style={{ fontSize: 20, color: '#94a3b8' }} />
        </button>
      </div>

      {/* Menu Items */}
      <div className="flex-1 px-3 overflow-y-auto scrollbar-hide">
        {menuItems.map((item: MenuItem) => (
          <motion.div
            key={item.key}
            variants={MENU_ITEM_VARIANTS}
            whileHover="hover"
            onClick={() => onSelectMenu(item.key)}
            className={`
              mb-2 rounded-2xl cursor-pointer transition-all duration-200
              ${
                selectedKey === item.key
                  ? 'bg-white/10 text-white'
                  : 'text-slate-400 hover:bg-white/5'
              }
            `}
          >
            <div
              className={`flex items-center gap-3 ${
                collapsed ? 'justify-center p-4' : 'px-4 py-3'
              }`}
            >
              {item.icon}
              <AnimatePresence>
                {!collapsed && (
                  <motion.span
                    initial={{ opacity: 0, width: 0 }}
                    animate={{ opacity: 1, width: 'auto' }}
                    exit={{ opacity: 0, width: 0 }}
                    className="text-sm font-medium whitespace-nowrap"
                  >
                    {item.label}
                  </motion.span>
                )}
              </AnimatePresence>
            </div>
          </motion.div>
        ))}
      </div>

      {/* User Profile */}
      <div className="p-4 border-t border-white/10">
        <div className={`flex items-center gap-3 ${collapsed ? 'justify-center' : ''}`}>
          <div className="w-10 h-10 bg-gradient-to-br from-pink-500 to-orange-500 rounded-full flex items-center justify-center text-white font-semibold">
            {role ? role.charAt(0) : 'U'}
          </div>
          <AnimatePresence>
            {!collapsed && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="flex-1"
              >
                <div className="text-white text-sm font-medium">{getRoleLabel(role)}</div>
                <div className="text-slate-400 text-xs">{role || 'Chưa đăng nhập'}</div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </motion.div>
  );
};
