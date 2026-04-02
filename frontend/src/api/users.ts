import { http } from '../lib/http';
import type { PageResponse } from '../types/api';
import type { UserProfile } from '../types/auth';

export const usersApi = {
  async getById(id: string) {
    const { data } = await http.get<UserProfile>(`/users/id/${id}`);
    return data;
  },

  async searchByName(name: string, page = 0, size = 10) {
    const { data } = await http.get<PageResponse<UserProfile>>('/users/search', {
      params: { name, page, size },
    });
    return data;
  },
};
