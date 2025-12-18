import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../../auth/types/auth';
import type { TenantCreationRequest, TenantResponse } from '../types/tenants';

// Get tenant by ID
export const useTenant = (id: number | null) => {
  return useQuery({
    queryKey: ['tenants', id],
    queryFn: async (): Promise<TenantResponse> => {
      const response = await axiosInstance.get<ApiResponse<TenantResponse>>(`/tenants/${id}`);
      return response.data.result;
    },
    enabled: !!id,
  });
};

// Create tenant mutation
export const useCreateTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: TenantCreationRequest): Promise<ApiResponse<TenantResponse>> => {
      const response = await axiosInstance.post<ApiResponse<TenantResponse>>('/tenants', request);
      return response.data;
    },
    onSuccess: (data) => {
      // Invalidate tenants list for the room
      if (data.result?.roomId) {
        queryClient.invalidateQueries({ queryKey: ['rooms', data.result.roomId, 'tenants'] });
      }
      // Also invalidate all tenant queries
      queryClient.invalidateQueries({ queryKey: ['tenants'] });
    },
  });
};

// End tenant contract mutation
export const useEndTenantContract = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number): Promise<ApiResponse<TenantResponse>> => {
      const response = await axiosInstance.put<ApiResponse<TenantResponse>>(`/tenants/${id}/end`);
      return response.data;
    },
    onSuccess: (data) => {
      // Invalidate tenant detail
      queryClient.invalidateQueries({ queryKey: ['tenants', data.result.id] });
      // Invalidate tenants list for the room
      if (data.result?.roomId) {
        queryClient.invalidateQueries({ queryKey: ['rooms', data.result.roomId, 'tenants'] });
      }
      // Also invalidate all tenant queries
      queryClient.invalidateQueries({ queryKey: ['tenants'] });
    },
  });
};
