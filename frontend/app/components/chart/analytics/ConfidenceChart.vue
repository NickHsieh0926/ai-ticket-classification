<template>
    <BarChart title="Prediction Confidence Distribution" :labels="data.labels" :values="data.values" />
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useTicketsStore } from "@/stores/tickets"
import BarChart from "@/components/chart/common/BarChart.vue"

const store = useTicketsStore()

const data = computed(() => store.confidenceHistogram)
</script>

<style scoped></style>
