import { z } from 'zod';

export const TenantResponseSchema = z.object({
  id: z.number(),
  roomId: z.number(),
  roomNo: z.string().optional(),
  name: z.string(),
  phone: z.string(),
  email: z.string().nullable().optional(),
  isContractHolder: z.boolean(),
  startDate: z.string(), // LocalDate từ backend dạng "YYYY-MM-DD"
  endDate: z.string().nullable(), // LocalDate từ backend dạng "YYYY-MM-DD" hoặc null
});

export const TenantCreationRequestSchema = z.object({
  roomId: z.number().min(1, 'ID phòng không được để trống'),
  name: z.string().min(1, 'Tên khách thuê không được để trống'),
  phone: z.string().optional(),
  email: z.string().email('Email không hợp lệ').optional().nullable(),
  isContractHolder: z.boolean().optional().default(false),
  startDate: z.string().optional(), // LocalDate từ backend dạng "YYYY-MM-DD"
});

export const TenantUpdateRequestSchema = z.object({
  name: z.string().min(1, 'Tên khách thuê không được để trống').optional(),
  phone: z.string().optional(),
  email: z.string().email('Email không hợp lệ').optional().nullable(),
  isContractHolder: z.boolean().optional(),
});

export type TenantResponse = z.infer<typeof TenantResponseSchema>;
export type TenantCreationRequest = z.infer<typeof TenantCreationRequestSchema>;
export type TenantUpdateRequest = z.infer<typeof TenantUpdateRequestSchema>;
