import { useMutation, useQuery } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type {
  ApiResponse,
  AuthenticationResponse,
  IntrospectRequest,
  IntrospectResponse,
  LoginFormData,
  RegistrationRequest,
  RegistrationResponse,
} from '../types/auth';

// Login mutation
export const useLoginMutation = () => {
  return useMutation({
    mutationFn: async (
      credentials: LoginFormData,
    ): Promise<ApiResponse<AuthenticationResponse>> => {
      const response = await axiosInstance.post<ApiResponse<AuthenticationResponse>>(
        '/token',
        credentials,
      );
      return response.data;
    },
  });
};

// Introspect mutation
export const useIntrospectMutation = () => {
  return useMutation({
    mutationFn: async (request: IntrospectRequest): Promise<ApiResponse<IntrospectResponse>> => {
      const response = await axiosInstance.post<ApiResponse<IntrospectResponse>>(
        '/auth/introspect',
        request,
      );
      return response.data;
    },
  });
};

// Register mutation
export const useRegisterMutation = () => {
  return useMutation({
    mutationFn: async (
      request: RegistrationRequest,
    ): Promise<ApiResponse<RegistrationResponse>> => {
      const response = await axiosInstance.post<ApiResponse<RegistrationResponse>>(
        '/auth/register',
        request,
      );
      return response.data;
    },
  });
};

// Activate account query
export const useActivateAccount = (token: string | null) => {
  return useQuery({
    queryKey: ['auth', 'activate', token],
    queryFn: async (): Promise<ApiResponse<string>> => {
      if (!token) {
        throw new Error('Token is required');
      }
      const response = await axiosInstance.get<ApiResponse<string>>('/auth/activate', {
        params: { token },
      });
      return response.data;
    },
    enabled: !!token,
    retry: false,
  });
};
