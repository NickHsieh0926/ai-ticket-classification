<template>
    <BarChart :labels="categoryChart.labels" :values="categoryChart.values" />
</template>

<script setup lang="ts">
import BarChart from "@/components/chart/common/BarChart.vue";

const props = defineProps<{
    categoryChart: { labels: string[]; values: number[] };
}>();
</script>
