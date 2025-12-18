import type { AxiosError } from 'axios';

export interface ApiErrorResponse {
  code?: number;
  message?: string;
  result?: unknown;
}

/**
 * Extract error message from Axios error response
 * Backend trả về ApiResponse với structure: { code?: number, message: string }
 */
export const getErrorMessage = (error: unknown, defaultMessage?: string): string => {
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as AxiosError<ApiErrorResponse>;
    const errorData = axiosError.response?.data;
    if (errorData?.message) {
      return errorData.message;
    }
  }
  return defaultMessage || 'Đã xảy ra lỗi. Vui lòng thử lại.';
};

/**
 * Extract error code from Axios error response
 */
export const getErrorCode = (error: unknown): number | undefined => {
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as AxiosError<ApiErrorResponse>;
    return axiosError.response?.data?.code;
  }
  return undefined;
};
