import { http } from '../lib/http';
import type { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '../types/auth';

export const authApi = {
  async login(payload: LoginRequest) {
    const { data } = await http.post<AuthResponse>('/users/auth/login', payload);
    return data;
  },

  async register(payload: RegisterRequest) {
    const { data } = await http.post<AuthResponse>('/users/auth/register', payload);
    return data;
  },

  async getMe() {
    const { data } = await http.get<UserProfile>('/users/me');
    return data;
  },
};
