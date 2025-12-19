import { z } from 'zod';

export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  UNPAID = 'UNPAID',
  PAID = 'PAID',
}

export const InvoiceResponseSchema = z.object({
  id: z.number(),
  roomNo: z.string(),
  tenantName: z.string(),
  period: z.string(), // Format: 'YYYY-MM'
  roomPrice: z.number(),
  elecAmount: z.number(),
  waterAmount: z.number(),
  totalAmount: z.number(),
  status: z.nativeEnum(InvoiceStatus),
  dueDate: z.string(), // LocalDate từ backend dạng "YYYY-MM-DD"
});

export const InvoiceCreationRequestSchema = z.object({
  buildingId: z.number().min(1, 'ID tòa nhà không được để trống'),
  period: z.string().regex(/^\d{4}-\d{2}$/, 'Kỳ thanh toán phải có định dạng YYYY-MM'),
});

// InvoiceDetailResponse - có thể có thêm thông tin chi tiết như chỉ số điện nước cũ/mới
export const InvoiceDetailResponseSchema = z.object({
  id: z.number(),
  roomNo: z.string(),
  tenantName: z.string(),
  period: z.string(),
  roomPrice: z.number(),
  elecAmount: z.number(),
  waterAmount: z.number(),
  totalAmount: z.number(),
  status: z.nativeEnum(InvoiceStatus),
  dueDate: z.string(),
  // Thêm các fields chi tiết nếu có
  oldElectricIndex: z.number().nullable().optional(),
  newElectricIndex: z.number().nullable().optional(),
  oldWaterIndex: z.number().nullable().optional(),
  newWaterIndex: z.number().nullable().optional(),
  electricUsage: z.number().nullable().optional(), // kWh
  waterUsage: z.number().nullable().optional(), // m³
  elecUnitPrice: z.number().nullable().optional(),
  waterUnitPrice: z.number().nullable().optional(),
  createdAt: z.string().optional(),
  paidAt: z.string().nullable().optional(),
});

export type InvoiceResponse = z.infer<typeof InvoiceResponseSchema>;
export type InvoiceCreationRequest = z.infer<typeof InvoiceCreationRequestSchema>;
export type InvoiceDetailResponse = z.infer<typeof InvoiceDetailResponseSchema>;
