<template>
    <div class="flex gap-4 items-end">
        <select v-model="label">
            <option value="">全部</option>
            <option v-for="l in labels" :key="l" :value="l">
                {{ l }}
            </option>
        </select>

        <input type="number" min="0" max="100" v-model.number="confidence" />

        <button @click="submit">套用</button>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue"

const props = defineProps<{
    labels: string[]
}>()

const emit = defineEmits<{
    (e: "submit", payload: {
        label: string
        minConfidence: number
    }): void
}>()

const label = ref("")
const confidence = ref(0)

function submit() {
    emit("submit", {
        label: label.value,
        minConfidence: confidence.value / 100,
    })
}
</script>
