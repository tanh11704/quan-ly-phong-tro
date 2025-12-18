import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../../auth/types/auth';
import type {
  BuildingCreationRequest,
  BuildingResponse,
  BuildingUpdateRequest,
  PageResponse,
  RoomResponse,
} from '../types/buildings';

// Get building by ID
export const useBuilding = (id: number | null) => {
  return useQuery({
    queryKey: ['buildings', id],
    queryFn: async (): Promise<BuildingResponse> => {
      const response = await axiosInstance.get<ApiResponse<BuildingResponse>>(`/buildings/${id}`);
      return response.data.result;
    },
    enabled: !!id,
  });
};

// Get buildings list with pagination
export const useBuildings = (page: number = 0, size: number = 20, sort?: string) => {
  return useQuery({
    queryKey: ['buildings', 'list', page, size, sort],
    queryFn: async (): Promise<PageResponse<BuildingResponse>> => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (sort) {
        params.append('sort', sort);
      }
      const response = await axiosInstance.get<PageResponse<BuildingResponse>>(
        `/buildings?${params.toString()}`,
      );
      return response.data;
    },
  });
};

// Get rooms by building ID with pagination and status filter
export const useBuildingRooms = (
  buildingId: number | null,
  page: number = 0,
  size: number = 20,
  sort?: string,
  status?: string | null,
) => {
  return useQuery({
    queryKey: ['buildings', buildingId, 'rooms', page, size, sort, status],
    queryFn: async (): Promise<PageResponse<RoomResponse>> => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (sort) {
        params.append('sort', sort);
      }
      if (status) {
        params.append('status', status);
      }
      const response = await axiosInstance.get<PageResponse<RoomResponse>>(
        `/buildings/${buildingId}/rooms?${params.toString()}`,
      );
      return response.data;
    },
    enabled: !!buildingId,
  });
};

// Create building mutation
export const useCreateBuilding = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (
      request: BuildingCreationRequest,
    ): Promise<ApiResponse<BuildingResponse>> => {
      const response = await axiosInstance.post<ApiResponse<BuildingResponse>>(
        '/buildings',
        request,
      );
      return response.data;
    },
    onSuccess: () => {
      // Invalidate buildings list queries
      queryClient.invalidateQueries({ queryKey: ['buildings'] });
    },
  });
};

// Update building mutation
export const useUpdateBuilding = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      request,
    }: {
      id: number;
      request: BuildingUpdateRequest;
    }): Promise<ApiResponse<BuildingResponse>> => {
      const response = await axiosInstance.put<ApiResponse<BuildingResponse>>(
        `/buildings/${id}`,
        request,
      );
      return response.data;
    },
    onSuccess: (_, variables) => {
      // Invalidate buildings list and specific building queries
      queryClient.invalidateQueries({ queryKey: ['buildings'] });
      queryClient.invalidateQueries({ queryKey: ['buildings', variables.id] });
    },
  });
};

// Delete building mutation
export const useDeleteBuilding = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number): Promise<ApiResponse<void>> => {
      const response = await axiosInstance.delete<ApiResponse<void>>(`/buildings/${id}`);
      return response.data;
    },
    onSuccess: () => {
      // Invalidate buildings list queries
      queryClient.invalidateQueries({ queryKey: ['buildings'] });
    },
  });
};
