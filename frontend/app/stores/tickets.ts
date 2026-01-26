import { defineStore } from 'pinia'
import { api } from '@/utils/api'
import { filterTickets } from '@/utils/filterHelper'
import type { PredictionResult } from '@/types/prediction'

export const useTicketsStore = defineStore('tickets', {
    state: () => ({
        tickets: [] as PredictionResult[],

        // pagination
        page: 1,
        pageSize: 5,

        // API UI/UX
        loading: false,
        error: null as string | null,

        // 新增 filter state
        filterLabel: "" as string,      
        minConfidence: 0,              
    }),

    getters: {
        filteredTickets(state) {
            return filterTickets(state.tickets, state.filterLabel, state.minConfidence)
        },
        paginatedTickets: (state) => {
            const start = (state.page - 1) * state.pageSize
            return filterTickets(state.tickets, state.filterLabel, state.minConfidence).slice(start, start + state.pageSize)
        },
        totalPages: (state) => {
            return Math.ceil(filterTickets(state.tickets, state.filterLabel, state.minConfidence).length / state.pageSize)
        },
        categoryDistribution() {
            const map: Record<string, number> = {}

            this.filteredTickets.forEach(t => {
                map[t.predictedLabel] = (map[t.predictedLabel] || 0) + 1
            })

            return {
                labels: Object.keys(map),
                values: Object.values(map)
            }
        },
        confidenceHistogram() {
            const buckets = {
                "0-60": 0,
                "60-70": 0,
                "70-80": 0,
                "80-90": 0,
                "90-100": 0
            }

            this.filteredTickets.forEach(t => {
                const c = t.confidence * 100
                if (c < 60) buckets["0-60"]++
                else if (c < 70) buckets["60-70"]++
                else if (c < 80) buckets["70-80"]++
                else if (c < 90) buckets["80-90"]++
                else buckets["90-100"]++
            })

            return {
                labels: Object.keys(buckets),
                values: Object.values(buckets)
            }
        }
    },

    actions: {
        async predictOne(text: string) {
            this.loading = true
            try {
                const { data } = await api.post('/api/tickets/predict', { text })
                this.tickets.push(data)
            } finally {
                this.loading = false
            }
        },
        async predictBatch(texts: string[]) {
            this.loading = true
            try {
                const { data } = await api.post('/api/tickets/predict/batch', { texts })
                this.tickets.push(...data)
            } finally {
                this.loading = false
            }
        },
        setPage(page: number) {
            this.page = page
        },
        setFilterLabel(label: string) {
            this.filterLabel = label
            this.page = 1 
        },
        setMinConfidence(value: number) {
            this.minConfidence = value
            this.page = 1
        }
    }
})