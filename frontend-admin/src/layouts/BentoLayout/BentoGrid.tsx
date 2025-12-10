import { BarChartOutlined, ShoppingCartOutlined, UserOutlined } from '@ant-design/icons';
import { motion } from 'framer-motion';
import React from 'react';
import { ActivityCard, ChartCard, QuickActionsCard, StatCard } from './components';

interface BentoGridProps {
  title?: string;
  subtitle?: string;
}

const CHART_DATA = [40, 70, 45, 80, 60, 90, 75, 85, 65, 95, 70, 88];
const ACTIVITIES = [
  { id: 1, text: 'Đơn hàng mới #1001' },
  { id: 2, text: 'Đơn hàng mới #1002' },
  { id: 3, text: 'Đơn hàng mới #1003' },
];
const QUICK_ACTIONS = ['Tạo đơn hàng', 'Thêm khách hàng', 'Xem báo cáo', 'Cài đặt'];

export const BentoGrid: React.FC<BentoGridProps> = ({
  title = 'Dashboard',
  subtitle = 'Chào mừng trở lại! Đây là tổng quan của bạn.',
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ delay: 0.1 }}
      className="h-full bg-white md:rounded-[32px] rounded-none shadow-sm flex flex-col overflow-hidden"
    >
      <div className="flex-1 min-h-0 overflow-y-auto p-4 md:p-4 p-3">
        {/* Page Title */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-3">
          <h1 className="text-xl md:text-2xl font-bold text-slate-900 mb-1">{title}</h1>
          <p className="text-xs text-slate-500">{subtitle}</p>
        </motion.div>

        {/* Bento Grid - Responsive: 1 cột mobile, 2 cột tablet, 4 cột desktop */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 auto-rows-min w-full">
          <StatCard
            value={1234}
            label="Tổng đơn hàng"
            icon={<ShoppingCartOutlined style={{ fontSize: 20, color: 'white' }} />}
            gradientFrom="from-blue-500"
            gradientTo="to-blue-600"
            delay={0.2}
            duration={2}
            separator=","
          />

          <StatCard
            value={8452}
            label="Khách hàng"
            icon={<UserOutlined style={{ fontSize: 20, color: 'white' }} />}
            gradientFrom="from-purple-500"
            gradientTo="to-purple-600"
            delay={0.25}
            duration={2}
            separator=","
          />

          <ChartCard
            title="Doanh thu theo tháng"
            data={CHART_DATA}
            delay={0.3}
            className="col-span-1 sm:col-span-2 lg:row-span-2"
          />

          <StatCard
            value={45200000}
            label="Doanh thu"
            icon={<BarChartOutlined style={{ fontSize: 20, color: '#059669' }} />}
            gradientFrom="from-white"
            gradientTo="to-white"
            textColor="dark"
            delay={0.35}
            duration={2.5}
            prefix="₫"
            separator=","
          />

          <ActivityCard title="Hoạt động gần đây" activities={ACTIVITIES} delay={0.4} />

          <QuickActionsCard title="Thao tác nhanh" actions={QUICK_ACTIONS} delay={0.45} />
        </div>
      </div>
    </motion.div>
  );
};
