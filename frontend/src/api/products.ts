import { request } from './client';

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stockQuantity: number;
  category: string;
  imageUrls: string[];
  active: boolean;
  createdAt: string;
}

export interface ProductPayload {
  name: string;
  description?: string | null;
  price: number;
  stockQuantity: number;
  category: string;
  imageUrls: string[];
  active?: boolean;
}

export async function listProducts(): Promise<Product[]> {
  return request<Product[]>('/api/products');
}

export async function createProduct(payload: ProductPayload): Promise<Product> {
  return request<Product>('/api/products', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function updateProduct(id: number, payload: ProductPayload): Promise<Product> {
  return request<Product>(`/api/products/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export async function deleteProduct(id: number): Promise<void> {
  await request<void>(`/api/products/${id}`, {
    method: 'DELETE',
  });
}
