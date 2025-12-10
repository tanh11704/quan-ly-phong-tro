import {
  BarChartOutlined,
  BugOutlined,
  CalendarOutlined,
  DashboardOutlined,
  FileTextOutlined,
  LockOutlined,
  MessageOutlined,
  SettingOutlined,
  ShoppingCartOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Role } from '../../features/auth/types/auth';
import type { MenuItem } from './types';

// Menu items cho tất cả các role
export const ALL_MENU_ITEMS: MenuItem[] = [
  // Menu chung cho tất cả role
  { key: 'dashboard', icon: <DashboardOutlined style={{ fontSize: 20 }} />, label: 'Dashboard' },

  // Menu cho MANAGER (quản lý tòa nhà)
  {
    key: 'buildings',
    icon: <FileTextOutlined style={{ fontSize: 20 }} />,
    label: 'Tòa nhà',
    allowedRoles: [Role.MANAGER],
  },
  {
    key: 'tenants',
    icon: <UserOutlined style={{ fontSize: 20 }} />,
    label: 'Khách thuê',
    allowedRoles: [Role.MANAGER],
  },
  {
    key: 'contracts',
    icon: <FileTextOutlined style={{ fontSize: 20 }} />,
    label: 'Hợp đồng',
    allowedRoles: [Role.MANAGER],
  },
  {
    key: 'invoices',
    icon: <ShoppingCartOutlined style={{ fontSize: 20 }} />,
    label: 'Hóa đơn',
    allowedRoles: [Role.MANAGER],
  },
  {
    key: 'analytics',
    icon: <BarChartOutlined style={{ fontSize: 20 }} />,
    label: 'Phân tích',
    allowedRoles: [Role.MANAGER],
  },
  {
    key: 'calendar',
    icon: <CalendarOutlined style={{ fontSize: 20 }} />,
    label: 'Lịch',
    allowedRoles: [Role.MANAGER],
  },
  {
    key: 'settings',
    icon: <SettingOutlined style={{ fontSize: 20 }} />,
    label: 'Cài đặt',
    allowedRoles: [Role.MANAGER],
  },

  // Menu cho ADMIN (quản lý hệ thống - chỉ xem và khóa)
  {
    key: 'sentry-logs',
    icon: <BugOutlined style={{ fontSize: 20 }} />,
    label: 'Sentry Logs',
    allowedRoles: [Role.ADMIN],
  },
  {
    key: 'managers',
    icon: <TeamOutlined style={{ fontSize: 20 }} />,
    label: 'Quản lý Manager',
    allowedRoles: [Role.ADMIN],
  },
  {
    key: 'buildings-overview',
    icon: <FileTextOutlined style={{ fontSize: 20 }} />,
    label: 'Xem tòa nhà',
    allowedRoles: [Role.ADMIN],
  },
  {
    key: 'moderation',
    icon: <LockOutlined style={{ fontSize: 20 }} />,
    label: 'Kiểm duyệt',
    allowedRoles: [Role.ADMIN],
  },
  {
    key: 'system-settings',
    icon: <SettingOutlined style={{ fontSize: 20 }} />,
    label: 'Cài đặt hệ thống',
    allowedRoles: [Role.ADMIN],
  },

  // Menu cho TENANT (khách thuê)
  {
    key: 'my-contracts',
    icon: <FileTextOutlined style={{ fontSize: 20 }} />,
    label: 'Hợp đồng của tôi',
    allowedRoles: [Role.TENANT],
  },
  {
    key: 'my-invoices',
    icon: <ShoppingCartOutlined style={{ fontSize: 20 }} />,
    label: 'Hóa đơn của tôi',
    allowedRoles: [Role.TENANT],
  },
  {
    key: 'messages',
    icon: <MessageOutlined style={{ fontSize: 20 }} />,
    label: 'Tin nhắn',
    allowedRoles: [Role.TENANT],
  },
];

// Helper function để filter menu items theo role
export const getMenuItemsByRole = (role: Role | null): MenuItem[] => {
  if (!role) {
    return [];
  }

  return ALL_MENU_ITEMS.filter((item) => {
    // Nếu không có allowedRoles, tất cả role đều được phép
    if (!item.allowedRoles || item.allowedRoles.length === 0) {
      return true;
    }
    // Kiểm tra role có trong allowedRoles không
    return item.allowedRoles.includes(role);
  });
};

export const SIDEBAR_VARIANTS = {
  expanded: { width: 260 },
  collapsed: { width: 80 },
};

export const MENU_ITEM_VARIANTS = {
  hover: { x: 4, transition: { duration: 0.2 } },
};
