import { createApi } from '@reduxjs/toolkit/query/react';
import { axiosBaseQuery } from '../lib/axiosBaseQuery';

export const api = createApi({
  reducerPath: 'api',
  baseQuery: axiosBaseQuery(),
  tagTypes: [], // Thêm các tag types khi cần (VD: 'User', 'Product')
  endpoints: () => ({}),
});
