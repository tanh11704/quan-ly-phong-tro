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

export type TenantResponse = z.infer<typeof TenantResponseSchema>;
