import axios from 'axios';
import { useAuthStore } from './store/authStore.js';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8090',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

client.interceptors.response.use(
  (res) => res,
  (error) => {
    const data = error.response?.data;
    const message =
      (data && data.message) ||
      (typeof data === 'string' && data) ||
      error.message ||
      'Request failed';
    return Promise.reject(Object.assign(new Error(message), { status: error.response?.status }));
  }
);

export const api = {
  register: (payload) => client.post('/api/auth/register', payload).then((r) => r.data),
  login: (payload) => client.post('/api/auth/login', payload).then((r) => r.data),

  createWallet: (payload) => client.post('/api/wallets', payload).then((r) => r.data),
  getWallets: (userId) => client.get(`/api/wallets/user/${userId}`).then((r) => r.data),

  topUp: (payload) => client.post('/api/payment/topup', payload).then((r) => r.data),
  transfer: (payload) => client.post('/api/payment/transfer', payload).then((r) => r.data),

  ledgerByAccount: (accountNo) =>
    client.get(`/api/ledger/accounts/${accountNo}/transactions`).then((r) => r.data),
};
