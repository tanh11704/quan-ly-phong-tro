import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../../auth/types/auth';
import type {
  UtilityReadingCreationRequest,
  UtilityReadingResponse,
  UtilityReadingUpdateRequest,
} from '../types/utility-readings';

// Get utility reading by ID
export const useUtilityReading = (id: number | null) => {
  return useQuery({
    queryKey: ['utility-readings', id],
    queryFn: async (): Promise<UtilityReadingResponse> => {
      const response = await axiosInstance.get<ApiResponse<UtilityReadingResponse>>(
        `/utility-readings/${id}`,
      );
      return response.data.result;
    },
    enabled: !!id,
  });
};

// Get utility readings by room ID
export const useUtilityReadingsByRoom = (roomId: number | null) => {
  return useQuery({
    queryKey: ['utility-readings', 'room', roomId],
    queryFn: async (): Promise<UtilityReadingResponse[]> => {
      const response = await axiosInstance.get<ApiResponse<UtilityReadingResponse[]>>(
        `/utility-readings/rooms/${roomId}`,
      );
      return response.data.result;
    },
    enabled: !!roomId,
  });
};

// Get utility readings by building ID and month
export const useUtilityReadingsByBuildingAndMonth = (
  buildingId: number | null,
  month: string | null,
) => {
  return useQuery({
    queryKey: ['utility-readings', 'building', buildingId, month],
    queryFn: async (): Promise<UtilityReadingResponse[]> => {
      const params = new URLSearchParams();
      if (month) {
        params.append('month', month);
      }
      const response = await axiosInstance.get<ApiResponse<UtilityReadingResponse[]>>(
        `/utility-readings/buildings/${buildingId}?${params.toString()}`,
      );
      return response.data.result;
    },
    enabled: !!buildingId && !!month,
  });
};

// Create utility reading mutation
export const useCreateUtilityReading = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (
      request: UtilityReadingCreationRequest,
    ): Promise<ApiResponse<UtilityReadingResponse>> => {
      const response = await axiosInstance.post<ApiResponse<UtilityReadingResponse>>(
        '/utility-readings',
        request,
      );
      return response.data;
    },
    onSuccess: (data) => {
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: ['utility-readings', 'room', data.result.roomId] });
      queryClient.invalidateQueries({
        queryKey: ['utility-readings', 'building'],
      });
      queryClient.invalidateQueries({ queryKey: ['utility-readings'] });
    },
  });
};

// Update utility reading mutation
export const useUpdateUtilityReading = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      request,
    }: {
      id: number;
      request: UtilityReadingUpdateRequest;
    }): Promise<ApiResponse<UtilityReadingResponse>> => {
      const response = await axiosInstance.put<ApiResponse<UtilityReadingResponse>>(
        `/utility-readings/${id}`,
        request,
      );
      return response.data;
    },
    onSuccess: (data, variables) => {
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: ['utility-readings', variables.id] });
      queryClient.invalidateQueries({
        queryKey: ['utility-readings', 'room', data.result.roomId],
      });
      queryClient.invalidateQueries({
        queryKey: ['utility-readings', 'building'],
      });
    },
  });
};
