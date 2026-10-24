import { api, ensureCsrfToken } from './api';

// Login starts the Google OAuth2 flow on the backend. The browser navigates there directly.
export function loginWithGoogle() {
  const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  window.location.href = `${base}/oauth2/authorization/google`;
}

export async function getCurrentUser() {
  const { data } = await api.get('/api/users/me');
  return data;
}

export async function logout() {
  await ensureCsrfToken();
  await api.post('/api/auth/logout');
}
