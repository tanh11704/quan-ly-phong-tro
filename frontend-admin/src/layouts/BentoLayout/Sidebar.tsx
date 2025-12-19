import { HomeOutlined, MenuOutlined } from '@ant-design/icons';
import { AnimatePresence, motion } from 'framer-motion';
import React, { useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/context';
import { MENU_ITEM_VARIANTS, SIDEBAR_VARIANTS, getMenuItemsByRole } from './constants';
import type { MenuItem } from './types';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ collapsed, onToggleCollapse }) => {
  const { role } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Map route paths to menu keys
  const getSelectedKey = () => {
    if (location.pathname === '/dashboard') return 'dashboard';
    if (location.pathname === '/buildings') return 'buildings';
    if (location.pathname === '/rooms') return 'rooms';
    if (location.pathname === '/invoices') return 'invoices';
    return 'dashboard';
  };

  const selectedKey = getSelectedKey();

  const handleSelectMenu = (key: string) => {
    const routeMap: Record<string, string> = {
      dashboard: '/dashboard',
      buildings: '/buildings',
      rooms: '/rooms',
      invoices: '/invoices',
    };
    const route = routeMap[key] || '/dashboard';
    navigate(route);
  };

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
      {/* Logo & Toggle - Fixed ở trên cùng */}
      <div className="shrink-0 p-6 flex items-center justify-between border-b border-white/10">
        <AnimatePresence mode="wait">
          {!collapsed && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex items-center gap-3"
            >
              <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
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

      {/* Menu Items - Scrollable ở giữa */}
      <div className="flex-1 min-h-0 px-3 py-4 overflow-y-auto overflow-x-hidden scrollbar-hide">
        {menuItems.map((item: MenuItem) => (
          <motion.div
            key={item.key}
            variants={MENU_ITEM_VARIANTS}
            whileHover="hover"
            onClick={() => handleSelectMenu(item.key)}
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

      {/* User Profile - Fixed ở dưới cùng - Padding giống phần trên */}
      <div className="shrink-0 px-6 py-6 border-t border-white/10">
        <div className={`flex items-center gap-3 ${collapsed ? 'justify-center' : ''}`}>
          <div className="w-10 h-10 bg-slate-600 rounded-full flex items-center justify-center text-white font-semibold">
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
