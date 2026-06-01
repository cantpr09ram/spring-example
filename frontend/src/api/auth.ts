import { request, setAuthToken } from './client';

export interface User {
  id: number;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
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
  role?: 'USER' | 'ADMIN';
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const auth = await request<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  setAuthToken(auth.token);
  return auth;
}

export function register(payload: RegisterPayload): Promise<User> {
  return request<User>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function getCurrentUser(): Promise<User> {
  return request<User>('/api/auth/me');
}

export function listUsers(): Promise<User[]> {
  return request<User[]>('/api/auth/users');
}
