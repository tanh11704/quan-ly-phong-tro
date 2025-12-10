import { motion } from 'framer-motion';
import React, { useMemo, useState } from 'react';
import type { ChartCardProps } from '../types';

const MONTHS = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];

export const ChartCard: React.FC<ChartCardProps> = ({
  title,
  data,
  labels,
  delay = 0.3,
  currencyPrefix = '₫',
  showValues = true,
  className = '',
}) => {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  const chartLabels = labels || MONTHS.slice(0, data.length);

  // Format value for display
  const formatValue = (value: number) => {
    if (value >= 1000000) {
      return `${currencyPrefix}${(value / 1000000).toFixed(1)}M`;
    }
    if (value >= 1000) {
      return `${currencyPrefix}${(value / 1000).toFixed(1)}K`;
    }
    return `${currencyPrefix}${value.toFixed(0)}`;
  };

  // Calculate actual values (if data is percentage, convert to millions)
  const actualValues = useMemo(() => {
    const max = Math.max(...data);
    if (max <= 100) {
      // Data is percentage-based, convert to millions (scale to 50M max)
      // Example: 95% = 47.5M, 40% = 20M
      return data.map((val) => (val / 100) * 50 * 1000000); // Scale to 50M VND max
    }
    return data;
  }, [data]);

  // Calculate bar heights as percentages
  const barHeights = useMemo(() => {
    const max = Math.max(...actualValues);
    return actualValues.map((val) => (val / max) * 100);
  }, [actualValues]);

  // Y-axis labels (4 ticks)
  const yAxisTicks = useMemo(() => {
    const ticks = [];
    const max = Math.max(...actualValues);
    for (let i = 4; i >= 0; i--) {
      ticks.push((max / 4) * i);
    }
    return ticks;
  }, [actualValues]);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay }}
      className={`border border-slate-100 rounded-2xl p-3 md:p-4 hover:shadow-lg transition-shadow flex flex-col min-h-[300px] md:min-h-0 md:h-full ${className}`}
    >
      <h3 className="text-sm font-bold text-slate-900 mb-3">{title}</h3>

      <div className="flex-1 flex flex-col min-h-[250px] md:min-h-0">
        {/* Y-axis and Chart Area */}
        <div className="flex-1 flex gap-2 md:gap-4 relative min-h-0">
          {/* Y-axis */}
          <div className="flex flex-col justify-between pb-2 pr-1 min-w-[50px] md:min-w-[60px]">
            {yAxisTicks.map((tick, i) => (
              <span
                key={i}
                className="text-[9px] md:text-[10px] text-slate-500 font-medium whitespace-nowrap"
              >
                {formatValue(tick)}
              </span>
            ))}
          </div>

          {/* Chart Bars - Responsive, không có min-width cố định */}
          <div className="flex-1 flex flex-col min-w-0 overflow-x-auto overflow-y-hidden">
            <div className="flex-1 flex items-end justify-around gap-1 relative pb-2 min-h-[200px] md:min-h-0 w-full">
              {/* Grid lines */}
              <div className="absolute inset-0 flex flex-col justify-between pointer-events-none">
                {yAxisTicks.map((_, i) => (
                  <div key={i} className="border-t border-slate-100" style={{ height: '20%' }} />
                ))}
              </div>

              {/* Bars */}
              {data.map((_, i) => {
                const height = barHeights[i];
                const actualValue = actualValues[i];
                const isHovered = hoveredIndex === i;

                return (
                  <div
                    key={i}
                    className="flex-1 flex items-end justify-center group relative"
                    onMouseEnter={() => setHoveredIndex(i)}
                    onMouseLeave={() => setHoveredIndex(null)}
                    style={{ height: '100%' }}
                  >
                    {/* Value Tooltip */}
                    {showValues && isHovered && (
                      <motion.div
                        initial={{ opacity: 0, y: -5 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="absolute -top-6 bg-slate-900 text-white text-[10px] px-1.5 py-0.5 rounded whitespace-nowrap z-10"
                      >
                        {formatValue(actualValue)}
                      </motion.div>
                    )}

                    {/* Bar */}
                    <motion.div
                      initial={{ height: 0 }}
                      animate={{ height: `${height}%` }}
                      transition={{ delay: 0.5 + i * 0.05, duration: 0.3 }}
                      className={`w-full bg-linear-to-t from-blue-500 to-blue-400 rounded-t transition-all ${
                        isHovered ? 'from-blue-600 to-blue-500 shadow-lg scale-105 z-10' : ''
                      }`}
                      style={{
                        minHeight: height > 0 ? '4px' : '0',
                      }}
                    />
                  </div>
                );
              })}
            </div>

            {/* X-axis Labels - Responsive */}
            <div className="flex justify-around gap-1 pt-1 border-t border-slate-100 mt-auto w-full">
              {chartLabels.map((label, i) => (
                <span
                  key={i}
                  className="text-[9px] md:text-[10px] text-slate-500 font-medium flex-1 text-center whitespace-nowrap"
                  title={label}
                >
                  {label}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
};
