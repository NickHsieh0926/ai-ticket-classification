<template>
    <div class="flex gap-2 justify-center">
        <button :disabled="page <= 1" @click="$emit('update:page', page - 1)">
            上一頁
        </button>

        <span>{{ page }} / {{ totalPages }}</span>

        <button :disabled="page >= totalPages" @click="$emit('update:page', page + 1)">
            下一頁
        </button>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    page: number
    totalPages: number
}>()

defineEmits<{
    (e: "update:page", page: number): void
}>()
</script>

<style scoped></style>
