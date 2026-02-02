import { defineStore } from 'pinia'
import { api } from '@/utils/api'
import { useTicketSocket } from '@/composables/useTicketSocket'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: useCookie<string | null>('auth_token').value || null,
        user: null as any // 存使用者資訊
    }),
    actions: {
        async login(credentials: { username: string; password: string }) {
            try {
                const response = await api.post<{ token: string }>('/api/auth/login', credentials)
                const token = response.data.token
                this.token = token

                const tokenCookie = useCookie('auth_token', {
                    path: '/',
                    sameSite: 'lax'
                })
                tokenCookie.value = token

                if (import.meta.client) {
                    sessionStorage.setItem('page_auth_flag', 'true')
                }

                useTicketSocket().connect()

                return true
            } catch (error: any) {
                console.error('登入 Store 報錯:', error.response?.data || error.message)
                return false
            }
        },

        async checkAuth() {
            if (!this.token) throw new Error('No Token');

            const config = useRuntimeConfig()
            const baseURL = import.meta.server ? config.apiInternalUrl : config.public.apiBaseUrl

            try {
                const response = await $fetch<{ user: string }>('/api/auth/getUserInfo', {
                    method: 'GET',
                    baseURL: baseURL,
                    headers: {
                        Authorization: `Bearer ${this.token}`
                    }
                })
                this.user = response
                return true;
            } catch (error) {
                this.token = null
                this.user = null
                throw error
            }
        },

        logout() {
            this.token = null
            useCookie('auth_token').value = null
            sessionStorage.removeItem('page_auth_flag')
            useTicketSocket().disconnect()
            navigateTo('/login')
        }
    }
})