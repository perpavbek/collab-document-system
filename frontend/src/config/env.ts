const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '');

const defaultApiBaseUrl = 'http://localhost:8080';
const defaultWsBaseUrl = 'ws://localhost:8080';

export const env = {
  apiBaseUrl: trimTrailingSlash(import.meta.env.VITE_API_BASE_URL ?? defaultApiBaseUrl),
  wsBaseUrl: trimTrailingSlash(import.meta.env.VITE_WS_BASE_URL ?? defaultWsBaseUrl),
};
