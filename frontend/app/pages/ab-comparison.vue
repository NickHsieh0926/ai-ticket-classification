<template>
    <div class="p-6 max-w-7xl mx-auto min-h-screen bg-gray-50">

        <!-- Header -->
        <div class="flex flex-col md:flex-row justify-between items-center mb-8 p-6
                    bg-white rounded-2xl shadow-sm border border-gray-100 gap-4">
            <div>
                <h1 class="text-2xl font-extrabold text-gray-800">ML vs LLM 比較</h1>
                <p class="text-sm text-gray-500 mt-1" v-if="selectedTraceId">
                    當前批次：
                    <span class="font-mono bg-gray-100 px-2 py-0.5 rounded text-blue-600">
                        {{ selectedTraceId }}
                    </span>
                </p>
            </div>
            <div class="flex items-center gap-3">
                <label class="text-sm font-bold text-gray-600">批次切換:</label>
                <select v-model="selectedTraceId" @change="fetchData" class="border border-gray-200 rounded-xl px-4 py-2 bg-white text-sm
                           focus:ring-2 focus:ring-blue-500 outline-none transition-all
                           cursor-pointer hover:border-blue-300">
                    <option value="" disabled>-- 請選擇歷史批次 --</option>
                    <option v-for="id in historyIds" :key="id" :value="id">
                        {{ id.substring(0, 8) }}... ({{ id.slice(-4) }})
                    </option>
                </select>
            </div>
        </div>

        <!-- AB 上傳區塊 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-6">
            <h2 class="text-base font-bold text-gray-700 mb-4">上傳新批次（同時執行 ML + LLM）</h2>
            <div class="flex items-center gap-4">
                <input type="file" @change="onFileChange" accept=".csv" :disabled="isUploading" ref="fileInputRef"
                    class="text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-full
                           file:border-0 file:text-sm file:font-semibold file:bg-blue-50
                           file:text-blue-700 hover:file:bg-blue-100" />
                <button @click="submitAbUpload" :disabled="!selectedFile || isUploading"
                    class="py-2 px-6 rounded-xl font-bold text-white transition-all shadow-sm whitespace-nowrap" :class="!selectedFile || isUploading
                        ? 'bg-gray-300 cursor-not-allowed'
                        : 'bg-blue-600 hover:bg-blue-700 active:scale-95'">
                    <span v-if="isUploading">分析中...</span>
                    <span v-else>開始 AB 分析</span>
                </button>
            </div>
            <p v-if="isUploading" class="mt-3 text-blue-600 animate-pulse text-sm">
                ML 與 LLM 任務已送出，請稍候結果...
            </p>
        </div>

        <!-- Loading -->
        <div v-if="pending" class="flex flex-col items-center justify-center py-32">
            <div class="w-12 h-12 border-4 border-blue-600 border-t-transparent
                        rounded-full animate-spin"></div>
            <p class="mt-4 text-gray-500 font-medium">載入比較資料中...</p>
        </div>

        <template v-else-if="rows.length">

            <!-- 統計摘要 -->
            <div class="grid grid-cols-3 gap-4 mb-6">
                <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 text-center">
                    <p class="text-sm text-gray-500">總筆數</p>
                    <p class="text-3xl font-extrabold text-gray-800 mt-1">{{ rows.length }}</p>
                </div>
                <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 text-center">
                    <p class="text-sm text-gray-500">一致筆數</p>
                    <p class="text-3xl font-extrabold text-green-600 mt-1">{{ matchCount }}</p>
                </div>
                <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 text-center">
                    <p class="text-sm text-gray-500">一致率</p>
                    <p class="text-3xl font-extrabold text-blue-600 mt-1">{{ matchRate }}%</p>
                </div>
            </div>

            <!-- 比較表格 -->
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                <table class="w-full text-sm">
                    <thead class="bg-gray-50 border-b border-gray-100">
                        <tr>
                            <th class="px-4 py-3 text-left font-semibold text-gray-600">工單內容</th>
                            <th class="px-4 py-3 text-center font-semibold text-gray-600">ML 分類</th>
                            <th class="px-4 py-3 text-center font-semibold text-gray-600">LLM 分類</th>
                            <th class="px-4 py-3 text-center font-semibold text-gray-600">ML 信心度</th>
                            <th class="px-4 py-3 text-center font-semibold text-gray-600">LLM 信心度</th>
                            <th class="px-4 py-3 text-center font-semibold text-gray-600">一致</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="row in rows" :key="row.content"
                            class="border-b border-gray-50 hover:bg-gray-50 transition-colors"
                            :class="row.isMatch ? '' : 'bg-red-50 hover:bg-red-100'">
                            <td class="px-4 py-3 text-gray-700 max-w-xs truncate">{{ row.content }}</td>
                            <td class="px-4 py-3 text-center">
                                <span class="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
                                    {{ row.mlCategory }}
                                </span>
                            </td>
                            <td class="px-4 py-3 text-center">
                                <span class="px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-700">
                                    {{ row.llmCategory }}
                                </span>
                            </td>
                            <td class="px-4 py-3 text-center text-gray-600">
                                {{ (row.mlConfidence * 100).toFixed(1) }}%
                            </td>
                            <td class="px-4 py-3 text-center text-gray-600">
                                {{ (row.llmConfidence * 100).toFixed(1) }}%
                            </td>
                            <td class="px-4 py-3 text-center text-lg">
                                {{ row.isMatch ? '✅' : '❌' }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </template>

        <!-- 空狀態 -->
        <div v-else-if="selectedTraceId" class="flex flex-col items-center justify-center py-32 text-gray-400">
            <p class="text-lg">此批次尚無比較資料</p>
            <p class="text-sm mt-1">請確認 ML 與 LLM 皆有處理此批次</p>
        </div>

    </div>
</template>

<script setup lang="ts">
import type { AbComparisonRow } from '@/types/abComparison'

const route = useRoute()
const router = useRouter()
const { subscribeToAbTask } = useTicketSocket()

const selectedTraceId = ref('')
const historyIds = ref<string[]>([])
const rows = ref<AbComparisonRow[]>([])
const pending = ref(false)

const selectedFile = ref<File | null>(null)
const isUploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

const matchCount = computed(() => rows.value.filter(r => r.isMatch === 1).length)
const matchRate = computed(() =>
    rows.value.length ? ((matchCount.value / rows.value.length) * 100).toFixed(1) : '0.0'
)

onMounted(async () => {
    await loadHistoryIds()
    const tid = route.query.traceId as string
    if (tid) {
        selectedTraceId.value = tid
        await fetchData()
    }
})

const loadHistoryIds = async () => {
    try {
        const { data } = await api.get<string[]>('/api/tickets/ab-trace-ids')
        historyIds.value = data
    } catch (err) {
        console.error('Failed to load history IDs')
    }
}

const fetchData = async () => {
    if (!selectedTraceId.value) return
    pending.value = true
    router.push({ query: { traceId: selectedTraceId.value } })
    try {
        const { data } = await api.get<AbComparisonRow[]>('/api/tickets/ab-comparison', {
            params: { traceId: selectedTraceId.value }
        })
        rows.value = data
    } catch (err) {
        console.error('Failed to load AB comparison data')
    } finally {
        pending.value = false
    }
}

const onFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement
    selectedFile.value = target.files?.[0] ?? null
}

const submitAbUpload = async () => {
    if (!selectedFile.value) return
    isUploading.value = true

    const formData = new FormData()
    formData.append('file', selectedFile.value)

    try {
        const res = await api.post<{ traceId: string }>('/api/tickets/upload/ab', formData)
        const traceId = res.data.traceId

        selectedTraceId.value = traceId
        selectedFile.value = null
        if (fileInputRef.value) fileInputRef.value.value = ''

        subscribeToAbTask(traceId, async () => {
            isUploading.value = false
            await loadHistoryIds()
            await fetchData()
        })
    } catch (err) {
        console.error('AB 上傳失敗:', err)
        alert('上傳失敗，請檢查網路或後端配置。')
        isUploading.value = false
    }
}
</script>