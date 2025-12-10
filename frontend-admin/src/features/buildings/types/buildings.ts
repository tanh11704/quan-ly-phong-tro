import { z } from 'zod';

export enum WaterCalcMethod {
  BY_METER = 'BY_METER',
  PER_CAPITA = 'PER_CAPITA',
}

export const BuildingResponseSchema = z.object({
  id: z.number(),
  name: z.string(),
  ownerName: z.string().optional(),
  ownerPhone: z.string().optional(),
  elecUnitPrice: z.number().optional(),
  waterUnitPrice: z.number().optional(),
  waterCalcMethod: z.nativeEnum(WaterCalcMethod).optional(),
});

export const BuildingCreationRequestSchema = z.object({
  name: z.string().min(1, 'Tên tòa nhà không được để trống'),
  ownerName: z.string().optional(),
  ownerPhone: z.string().optional(),
  elecUnitPrice: z.number().min(0, 'Đơn giá điện phải >= 0').optional(),
  waterUnitPrice: z.number().min(0, 'Đơn giá nước phải >= 0').optional(),
  waterCalcMethod: z.nativeEnum(WaterCalcMethod),
});

export const RoomResponseSchema = z.object({
  id: z.number(),
  buildingId: z.number(),
  buildingName: z.string(),
  roomNo: z.string(),
  price: z.number(),
  status: z.string(),
});

// PageResponse types
export const PageInfoSchema = z.object({
  page: z.number(),
  size: z.number(),
  totalElements: z.number(),
  totalPages: z.number(),
  first: z.boolean(),
  last: z.boolean(),
});

export const PageResponseSchema = <T extends z.ZodTypeAny>(contentSchema: T) =>
  z.object({
    code: z.number().optional(),
    message: z.string().optional(),
    content: z.array(contentSchema),
    page: PageInfoSchema,
  });

export type PageInfo = z.infer<typeof PageInfoSchema>;
export type PageResponse<T> = {
  code?: number;
  message?: string;
  content: T[];
  page: PageInfo;
};

// BuildingUpdateRequest - all fields optional
export const BuildingUpdateRequestSchema = z.object({
  name: z.string().min(1, 'Tên tòa nhà không được để trống').optional(),
  ownerName: z.string().optional(),
  ownerPhone: z.string().optional(),
  elecUnitPrice: z.number().min(0, 'Đơn giá điện phải >= 0').optional(),
  waterUnitPrice: z.number().min(0, 'Đơn giá nước phải >= 0').optional(),
  waterCalcMethod: z.nativeEnum(WaterCalcMethod).optional(),
});

export type BuildingResponse = z.infer<typeof BuildingResponseSchema>;
export type BuildingCreationRequest = z.infer<typeof BuildingCreationRequestSchema>;
export type BuildingUpdateRequest = z.infer<typeof BuildingUpdateRequestSchema>;
export type RoomResponse = z.infer<typeof RoomResponseSchema>;
