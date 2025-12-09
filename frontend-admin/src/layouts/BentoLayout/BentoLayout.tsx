import React, { useState } from 'react';
import { BentoGrid } from './BentoGrid';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

const BentoLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [selectedKey, setSelectedKey] = useState('dashboard');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleToggleCollapse = () => {
    setCollapsed(!collapsed);
  };

  const handleSelectMenu = (key: string) => {
    setSelectedKey(key);
    // Đóng mobile menu sau khi chọn
    setMobileMenuOpen(false);
  };

  return (
    <div className="min-h-screen w-full bg-slate-100 md:p-3 p-0 flex flex-col md:flex-row gap-0 md:gap-3">
      {/* Mobile Menu Overlay */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 md:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* Sidebar - Ẩn trên mobile, hiện khi mobileMenuOpen = true */}
      <div
        className={`
          fixed md:relative
          top-0 left-0
          h-screen md:h-full z-50
          transition-transform duration-300 ease-in-out
          ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'}
          md:translate-x-0
        `}
      >
        <Sidebar
          collapsed={collapsed}
          selectedKey={selectedKey}
          onToggleCollapse={handleToggleCollapse}
          onSelectMenu={handleSelectMenu}
        />
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-h-screen md:min-h-0 md:h-screen w-full md:min-w-0">
        <Header onMenuClick={() => setMobileMenuOpen(!mobileMenuOpen)} />
        <div className="flex-1 overflow-y-auto">
          <BentoGrid />
        </div>
      </div>
    </div>
  );
};

export default BentoLayout;
