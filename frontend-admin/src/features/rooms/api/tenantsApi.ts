import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../../auth/types/auth';
import type { PageResponse } from '../../buildings/types/buildings';
import type { TenantCreationRequest, TenantResponse, TenantUpdateRequest } from '../types/tenants';

// Get tenants list with pagination and filters
export const useTenants = (
  page: number = 0,
  size: number = 20,
  buildingId?: number | null,
  roomId?: number | null,
  active?: boolean | null,
  sort?: string,
) => {
  return useQuery({
    queryKey: ['tenants', 'list', page, size, buildingId, roomId, active, sort],
    queryFn: async (): Promise<PageResponse<TenantResponse>> => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (buildingId) {
        params.append('buildingId', buildingId.toString());
      }
      if (roomId) {
        params.append('roomId', roomId.toString());
      }
      if (active !== null && active !== undefined) {
        params.append('active', active.toString());
      }
      if (sort) {
        params.append('sort', sort);
      }
      const response = await axiosInstance.get<PageResponse<TenantResponse>>(
        `/tenants?${params.toString()}`,
      );
      return response.data;
    },
  });
};

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

// Update tenant mutation
export const useUpdateTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      request,
    }: {
      id: number;
      request: TenantUpdateRequest;
    }): Promise<ApiResponse<TenantResponse>> => {
      const response = await axiosInstance.put<ApiResponse<TenantResponse>>(
        `/tenants/${id}`,
        request,
      );
      return response.data;
    },
    onSuccess: (data, variables) => {
      // Invalidate tenant detail
      queryClient.invalidateQueries({ queryKey: ['tenants', variables.id] });
      // Invalidate tenants list for the room
      if (data.result?.roomId) {
        queryClient.invalidateQueries({ queryKey: ['rooms', data.result.roomId, 'tenants'] });
      }
      // Invalidate all tenant queries
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
