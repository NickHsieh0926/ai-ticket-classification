<template>
  <canvas ref="canvas" class="w-full h-64"></canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { Chart, registerables } from "chart.js";

Chart.register(...registerables);

const canvas = ref<HTMLCanvasElement | null>(null);

const props = defineProps<{
  labels: string[];
  values: number[];
}>();

let chartInstance: Chart | null = null;

onMounted(() => renderChart());

watch([() => props.labels, () => props.values], () => {
  renderChart();
});

function renderChart() {
  if (!canvas.value) return;
  if (chartInstance) chartInstance.destroy();

  chartInstance = new Chart(canvas.value, {
    type: "bar",
    data: {
      labels: props.labels,
      datasets: [
        {
          label: "預測信心度",
          data: props.values,
          backgroundColor: "#3b82f6",
        },
      ],
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true } },
    },
  });
}
</script>
