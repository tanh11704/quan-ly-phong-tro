import { api } from '../../../stores/api';
import type {
  ApiResponse,
  AuthenticationResponse,
  IntrospectRequest,
  IntrospectResponse,
  LoginFormData,
} from '../types/auth';

export const authApi = api.injectEndpoints({
  endpoints: (builder) => ({
    login: builder.mutation<ApiResponse<AuthenticationResponse>, LoginFormData>({
      query: (credentials) => ({
        url: '/token',
        method: 'POST',
        data: credentials,
      }),
    }),
    introspect: builder.mutation<ApiResponse<IntrospectResponse>, IntrospectRequest>({
      query: (request) => ({
        url: '/auth/introspect',
        method: 'POST',
        data: request,
      }),
    }),
  }),
});

export const { useLoginMutation, useIntrospectMutation } = authApi;
