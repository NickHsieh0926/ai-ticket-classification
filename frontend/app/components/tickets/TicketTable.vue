<!-- <template>
    <table class="min-w-full border">
        <thead>
            <tr class="bg-gray-100">
                <th class="border p-2">輸入文字</th>
                <th class="border p-2">預測標籤</th>
                <th class="border p-2">信心度</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="ticket in tickets" :key="ticket.input + ticket.predictedLabel">
                <td class="border p-2">{{ ticket.input }}</td>
                <td class="border p-2">{{ ticket.predictedLabel }}</td>
                <td class="border p-2">{{ (ticket.confidence * 100).toFixed(1) }}%</td>
            </tr>
        </tbody>
    </table>
</template>

<script setup lang="ts">
import type { PredictionResult } from "@/types/prediction.ts";

const props = defineProps<{
    tickets: PredictionResult[];
}>();
</script> -->

<template>
    <div>
        <table class="w-full border">
            <thead>
                <tr>
                    <th class="border p-2">Input</th>
                    <th class="border p-2">Label</th>
                    <th class="border p-2">Confidence</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(row, i) in rows" :key="i">
                    <td class="border p-2">{{ row.input }}</td>
                    <td class="border p-2">{{ row.predictedLabel }}</td>
                    <td class="border p-2">{{ (row.confidence * 100).toFixed(1) }}%</td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script setup lang="ts">
import type { PredictionResult } from '@/types/prediction'

const props = defineProps<{
    rows: PredictionResult[]
}>()

const emits = defineEmits<{
    (e: 'page-change', page: number): void
}>()
</script>
