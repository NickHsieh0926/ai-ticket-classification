import { defineStore } from 'pinia'
import { api } from '@/utils/api'
import { useTicketSocket } from '@/composables/useTicketSocket';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: useCookie('auth_token').value || null,
        isAuthenticated: !!useCookie('auth_token').value
    }),
    actions: {
        async login(credentials: { username: string; password: string }) {
            try {
                const response = await api.post<{ token: string }>('/api/auth/login', credentials)

                const token = response.data.token

                this.token = token
                this.isAuthenticated = true

                const tokenCookie = useCookie('auth_token', {
                    maxAge: 60 * 60 * 24, 
                    path: '/',            
                    sameSite: 'lax'
                })
                tokenCookie.value = token

                useTicketSocket().connect()

                return true
            } catch (error: any) {
                console.error('登入 Store 報錯:', error.response?.data || error.message)
                return false
            }
        },

        logout() {
            this.token = null
            this.isAuthenticated = false
            useCookie('auth_token').value = null
            useTicketSocket().disconnect()
            navigateTo('/login')
        }
    }
})