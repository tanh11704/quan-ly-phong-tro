import { z } from 'zod';

export enum Role {
  ADMIN = 'ADMIN',
  TENANT = 'TENANT',
  MANAGER = 'MANAGER',
}

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
  role: z.nativeEnum(Role),
});

export type AuthenticationResponse = z.infer<typeof AuthenticationResponseSchema>;

export const IntrospectResponseSchema = z.object({
  valid: z.boolean(),
});

export type IntrospectResponse = z.infer<typeof IntrospectResponseSchema>;

export const ApiResponseSchema = <T extends z.ZodTypeAny>(dataSchema: T) =>
  z.object({
    code: z.number().optional(),
    result: dataSchema,
    message: z.string(),
  });

export type ApiResponse<T> = {
  code?: number;
  result: T;
  message: string;
};

// Registration types
export const RegistrationRequestSchema = z.object({
  username: z
    .string()
    .min(3, 'Tên đăng nhập phải có ít nhất 3 ký tự')
    .max(50, 'Tên đăng nhập không được quá 50 ký tự'),
  password: z
    .string()
    .min(6, 'Mật khẩu phải có ít nhất 6 ký tự')
    .max(100, 'Mật khẩu không được quá 100 ký tự'),
  fullName: z
    .string()
    .min(1, 'Họ và tên không được để trống')
    .max(100, 'Họ và tên không được quá 100 ký tự'),
  email: z.string().email('Email không hợp lệ'),
});

export type RegistrationRequest = z.infer<typeof RegistrationRequestSchema>;

export const RegistrationResponseSchema = z.object({
  userId: z.string(),
  message: z.string(),
});

export type RegistrationResponse = z.infer<typeof RegistrationResponseSchema>;
