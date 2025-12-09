import { motion } from 'framer-motion';
import React from 'react';
import type { ActivityCardProps } from '../types';

export const ActivityCard: React.FC<ActivityCardProps> = ({ title, activities, delay = 0.4 }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay }}
      className="col-span-1 border border-slate-100 rounded-2xl p-4 hover:shadow-lg transition-shadow flex flex-col h-full"
    >
      <h3 className="text-xs font-semibold text-slate-900 mb-2">{title}</h3>
      <div className="space-y-2 flex-1">
        {activities.map((activity) => (
          <div key={activity.id} className="flex items-center gap-3">
            <div className="w-2 h-2 bg-green-500 rounded-full"></div>
            <div className="text-xs text-slate-600">{activity.text}</div>
          </div>
        ))}
      </div>
    </motion.div>
  );
};
