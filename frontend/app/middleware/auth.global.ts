export default defineNuxtRouteMiddleware(async (to, from) => {
    const tokenCookie = useCookie('auth_token')
    const authStore = useAuthStore()

    if (to.path === '/login') return

    if (!tokenCookie.value) {
        return navigateTo('/login')
    }

    if (import.meta.client) {
        const hasPageFlag = sessionStorage.getItem('page_auth_flag')

        if (!hasPageFlag) {
            return navigateTo('/login')
        }
    }

    if (tokenCookie.value && !authStore.user) {
        try {
            await authStore.checkAuth()
        } catch (err) {
            return navigateTo('/login')
        }
    }
})