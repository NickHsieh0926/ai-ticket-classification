<template>
    <div class="min-h-screen bg-gray-100 flex">
        <!-- Sidebar -->
        <aside class="w-64 bg-white border-r">
            <div class="p-4 font-bold text-lg">
                Ticket Dashboard
            </div>

            <nav class="p-2 space-y-2">
                <NuxtLink to="/" class="block px-3 py-2 rounded hover:bg-gray-100">
                    Home
                </NuxtLink>

                <NuxtLink to="/tickets" class="block px-3 py-2 rounded hover:bg-gray-100">
                    Tickets
                </NuxtLink>

                <NuxtLink to="/upload" class="block px-3 py-2 rounded hover:bg-gray-100">
                    upload
                </NuxtLink>
            </nav>
        </aside>

        <!-- Main -->
        <main class="flex-1 p-6">
            <slot />
            <div class="fixed bottom-5 right-5 z-50 flex flex-col gap-3 w-80 pointer-events-none">
                <TransitionGroup name="task-list">
                    <div v-for="task in taskStore.activeTasks" :key="task.traceId"
                        class="bg-white p-4 shadow-xl border border-gray-100 rounded-2xl pointer-events-auto">
                        <div class="flex justify-between items-center mb-2 font-bold">
                            <span class="text-xs font-mono text-blue-500">ID: {{ task.traceId.slice(0, 8) }}</span>
                            <span :class="task.status === 'COMPLETED' ? 'text-green-500' : 'text-blue-500'"
                                class="text-[10px]">
                                {{ task.status === 'COMPLETED' ? '● 已完成' : '○ 處理中' }}
                            </span>
                        </div>
                        <div class="w-full bg-gray-100 h-2 rounded-full mb-2 overflow-hidden">
                            <div class="bg-blue-600 h-full transition-all duration-500"
                                :style="{ width: task.percentage + '%' }"></div>
                        </div>
                        <div class="flex justify-between items-end">
                            <span class="text-[10px] text-gray-400">{{ task.percentage }}% ({{ task.current }}/{{
                                task.total }})</span>
                            <button v-if="task.status === 'COMPLETED'" @click="navigateTo(`/dashboard?traceId=${task.traceId}`)"
                                class="bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded-lg font-bold">
                                查看分析圖表
                            </button>
                        </div>
                    </div>
                </TransitionGroup>
            </div>
        </main>
    </div>
</template>

<script setup lang="ts">
const { connect } = useTicketSocket()
const taskStore = useTaskStore()
onMounted(() => connect())
</script>
<style scoped>
.task-list-enter-active,
.task-list-leave-active {
    transition: all 0.4s ease;
}

.task-list-enter-from {
    opacity: 0;
    transform: translateX(30px);
}

.task-list-leave-to {
    opacity: 0;
    transform: scale(0.9);
}
</style>