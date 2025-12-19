import { useQuery } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type { ApiResponse } from '../types/auth';
import type { UserDTO } from '../types/users';

// Get current user info
export const useMyInfo = () => {
  return useQuery({
    queryKey: ['users', 'my-info'],
    queryFn: async (): Promise<UserDTO> => {
      const response = await axiosInstance.get<ApiResponse<UserDTO>>('/users/my-info');
      return response.data.result;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  });
};
