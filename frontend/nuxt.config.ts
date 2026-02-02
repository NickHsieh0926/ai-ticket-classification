export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss', '@pinia/nuxt'],
  css: ['assets/css/main.css'],
  runtimeConfig: {
    // SSR 內部連線 Java，可以在 docker-compose.yml 裡直接覆蓋
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
        proxy: 'http://java-api:8080/ws-ticket/**'
      }
    }
  }

})
