import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { BentoGrid } from './BentoGrid';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

interface BentoLayoutProps {
  children?: React.ReactNode;
}

const BentoLayout: React.FC<BentoLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const location = useLocation();

  const handleToggleCollapse = () => {
    setCollapsed(!collapsed);
  };

  // Render BentoGrid for dashboard, otherwise render children
  const isDashboard = location.pathname === '/dashboard';

  return (
    <div className="h-screen w-full bg-slate-100 md:p-3 p-0 flex flex-col md:flex-row gap-0 md:gap-3 overflow-hidden">
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
          shrink-0
          ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'}
          md:translate-x-0
        `}
      >
        <Sidebar collapsed={collapsed} onToggleCollapse={handleToggleCollapse} />
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col h-screen w-full min-w-0 overflow-hidden">
        <div className="shrink-0">
          <Header onMenuClick={() => setMobileMenuOpen(!mobileMenuOpen)} />
        </div>
        <div className="flex-1 min-h-0 overflow-y-auto overflow-x-hidden">
          {isDashboard ? <BentoGrid /> : children}
        </div>
      </div>
    </div>
  );
};

export default BentoLayout;
