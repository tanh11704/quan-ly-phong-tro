import { motion } from 'framer-motion';
import React from 'react';
import type { QuickActionsCardProps } from '../types';

export const QuickActionsCard: React.FC<QuickActionsCardProps> = ({
  title,
  actions,
  delay = 0.45,
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay }}
      className="col-span-2 border border-slate-100 rounded-2xl p-4 hover:shadow-lg transition-shadow flex flex-col h-full"
    >
      <h3 className="text-xs font-bold text-slate-900 mb-2">{title}</h3>
      <div className="grid grid-cols-2 gap-2 flex-1">
        {actions.map((action, i) => (
          <button
            key={i}
            className="py-2 px-3 bg-slate-50 hover:bg-slate-100 rounded-xl text-xs font-medium text-slate-700 transition-colors"
          >
            {action}
          </button>
        ))}
      </div>
    </motion.div>
  );
};
