import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../../auth/types/auth';
import type { PageResponse } from '../../buildings/types/buildings';
import type {
  InvoiceCreationRequest,
  InvoiceDetailResponse,
  InvoiceResponse,
  InvoiceStatus,
} from '../types/invoices';

// Get invoices with pagination and filters
export const useInvoices = (
  buildingId: number | null,
  page: number = 0,
  size: number = 20,
  period?: string | null,
  status?: InvoiceStatus | null,
  sort?: string,
) => {
  return useQuery({
    queryKey: ['invoices', 'list', buildingId, page, size, period, status, sort],
    queryFn: async (): Promise<PageResponse<InvoiceResponse>> => {
      const params = new URLSearchParams({
        buildingId: buildingId!.toString(),
        page: page.toString(),
        size: size.toString(),
      });
      if (period) {
        params.append('period', period);
      }
      if (status) {
        params.append('status', status);
      }
      if (sort) {
        params.append('sort', sort);
      }
      const response = await axiosInstance.get<PageResponse<InvoiceResponse>>(
        `/invoices?${params.toString()}`,
      );
      return response.data;
    },
    enabled: !!buildingId,
  });
};

// Get invoice detail by ID
export const useInvoiceDetail = (id: number | null) => {
  return useQuery({
    queryKey: ['invoices', id],
    queryFn: async (): Promise<InvoiceDetailResponse> => {
      const response = await axiosInstance.get<ApiResponse<InvoiceDetailResponse>>(
        `/invoices/${id}`,
      );
      return response.data.result;
    },
    enabled: !!id,
  });
};

// Create invoices mutation
export const useCreateInvoices = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (
      request: InvoiceCreationRequest,
    ): Promise<ApiResponse<InvoiceResponse[]>> => {
      const response = await axiosInstance.post<ApiResponse<InvoiceResponse[]>>(
        '/invoices/generate',
        request,
      );
      return response.data;
    },
    onSuccess: (_data, variables) => {
      // Invalidate invoices list for the building
      queryClient.invalidateQueries({
        queryKey: ['invoices', 'list', variables.buildingId],
      });
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
    },
  });
};

// Pay invoice mutation
export const usePayInvoice = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number): Promise<ApiResponse<InvoiceResponse>> => {
      const response = await axiosInstance.put<ApiResponse<InvoiceResponse>>(`/invoices/${id}/pay`);
      return response.data;
    },
    onSuccess: (_data, variables) => {
      // Invalidate invoice detail
      queryClient.invalidateQueries({ queryKey: ['invoices', variables] });
      // Invalidate invoices list
      queryClient.invalidateQueries({ queryKey: ['invoices', 'list'] });
    },
  });
};

// Send invoice email mutation
export const useSendInvoiceEmail = () => {
  return useMutation({
    mutationFn: async (id: number): Promise<ApiResponse<string>> => {
      const response = await axiosInstance.post<ApiResponse<string>>(`/invoices/${id}/send-email`);
      return response.data;
    },
  });
};
