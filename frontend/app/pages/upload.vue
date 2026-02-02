<template>
    <div class="p-10 text-center">
        <div class="max-w-md mx-auto border-2 border-dashed border-gray-300 rounded-2xl p-8 bg-white shadow-sm">
            <h2 class="text-xl font-bold mb-4">上傳 CSV 分析</h2>

            <div class="mb-6">
                <a href="/demo/async_ticket_test_data.csv" download
                    class="text-sm text-blue-600 hover:text-blue-800 underline decoration-dotted underline-offset-4">
                    下載範例數據
                </a>
            </div>

            <input type="file" @change="onFileChange" accept=".csv" :disabled="isUploading" ref="fileInputRef"
                class="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100" />

            <div v-if="selectedFile" class="mt-4 p-3 bg-gray-50 rounded-lg text-left border border-gray-100">
                <p class="text-xs text-gray-500 uppercase font-bold">待上傳檔案：</p>
                <p class="text-sm text-gray-700 truncate">{{ selectedFile.name }}</p>
                <p class="text-xs text-gray-400">
                    {{ selectedFile.size > 1024 * 1024
                        ? (selectedFile.size / (1024 * 1024)).toFixed(2) + ' MB'
                        : (selectedFile.size / 1024).toFixed(2) + ' KB'
                    }}
                </p>
            </div>

            <button @click="submitUpload" :disabled="!selectedFile || isUploading"
                class="mt-6 w-full py-3 px-4 rounded-xl font-bold text-white transition-all shadow-md"
                :class="[!selectedFile || isUploading ? 'bg-gray-300 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 active:scale-95']">
                <span v-if="isUploading">正在啟動分析...</span>
                <span v-else>開始分析任務</span>
            </button>

            <p v-if="isUploading" class="mt-4 text-blue-600 animate-pulse text-sm">
                系統正在背景排隊任務，請稍候...
            </p>
        </div>
    </div>
</template>

<script setup lang="ts">
const { subscribeToTask } = useTicketSocket()
const isUploading = ref(false)
const selectedFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const onFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement
    if (target.files && target.files[0]) {
        selectedFile.value = target.files[0]
    } else {
        selectedFile.value = null
    }
}

const submitUpload = async () => {
    if (!selectedFile.value) return

    isUploading.value = true
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    try {
        const res = await api.post<{ traceId: string }>('/api/tickets/upload', formData)

        if (res.data.traceId) {
            subscribeToTask(res.data.traceId)

            selectedFile.value = null

            if (fileInputRef.value) {
                fileInputRef.value.value = ''
            }

            alert('分析任務已成功啟動！')
        }
    } catch (error) {
        console.error('上傳失敗:', error)
        alert('檔案上傳失敗，請檢查網路或後端配置。')
    } finally {
        isUploading.value = false
    }
}
</script>