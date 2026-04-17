import { defineStore } from 'pinia'

export const useTaskStore = defineStore('task', {
    state: () => ({
        activeTasks: {} as Record<string, any>,
    }),
    actions: {
        updateTask(payload: any) {
            if (this.activeTasks[payload.traceId]?.status === 'COMPLETED') return

            this.activeTasks[payload.traceId] = payload

            if (payload.status === 'COMPLETED') {
                setTimeout(() => {
                    delete this.activeTasks[payload.traceId]
                }, 30000)
            }
        }
    }
})