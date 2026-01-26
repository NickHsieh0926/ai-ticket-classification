import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { useAuthStore } from '@/stores/useAuthStore'
import { useTaskStore } from '@/stores/useTaskStore'

let stompClient: Client | null = null

export const useTicketSocket = () => {
    const authStore = useAuthStore()
    const taskStore = useTaskStore()

    const connect = () => {
        if (import.meta.server || stompClient?.active || !authStore.token) return

        // const socket = new SockJS('/ws-ticket')
        const socket = new SockJS('http://localhost:8080/ws-ticket')
        stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: { Authorization: `Bearer ${authStore.token}` },
            reconnectDelay: 5000,
            debug: (msg) => console.log('STOMP Debug:', msg), // 開啟除錯日誌
            onConnect: () => {
                console.log('STOMP 連線成功')
                Object.keys(taskStore.activeTasks).forEach(id => subscribeToTask(id))
            }
        })
        stompClient.activate()
    }

    const subscribeToTask = (traceId: string) => {
        if (stompClient?.connected) {
            stompClient.subscribe(`/topic/progress/${traceId}`, (msg) => {
                taskStore.updateTask(JSON.parse(msg.body))
            })
        }
    }

    const disconnect = () => {
        stompClient?.deactivate()
        stompClient = null
    }

    return { connect, subscribeToTask, disconnect }
}