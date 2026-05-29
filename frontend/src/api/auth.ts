import { request, setAuthToken } from './client';

export interface User {
  id: number;
  email: string;
  displayName: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  displayName: string;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const auth = await request<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  setAuthToken(auth.token);
  return auth;
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const auth = await request<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  setAuthToken(auth.token);
  return auth;
}

export function getCurrentUser(): Promise<User> {
  return request<User>('/api/auth/me');
}
