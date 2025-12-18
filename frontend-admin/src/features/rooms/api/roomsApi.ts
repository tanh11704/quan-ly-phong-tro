import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../../auth/types/auth';
import type { PageResponse } from '../../buildings/types/buildings';
import type { RoomCreationRequest, RoomResponse, RoomUpdateRequest } from '../types/rooms';
import type { TenantResponse } from '../types/tenants';

// Get room by ID
export const useRoom = (id: number | null) => {
  return useQuery({
    queryKey: ['rooms', id],
    queryFn: async (): Promise<RoomResponse> => {
      const response = await axiosInstance.get<ApiResponse<RoomResponse>>(`/rooms/${id}`);
      return response.data.result;
    },
    enabled: !!id,
  });
};

// Create room mutation
export const useCreateRoom = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: RoomCreationRequest): Promise<ApiResponse<RoomResponse>> => {
      const response = await axiosInstance.post<ApiResponse<RoomResponse>>('/rooms', request);
      return response.data;
    },
    onSuccess: (_, variables) => {
      // Invalidate rooms list and building rooms queries
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      queryClient.invalidateQueries({ queryKey: ['buildings', variables.buildingId, 'rooms'] });
    },
  });
};

// Update room mutation
export const useUpdateRoom = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      request,
    }: {
      id: number;
      request: RoomUpdateRequest;
    }): Promise<ApiResponse<RoomResponse>> => {
      const response = await axiosInstance.put<ApiResponse<RoomResponse>>(`/rooms/${id}`, request);
      return response.data;
    },
    onSuccess: (data, variables) => {
      // Invalidate rooms list and specific room queries
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      queryClient.invalidateQueries({ queryKey: ['rooms', variables.id] });
      // Also invalidate building rooms if we have buildingId
      if (data.result?.buildingId) {
        queryClient.invalidateQueries({
          queryKey: ['buildings', data.result.buildingId, 'rooms'],
        });
      }
    },
  });
};

// Delete room mutation
export const useDeleteRoom = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number): Promise<ApiResponse<void>> => {
      const response = await axiosInstance.delete<ApiResponse<void>>(`/rooms/${id}`);
      return response.data;
    },
    onSuccess: () => {
      // Invalidate rooms list queries
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      queryClient.invalidateQueries({ queryKey: ['buildings'] });
    },
  });
};

// Get tenants by room ID with pagination
export const useRoomTenants = (
  roomId: number | null,
  page: number = 0,
  size: number = 20,
  sort?: string,
) => {
  return useQuery({
    queryKey: ['rooms', roomId, 'tenants', page, size, sort],
    queryFn: async (): Promise<PageResponse<TenantResponse>> => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (sort) {
        params.append('sort', sort);
      }
      const response = await axiosInstance.get<PageResponse<TenantResponse>>(
        `/rooms/${roomId}/tenants?${params.toString()}`,
      );
      return response.data;
    },
    enabled: !!roomId,
  });
};
