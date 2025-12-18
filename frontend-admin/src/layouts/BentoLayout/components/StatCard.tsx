import { motion } from 'framer-motion';
import React, { useMemo } from 'react';
import CountUp from 'react-countup';
import type { StatCardProps } from '../types';

export const StatCard: React.FC<StatCardProps> = ({
  value,
  label,
  icon,
  gradientFrom,
  gradientTo: _gradientTo, // Keep for interface compatibility but not used
  textColor = 'white',
  delay = 0,
  enableCountUp = true,
  duration = 2,
  decimals = 0,
  prefix = '',
  suffix = '',
  separator = ',',
}) => {
  const isDark = textColor === 'dark';
  const textClass = isDark ? 'text-slate-900' : 'text-white';
  const labelClass = isDark ? 'text-slate-500' : `${gradientFrom.replace('from-', 'text-')}-100`;
  // Extract base color from gradient (e.g., "from-blue-500" -> "blue-500")
  const baseColor = gradientFrom.replace('from-', '');
  const backgroundClass = isDark ? 'border border-slate-100 bg-white' : `bg-${baseColor}`;

  // Format large numbers with K, M, B suffixes
  const formatValue = useMemo(() => {
    if (typeof value === 'number') {
      const num = value;
      if (num >= 1000000000) {
        return { displayValue: num / 1000000000, suffix: 'B', decimals: 1 };
      }
      if (num >= 1000000) {
        return { displayValue: num / 1000000, suffix: 'M', decimals: 1 };
      }
      if (num >= 1000) {
        return { displayValue: num / 1000, suffix: 'K', decimals: 0 };
      }
      return { displayValue: num, suffix: '', decimals: 0 };
    }

    // If string, parse it
    const cleaned = String(value)
      .replace(/[₫$€£¥,\s]/g, '')
      .replace(/M/gi, '000000')
      .replace(/K/gi, '000')
      .replace(/B/gi, '000000000');

    const num = parseFloat(cleaned) || 0;
    if (num >= 1000000) {
      return { displayValue: num / 1000000, suffix: 'M', decimals: 1 };
    }
    return { displayValue: num, suffix: '', decimals: 0 };
  }, [value]);

  // Determine if value is a currency
  const hasCurrencyPrefix = (typeof value === 'string' && value.includes('₫')) || prefix === '₫';
  const displayPrefix = hasCurrencyPrefix ? '₫' : prefix;

  // Use suffix from formatValue or prop
  const displaySuffix = formatValue.suffix || suffix;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay }}
      className={`col-span-1 ${backgroundClass} rounded-2xl p-4 ${textClass} flex flex-col justify-between hover:shadow-lg transition-shadow`}
    >
      <div
        className={`w-10 h-10 ${
          isDark ? 'bg-emerald-100' : 'bg-white/20'
        } rounded-xl flex items-center justify-center`}
      >
        {icon}
      </div>
      <div>
        <div className={`text-2xl font-bold mb-1 ${textClass}`}>
          {enableCountUp ? (
            <CountUp
              start={0}
              end={formatValue.displayValue}
              duration={duration}
              decimals={formatValue.decimals || decimals}
              separator={separator}
              prefix={displayPrefix}
              suffix={displaySuffix}
              delay={delay}
            />
          ) : (
            <>
              {displayPrefix}
              {typeof value === 'number' ? value.toLocaleString() : value}
              {displaySuffix}
            </>
          )}
        </div>
        <div className={`${labelClass} text-xs`}>{label}</div>
      </div>
    </motion.div>
  );
};
