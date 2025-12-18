import { BellOutlined, MenuOutlined, SearchOutlined } from '@ant-design/icons';
import { motion } from 'framer-motion';
import React from 'react';

interface HeaderProps {
  onMenuClick?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
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

        <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-semibold cursor-pointer hover:bg-blue-600 transition-colors">
          A
        </div>
      </div>
    </motion.div>
  );
};
