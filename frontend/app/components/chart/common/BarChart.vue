<template>
  <v-chart class="chart" :option="chartOption" autoresize />
</template>

<script setup lang="ts">
import { computed } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart } from "echarts/charts";
import { GridComponent, TooltipComponent } from "echarts/components";
import VChart from "vue-echarts";

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent]);

const props = defineProps<{
  labels: string[];
  values: number[];
}>();

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  grid: {
    top: '10%',
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: props.labels,
    axisTick: { alignWithLabel: true }
  },
  yAxis: {
    type: 'value',
    name: '工單量'
  },
  series: [
    {
      name: '預測信心度',
      type: 'bar',
      data: props.values,
      barWidth: '60%',
      itemStyle: {
        color: '#3b82f6',
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}));
</script>

<style scoped>
.chart {
  height: 256px;
  width: 100%;
}
</style>