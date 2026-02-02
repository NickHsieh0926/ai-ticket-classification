<template>
    <div class="flex items-center justify-center min-h-screen bg-slate-50">
        <div class="p-8 bg-white shadow-2xl rounded-3xl w-full max-w-md border border-gray-100">
            <h1 class="text-3xl font-extrabold mb-8 text-center text-slate-800">AI 工單分析系統</h1>
            <form @submit.prevent="handleLogin" class="space-y-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">帳號</label>
                    <input v-model="form.username" type="text"
                        class="w-full p-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition"
                        required />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">密碼</label>
                    <input v-model="form.password" type="password"
                        class="w-full p-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition"
                        required />
                </div>
                <button type="submit"
                    class="w-full bg-blue-600 text-white py-4 rounded-2xl font-bold text-lg hover:bg-blue-700 active:scale-[0.98] transition-all">
                    進入系統
                </button>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/useAuthStore'

definePageMeta({ layout: 'defaultlogin' });

const authStore = useAuthStore()
const form = ref({ username: '', password: '' });

const handleLogin = async () => {
    const success = await authStore.login(form.value)
    if (success) {
        navigateTo('/')
    } else {
        alert('登入失敗，請確認帳號密碼')
    }
}
</script>