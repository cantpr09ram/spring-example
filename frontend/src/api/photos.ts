import { request } from './client';

export interface UploadedPhoto {
  filename: string;
  contentType: string;
  size: number;
  url: string;
}

export async function uploadPhoto(file: File): Promise<UploadedPhoto> {
  const formData = new FormData();
  formData.append('file', file);

  return request<UploadedPhoto>('/api/photos', {
    method: 'POST',
    body: formData,
  });
}
