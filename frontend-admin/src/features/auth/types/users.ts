import { z } from 'zod';
import { Role } from './auth';

export const UserDTOSchema = z.object({
  id: z.string().uuid(),
  username: z.string().nullable().optional(),
  fullName: z.string(),
  role: z.nativeEnum(Role),
  active: z.boolean(),
});

export type UserDTO = z.infer<typeof UserDTOSchema>;
