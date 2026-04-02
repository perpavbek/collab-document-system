import { create } from 'zustand';

import { authApi } from '../api/auth';
import { tokenStorage } from '../lib/storage';
import type { LoginRequest, RegisterRequest, UserProfile } from '../types/auth';

interface AuthState {
  token: string | null;
  user: UserProfile | null;
  isBootstrapping: boolean;
  isSubmitting: boolean;
  bootstrap: () => Promise<void>;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: tokenStorage.get(),
  user: null,
  isBootstrapping: true,
  isSubmitting: false,

  async bootstrap() {
    const token = tokenStorage.get();

    if (!token) {
      set({ token: null, user: null, isBootstrapping: false });
      return;
    }

    try {
      const user = await authApi.getMe();
      set({ token, user, isBootstrapping: false });
    } catch {
      tokenStorage.clear();
      set({ token: null, user: null, isBootstrapping: false });
    }
  },

  async login(payload) {
    set({ isSubmitting: true });

    try {
      const { token } = await authApi.login(payload);
      tokenStorage.set(token);
      const user = await authApi.getMe();

      set({ token, user, isSubmitting: false });
    } catch (error) {
      set({ isSubmitting: false });
      throw error;
    }
  },

  async register(payload) {
    set({ isSubmitting: true });

    try {
      const { token } = await authApi.register(payload);
      tokenStorage.set(token);
      const user = await authApi.getMe();

      set({ token, user, isSubmitting: false });
    } catch (error) {
      set({ isSubmitting: false });
      throw error;
    }
  },

  logout() {
    tokenStorage.clear();
    set({ token: null, user: null });
  },
}));

export const authSelectors = {
  isAuthenticated: () => Boolean(getAuthStoreSnapshot().token),
};

const getAuthStoreSnapshot = () => useAuthStore.getState();
