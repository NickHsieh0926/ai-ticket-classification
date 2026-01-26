<template>
    <div class="flex flex-col gap-2">
        <!-- 單筆輸入 -->
        <input v-model="singleInput" type="text" placeholder="輸入單筆文字" class="border p-2 rounded" />
        <button class="bg-blue-500 text-white px-4 py-1 rounded" @click="onSearchSingle">
            預測單筆
        </button>

        <!-- 批次輸入 -->
        <textarea v-model="batchInput" rows="4" placeholder="每行一筆文字" class="border p-2 rounded"></textarea>
        <button class="bg-green-500 text-white px-4 py-1 rounded" @click="onSearchBatch">
            批次預測
        </button>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 定義 emit
const emits = defineEmits<{
    (e: 'search', payload: { text?: string; texts?: string[] }): void
}>()

// 單筆 / 批次 input
const singleInput = ref('')
const batchInput = ref('')

// 單筆預測
function onSearchSingle() {
    if (!singleInput.value) return
    emits('search', { text: singleInput.value })
    singleInput.value = '' 
}

// 批次預測
function onSearchBatch() {
    const texts = batchInput.value
        .split('\n')
        .map(t => t.trim())
        .filter(Boolean)

    if (!texts.length) return
    emits('search', { texts })
    batchInput.value = '' 
}
</script>

<style scoped>
textarea {
    resize: vertical;
}
</style>
