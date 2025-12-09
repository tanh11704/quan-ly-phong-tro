import {
  BarChartOutlined,
  CalendarOutlined,
  DashboardOutlined,
  FileTextOutlined,
  MessageOutlined,
  SettingOutlined,
  ShoppingCartOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { MenuItem } from './types';

export const MENU_ITEMS: MenuItem[] = [
  { key: 'dashboard', icon: <DashboardOutlined style={{ fontSize: 20 }} />, label: 'Dashboard' },
  { key: 'customers', icon: <UserOutlined style={{ fontSize: 20 }} />, label: 'Khách hàng' },
  { key: 'orders', icon: <ShoppingCartOutlined style={{ fontSize: 20 }} />, label: 'Đơn hàng' },
  { key: 'analytics', icon: <BarChartOutlined style={{ fontSize: 20 }} />, label: 'Phân tích' },
  { key: 'calendar', icon: <CalendarOutlined style={{ fontSize: 20 }} />, label: 'Lịch' },
  { key: 'messages', icon: <MessageOutlined style={{ fontSize: 20 }} />, label: 'Tin nhắn' },
  { key: 'documents', icon: <FileTextOutlined style={{ fontSize: 20 }} />, label: 'Tài liệu' },
  { key: 'settings', icon: <SettingOutlined style={{ fontSize: 20 }} />, label: 'Cài đặt' },
];

export const SIDEBAR_VARIANTS = {
  expanded: { width: 260 },
  collapsed: { width: 80 },
};

export const MENU_ITEM_VARIANTS = {
  hover: { x: 4, transition: { duration: 0.2 } },
};
