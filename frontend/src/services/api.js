import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Cookies (the JWT and the XSRF token) are sent cross-origin only when credentials are enabled.
export const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: { Accept: 'application/json' },
});

// Reads the XSRF-TOKEN cookie that the backend writes. It is not secret; it is echoed back in a header.
function readCookie(name) {
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
  return match ? decodeURIComponent(match[1]) : null;
}

// Fetches a CSRF token if the cookie is missing. Called before any unsafe request.
export async function ensureCsrfToken() {
  if (!readCookie('XSRF-TOKEN')) {
    await api.get('/api/auth/csrf');
  }
}

api.interceptors.request.use(async (config) => {
  const method = (config.method || 'get').toLowerCase();
  if (['post', 'put', 'patch', 'delete'].includes(method)) {
    await ensureCsrfToken();
    const token = readCookie('XSRF-TOKEN');
    if (token) {
      config.headers['X-XSRF-TOKEN'] = token;
    }
  }
  return config;
});

// Normalises backend errors into a single message. A 401 is reported to listeners so the app can
// redirect to login. The token itself is never logged or shown.
export function getErrorMessage(error) {
  return error?.response?.data?.message || 'Something went wrong. Please try again.';
}

export function isUnauthorized(error) {
  return error?.response?.status === 401;
}
