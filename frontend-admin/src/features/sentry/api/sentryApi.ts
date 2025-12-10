import { useQuery } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { SentryEventResponse, SentryIssue, SentryLogsResponse } from '../types/sentry';

export const useSentryIssues = (params?: {
  page?: number;
  pageSize?: number;
  status?: 'unresolved' | 'resolved' | 'ignored' | 'muted';
  level?: 'error' | 'warning' | 'info' | 'debug' | 'fatal';
  query?: string;
}) => {
  return useQuery({
    queryKey: ['sentry', 'issues', params],
    queryFn: async (): Promise<SentryLogsResponse> => {
      const response = await axiosInstance.get<SentryLogsResponse>('/api/sentry/issues', {
        params: {
          page: params?.page || 1,
          pageSize: params?.pageSize || 20,
          status: params?.status,
          level: params?.level,
          query: params?.query,
        },
      });
      return response.data;
    },
    staleTime: 30000, // 30 seconds
  });
};

// Get single Sentry issue details
export const useSentryIssue = (issueId: string) => {
  return useQuery({
    queryKey: ['sentry', 'issue', issueId],
    queryFn: async (): Promise<SentryIssue> => {
      const response = await axiosInstance.get<SentryIssue>(`/api/sentry/issues/${issueId}`);
      return response.data;
    },
    enabled: !!issueId,
  });
};

// Get events for an issue
export const useSentryIssueEvents = (issueId: string, limit = 10) => {
  return useQuery({
    queryKey: ['sentry', 'issue', issueId, 'events'],
    queryFn: async (): Promise<SentryEventResponse[]> => {
      const response = await axiosInstance.get<SentryEventResponse[]>(
        `/api/sentry/issues/${issueId}/events`,
        {
          params: { limit },
        },
      );
      return response.data;
    },
    enabled: !!issueId,
  });
};
