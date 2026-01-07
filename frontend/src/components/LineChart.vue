<template>
  <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

interface Props {
  data: any[]
  xField: string
  yField: string
  seriesField?: string
  smooth?: boolean
  point?: boolean
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  seriesField: '',
  smooth: false,
  point: true,
  height: 300
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value, null, {
    width: chartRef.value.clientWidth,
    height: props.height
  })
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  const option = generateChartOption()
  chartInstance.setOption(option)
}

const generateChartOption = () => {
  if (props.seriesField) {
    // 多系列折线图
    const seriesMap = new Map<string, any[]>()
    const xAxisData = new Set<string>()

    props.data.forEach(item => {
      const seriesName = item[props.seriesField]
      const xValue = item[props.xField]
      const yValue = item[props.yField]

      xAxisData.add(xValue)

      if (!seriesMap.has(seriesName)) {
        seriesMap.set(seriesName, [])
      }
      seriesMap.get(seriesName)!.push({ x: xValue, y: yValue })
    })

    const xData = Array.from(xAxisData).sort((a, b) => {
      const aDate = new Date(a).getTime()
      const bDate = new Date(b).getTime()
      return aDate - bDate
    })
    const series = Array.from(seriesMap.entries()).map(([name, data]) => {
      const dataMap = new Map(data.map(item => [item.x, item.y]))
      const seriesData = xData.map(x => dataMap.get(x) || 0)

      return {
        name,
        type: 'line',
        smooth: props.smooth,
        showSymbol: props.point,
        data: seriesData,
        lineStyle: {
          width: 2
        }
      }
    })

    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          let result = `${params[0].name}<br/>`
          params.forEach((param: any) => {
            result += `${param.seriesName}: ${param.value}<br/>`
          })
          return result
        }
      },
      legend: {
        data: Array.from(seriesMap.keys()),
        top: 10
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '10%',
        top: '20%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xData
      },
      yAxis: {
        type: 'value',
        name: props.yField === 'occupancyRate' ? '入住率 (%)' : '入住数量'
      },
      series
    }
  } else {
    // 单系列折线图
    const sortedData = [...props.data].sort((a, b) => {
      const aDate = new Date(a[props.xField]).getTime()
      const bDate = new Date(b[props.xField]).getTime()
      return aDate - bDate
    })
    const xData = sortedData.map(item => item[props.xField])
    const yData = sortedData.map(item => item[props.yField])

    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const param = params[0]
          return `${param.name}<br/>${param.seriesName}: ${param.value}${props.yField === 'occupancyRate' ? '%' : ''}`
        }
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '10%',
        top: '20%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xData
      },
      yAxis: {
        type: 'value',
        name: props.yField === 'occupancyRate' ? '入住率 (%)' : '入住数量'
      },
      series: [{
        name: props.yField === 'occupancyRate' ? '入住率' : '入住数量',
        type: 'line',
        smooth: props.smooth,
        showSymbol: props.point,
        data: yData,
        lineStyle: {
          width: 2
        },
        areaStyle: props.yField === 'occupancyRate' ? {
          opacity: 0.1
        } : undefined
      }]
    }
  }
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', resizeChart)
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

watch(() => [props.xField, props.yField, props.seriesField, props.smooth, props.point], () => {
  updateChart()
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  min-height: 200px;
}
</style>
