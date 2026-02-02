<template>
    <div class="p-6 max-w-7xl mx-auto min-h-screen bg-gray-50">
        <div
            class="flex flex-col md:flex-row justify-between items-center mb-8 p-6 bg-white rounded-2xl shadow-sm border border-gray-100 gap-4">
            <div>
                <h1 class="text-2xl font-extrabold text-gray-800">AI 分析看板</h1>
                <p class="text-sm text-gray-500 mt-1" v-if="selectedTraceId">
                    當前檢視：<span class="font-mono bg-gray-100 px-2 py-0.5 rounded text-blue-600">{{ selectedTraceId
                        }}</span>
                </p>
            </div>

            <div class="flex items-center gap-3">
                <label class="text-sm font-bold text-gray-600">批次切換:</label>
                <select v-model="selectedTraceId" @change="onIdSwitch"
                    class="border border-gray-200 rounded-xl px-4 py-2 bg-white text-sm focus:ring-2 focus:ring-blue-500 outline-none transition-all cursor-pointer hover:border-blue-300">
                    <option value="" disabled>-- 請選擇歷史批次 --</option>
                    <option v-for="id in historyIds" :key="id" :value="id">
                        {{ id.substring(0, 8) }}... ({{ id.slice(-4) }})
                    </option>
                </select>
            </div>
        </div>

        <div v-if="pending" class="flex flex-col items-center justify-center py-32">
            <div class="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            <p class="mt-4 text-gray-500 font-medium">正在處理 1.6 萬筆數據統計...</p>
        </div>

        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
                <h3 class="text-lg font-bold text-gray-700 mb-6 flex items-center">
                    <span class="w-1 h-5 bg-blue-500 rounded-full mr-3"></span>
                    工單類別佔比
                </h3>
                <v-chart class="h-[400px]" :option="pieOption" autoresize />
            </div>

            <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
                <h3 class="text-lg font-bold text-gray-700 mb-6 flex items-center">
                    <span class="w-1 h-5 bg-green-500 rounded-full mr-3"></span>
                    AI 信心度區間分佈
                </h3>
                <v-chart class="h-[400px]" :option="barOption" autoresize />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const route = useRoute()
const router = useRouter()
const selectedTraceId = ref('')
const historyIds = ref<string[]>([])
const pending = ref(false)

const pieOption = ref({})
const barOption = ref({})

onMounted(async () => {
    await loadHistoryList()

    const tid = route.query.traceId as string
    if (tid) {
        selectedTraceId.value = tid
        fetchData(tid)
    }
})

const loadHistoryList = async () => {
    try {
        const { data } = await api.get<string[]>('/api/tickets/trace-ids')
        historyIds.value = data
    } catch (err) {
        console.error('Failed to load history IDs')
    }
}

const fetchData = async (tid: string) => {
    pending.value = true
    try {
        const { data } = await api.get('/api/tickets/stats', { params: { traceId: tid } })
        updateCharts(data)
    } catch (err) {
        alert('無法獲取該批次的分析數據')
    } finally {
        pending.value = false
    }
}

const updateCharts = (stats: any) => {
    // 圓餅圖
    pieOption.value = {
        tooltip: { trigger: 'item', formatter: '{b}: <b>{c}</b> 件 ({d}%)' },
        legend: { bottom: '0%', left: 'center', icon: 'circle' },
        series: [{
            name: '工單分類',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: true,
            itemStyle: { borderRadius: 12, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
            data: stats.categoryStats // 應為 [{name: 'X', value: 10}, ...]
        }]
    }

    // 長條圖
    barOption.value = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
            type: 'category',
            data: ['0-0.2', '0.2-0.4', '0.4-0.6', '0.6-0.8', '0.8-1.0'],
            axisTick: { alignWithLabel: true }
        },
        yAxis: { type: 'value', name: '工單量' },
        series: [{
            name: '件數',
            type: 'bar',
            barWidth: '60%',
            itemStyle: {
                color: {
                    type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                    colorStops: [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#60a5fa' }]
                },
                borderRadius: [6, 6, 0, 0]
            },
            data: stats.confidenceStats 
        }]
    }
}

const onIdSwitch = () => {
    if (selectedTraceId.value) {
        router.push({ query: { traceId: selectedTraceId.value } })
        fetchData(selectedTraceId.value)
    }
}
</script>

<style scoped>
.v-chart {
    width: 100%;
}
</style>