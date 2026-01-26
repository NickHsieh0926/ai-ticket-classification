// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss', '@pinia/nuxt'],
  css: ['assets/css/main.css'],
  runtimeConfig: {
    // SSR 內部連線 Java
    apiInternalUrl: 'http://localhost:8080',
    public: {
      // 瀏覽器端發送到 Nuxt 的前綴
      apiBaseUrl: 'http://localhost:8080'
    }
  },

  nitro: {
    routeRules: {
      '/api/**': {
        proxy: 'http://java-api:8080/api/**',
      },
      '/ws-ticket/**': {
        proxy: 'http://java-api:8080/ws-ticket'
      }
    }
  }
  
})
