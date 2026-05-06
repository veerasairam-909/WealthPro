import axios from 'axios';
import { useAuth } from '@/auth/store';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9999';

export const api = axios.create({
  baseURL: BASE_URL,
});

// add token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

// handle 401 - clear auth state and redirect to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response && err.response.status === 401) {
      // clears token from localStorage AND resets Zustand user state
      useAuth.getState().logout();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);
