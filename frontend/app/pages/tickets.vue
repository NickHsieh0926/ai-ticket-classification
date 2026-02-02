<template>
    <div class="p-4 space-y-4">

        <TicketSearch @search="handleSearch" />

        <TicketFilter :labels="labels" @submit="applyFilter" />

        <TicketTable v-if="tickets.length" :rows="store.paginatedTickets"/>

        <Pagination :page="store.page" :total-pages="store.totalPages" @update:page="store.page = $event" />

        <AnalysisDashboard  v-if="tickets.length" />

        <!-- <TicketCharts v-if="tickets.length" :categoryChart="chartData" /> -->

        <div v-if="loading" class="text-gray-500">Loading...</div>
        <div v-if="error" class="text-red-500">{{ error }}</div>

    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useTicketsStore } from '~/stores/tickets'
import { storeToRefs } from 'pinia'
import TicketSearch from "@/components/tickets/TicketSearch.vue";
import TicketTable from "@/components/tickets/TicketTable.vue";
import TicketCharts from "@/components/tickets/TicketCharts.vue";
import TicketFilter from "@/components/tickets/TicketFilter.vue";
import Pagination from "@/components/common/Pagination.vue";
import AnalysisDashboard  from "@/components/chart/analytics/AnalysisDashboard.vue"

// 狀態
const store = useTicketsStore()
const { tickets, loading, error } = storeToRefs(store)

// 計算圖表資料
const chartData = computed(() => {
    if (!tickets.value.length) return { labels: [], values: [] };

    return {
        labels: tickets.value.map(t => t.predictedLabel),
        values: tickets.value.map(t => t.confidence * 100),
    };
});

function handleSearch(payload: { text?: string; texts?: string[] }) {
    if (payload.text) {
        store.predictOne(payload.text)
    }

    if (payload.texts) {
        store.predictBatch(payload.texts)
    }
}

// filter
const labels = ["Billing", "Technical", "Account", "General"]

function applyFilter(payload: {
    label: string
    minConfidence: number
}) {
    store.filterLabel = payload.label
    store.minConfidence = payload.minConfidence
    store.page = 1
}
</script>
