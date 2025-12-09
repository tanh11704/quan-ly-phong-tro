import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { Role } from '../features/auth/types/auth';

interface AuthState {
  isAuthenticated: boolean;
  isInitialized: boolean;
  role: Role | null;
}

const initialState: AuthState = {
  isAuthenticated: false,
  isInitialized: false,
  role: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setAuthenticated: (state, action: PayloadAction<boolean>) => {
      state.isAuthenticated = action.payload;
    },
    setInitialized: (state, action: PayloadAction<boolean>) => {
      state.isInitialized = action.payload;
    },
    setRole: (state, action: PayloadAction<Role>) => {
      state.role = action.payload;
    },
    logout: (state) => {
      state.isAuthenticated = false;
      state.role = null;
    },
  },
});

export const { setAuthenticated, setInitialized, setRole, logout } = authSlice.actions;
export default authSlice.reducer;
