import axios from "axios";

export const api = axios.create({
  timeout: 30000,
});

// 請求攔截器
api.interceptors.request.use((config) => {
  const runtimeConfig = useRuntimeConfig();
  const authStore = useAuthStore();
  const modelStore = useModelStore();

  config.baseURL = runtimeConfig.public.apiBaseUrl;

  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }

  config.headers['X-Model-Type'] = modelStore.modelType;

  return config;
}, (error) => {
  return Promise.reject(error);
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const authStore = useAuthStore();
      authStore.logout();
      if (!import.meta.server) {
        alert("登入逾時或權限不足，請重新登入");
      }
    }
    return Promise.reject(error);
  }
);