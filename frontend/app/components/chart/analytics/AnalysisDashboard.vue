<template>
    <div class="space-y-6">
        <h2 class="text-xl font-semibold">
            Ticket Analysis
        </h2>

        <!-- 上圖 -->
        <div>
            <CategoryBarChart class="w-full h-80 md:h-[400px]" />
        </div>

        <!-- 下圖 -->
        <div>
            <ConfidenceChart class="w-full h-80 md:h-[400px]" />
        </div>
    </div>
</template>

<script setup lang="ts">
import CategoryBarChart from '@/components/chart/analytics/CategoryBarChart.vue'
import ConfidenceChart from '@/components/chart/analytics/ConfidenceChart.vue'
</script>
