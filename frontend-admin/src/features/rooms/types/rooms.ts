import { z } from 'zod';

export enum RoomStatus {
  VACANT = 'VACANT',
  OCCUPIED = 'OCCUPIED',
  MAINTENANCE = 'MAINTENANCE',
}

export const RoomResponseSchema = z.object({
  id: z.number(),
  buildingId: z.number(),
  buildingName: z.string(),
  roomNo: z.string(),
  price: z.number(),
  status: z.string(),
});

export const RoomCreationRequestSchema = z.object({
  buildingId: z.number().min(1, 'Tòa nhà không được để trống'),
  roomNo: z.string().min(1, 'Số phòng không được để trống'),
  price: z.number().min(0, 'Giá thuê phải >= 0'),
  status: z.nativeEnum(RoomStatus).optional(),
});

export const RoomUpdateRequestSchema = z.object({
  roomNo: z.string().min(1, 'Số phòng không được để trống').optional(),
  price: z.number().min(0, 'Giá thuê phải >= 0').optional(),
  status: z.nativeEnum(RoomStatus).optional(),
});

export type RoomResponse = z.infer<typeof RoomResponseSchema>;
export type RoomCreationRequest = z.infer<typeof RoomCreationRequestSchema>;
export type RoomUpdateRequest = z.infer<typeof RoomUpdateRequestSchema>;
