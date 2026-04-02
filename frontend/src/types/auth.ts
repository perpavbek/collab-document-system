export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  name: string;
}

export interface AuthResponse {
  token: string;
}

export interface UserProfile {
  id: string;
  email: string;
  name: string;
  role: string;
}
