import { useMutation } from '@tanstack/react-query';
import axiosInstance from '../../../lib/axios';
import type {
  ApiResponse,
  AuthenticationResponse,
  IntrospectRequest,
  IntrospectResponse,
  LoginFormData,
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
