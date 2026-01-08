// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss', '@pinia/nuxt'],
  css: ['assets/css/main.css'],
  runtimeConfig: {
    // SSR 內部連線依然直連 Java
    apiInternalUrl: 'http://localhost:8080',
    public: {
      // 瀏覽器端發送到 Nuxt 的前綴
      apiBaseUrl: '/api'
    }
  },

  nitro: {
    routeRules: {
      '/api/**': {
        proxy: 'http://java-api:8080/**',
      }
    }
  }
})
