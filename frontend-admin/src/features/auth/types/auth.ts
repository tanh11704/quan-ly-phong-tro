import { z } from 'zod';

export const LoginSchema = z.object({
  username: z.string().min(1, 'Tên đăng nhập không được để trống'),
  password: z.string().min(1, 'Mật khẩu không được để trống'),
});

export type LoginFormData = z.infer<typeof LoginSchema>;

export const IntrospectRequestSchema = z.object({
  token: z.string().min(1, 'Token không được để trống'),
});

export type IntrospectRequest = z.infer<typeof IntrospectRequestSchema>;

export const AuthenticationResponseSchema = z.object({
  token: z.string(),
});

export type AuthenticationResponse = z.infer<typeof AuthenticationResponseSchema>;

export const IntrospectResponseSchema = z.object({
  valid: z.boolean(),
});

export type IntrospectResponse = z.infer<typeof IntrospectResponseSchema>;

export const ApiResponseSchema = <T extends z.ZodTypeAny>(dataSchema: T) =>
  z.object({
    result: dataSchema,
    message: z.string(),
  });

export type ApiResponse<T> = {
  result: T;
  message: string;
};
