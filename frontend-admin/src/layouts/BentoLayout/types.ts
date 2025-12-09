export interface MenuItem {
  key: string;
  icon: React.ReactNode;
  label: string;
}

export interface StatCardProps {
  value: string | number;
  label: string;
  icon: React.ReactNode;
  gradientFrom: string;
  gradientTo: string;
  textColor?: 'white' | 'dark';
  delay?: number;
  enableCountUp?: boolean;
  duration?: number;
  decimals?: number;
  prefix?: string;
  suffix?: string;
  separator?: string;
}

export interface ChartCardProps {
  title: string;
  data: number[];
  labels?: string[];
  delay?: number;
  currencyPrefix?: string;
  showValues?: boolean;
  maxValue?: number;
  className?: string;
}

export interface ActivityItem {
  id: string | number;
  text: string;
}

export interface ActivityCardProps {
  title: string;
  activities: ActivityItem[];
  delay?: number;
}

export interface QuickActionsCardProps {
  title: string;
  actions: string[];
  delay?: number;
}
