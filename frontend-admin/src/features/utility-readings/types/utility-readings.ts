import { z } from 'zod';

export const UtilityReadingResponseSchema = z.object({
  id: z.number(),
  roomId: z.number(),
  roomNo: z.string().optional(),
  month: z.string(), // Format: 'YYYY-MM'
  electricIndex: z.number().nullable(),
  waterIndex: z.number().nullable(),
  imageEvidence: z.string().nullable().optional(),
  createdAt: z.string(), // LocalDateTime từ backend
});

export const UtilityReadingCreationRequestSchema = z.object({
  roomId: z.number().min(1, 'ID phòng không được để trống'),
  month: z.string().regex(/^\d{4}-\d{2}$/, 'Tháng phải có định dạng YYYY-MM'),
  electricIndex: z.number().min(0, 'Chỉ số điện phải >= 0').nullable().optional(),
  waterIndex: z.number().min(0, 'Chỉ số nước phải >= 0').nullable().optional(),
  isMeterReset: z.boolean().optional().default(false),
  imageEvidence: z.string().url('URL ảnh không hợp lệ').optional().nullable(),
});

export const UtilityReadingUpdateRequestSchema = z.object({
  electricIndex: z.number().min(0, 'Chỉ số điện phải >= 0').nullable().optional(),
  waterIndex: z.number().min(0, 'Chỉ số nước phải >= 0').nullable().optional(),
  isMeterReset: z.boolean().optional(),
  imageEvidence: z.string().url('URL ảnh không hợp lệ').optional().nullable(),
});

export type UtilityReadingResponse = z.infer<typeof UtilityReadingResponseSchema>;
export type UtilityReadingCreationRequest = z.infer<typeof UtilityReadingCreationRequestSchema>;
export type UtilityReadingUpdateRequest = z.infer<typeof UtilityReadingUpdateRequestSchema>;
