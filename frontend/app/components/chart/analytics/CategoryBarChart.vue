<template>
    <div class="chart-wrapper">
        <h3 class="title">分類分布</h3>

        <BarChart v-if="data.labels.length > 0" :labels="data.labels" :values="data.values" />

        <p v-else class="empty">No data</p>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useTicketsStore } from "@/stores/tickets"
import BarChart from "@/components/chart/common/BarChart.vue"

const store = useTicketsStore()

const data = computed(() => store.categoryDistribution)
</script>

<style scoped>
.chart-wrapper {
    padding: 16px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
}

.title {
    font-weight: 600;
    margin-bottom: 8px;
}

.empty {
    text-align: center;
    color: #6b7280;
}
</style>
