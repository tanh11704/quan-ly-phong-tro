import { z } from 'zod';

export const TenantResponseSchema = z.object({
  id: z.number(),
  roomId: z.number(),
  roomNo: z.string().optional(),
  name: z.string(),
  phone: z.string(),
  isContractHolder: z.boolean(),
  startDate: z.string(), // LocalDate từ backend dạng "YYYY-MM-DD"
  endDate: z.string().nullable(), // LocalDate từ backend dạng "YYYY-MM-DD" hoặc null
});

export const TenantCreationRequestSchema = z.object({
  roomId: z.number().min(1, 'ID phòng không được để trống'),
  name: z.string().min(1, 'Tên khách thuê không được để trống'),
  phone: z.string().optional(),
  isContractHolder: z.boolean().optional().default(false),
  startDate: z.string().optional(), // LocalDate từ backend dạng "YYYY-MM-DD"
});

export type TenantResponse = z.infer<typeof TenantResponseSchema>;
export type TenantCreationRequest = z.infer<typeof TenantCreationRequestSchema>;
