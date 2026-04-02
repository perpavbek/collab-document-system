import axios from 'axios';

import { env } from '../config/env';
import { tokenStorage } from './storage';

export const http = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

http.interceptors.request.use((config) => {
  const token = tokenStorage.get();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status as number | undefined;

    if (status === 401 && window.location.pathname !== '/login' && window.location.pathname !== '/register') {
      tokenStorage.clear();
      window.location.assign('/login');
    }

    return Promise.reject(error);
  },
);
