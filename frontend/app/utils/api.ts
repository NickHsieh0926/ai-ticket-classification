import axios from "axios";

export const api = axios.create({
  timeout: 10000,
});

// 攔截器：動態獲取當前環境應用的 Base URL
api.interceptors.request.use((config) => {
  const runtimeConfig = useRuntimeConfig();
  
  if (import.meta.server) {
    //執行 (SSR)，連線到 Docker 內部名稱
    config.baseURL = runtimeConfig.apiInternalUrl;
  } else {
    //瀏覽器端執行，連線到外部可存取的 URL
    config.baseURL = runtimeConfig.public.apiBaseUrl;
  }
  
  return config;
});