import React, { useState } from 'react';
import { BentoGrid } from './BentoGrid';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

const BentoLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [selectedKey, setSelectedKey] = useState('dashboard');

  const handleToggleCollapse = () => {
    setCollapsed(!collapsed);
  };

  const handleSelectMenu = (key: string) => {
    setSelectedKey(key);
  };

  return (
    <div className="h-screen w-full bg-slate-100 p-3 flex gap-3 overflow-hidden">
      <Sidebar
        collapsed={collapsed}
        selectedKey={selectedKey}
        onToggleCollapse={handleToggleCollapse}
        onSelectMenu={handleSelectMenu}
      />

      <div className="flex-1 flex flex-col h-full min-w-0">
        <Header />
        <BentoGrid />
      </div>
    </div>
  );
};

export default BentoLayout;
