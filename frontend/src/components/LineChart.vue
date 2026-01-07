<template>
  <div ref="chartRef" class="line-chart-container" :style="{ width: '100%', height: height + 'px' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, nextTick, onUpdated } from 'vue'
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

  // 销毁现有实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 确保容器有明确的大小
  chartRef.value.style.width = '100%'
  chartRef.value.style.height = props.height + 'px'

  chartInstance = echarts.init(chartRef.value, null, {
    renderer: 'canvas',
    useDirtyRect: false,
    height: props.height
  })

  updateChart()

  // 多次调整大小确保正确
  setTimeout(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 50)

  setTimeout(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 150)

  setTimeout(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 300)
}

const updateChart = () => {
  if (!chartInstance) return

  const option = generateChartOption()
  chartInstance.setOption(option)

  // 更新选项后立即调整大小
  setTimeout(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 10)
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
        top: 10,
        type: 'scroll', // 允许滚动
        orient: 'horizontal',
        left: 'center'
      },
      grid: {
        left: '5%',
        right: '5%',
        bottom: '5%',
        top: '8%',
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
        left: '5%',
        right: '5%',
        bottom: '8%',
        top: '15%',
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
    // 确保容器大小正确后再调整
    setTimeout(() => {
      chartInstance.resize()
    }, 100)
  }
}

onMounted(() => {
  nextTick(() => {
    // 多重延迟确保容器大小稳定
    let attempts = 0
    const maxAttempts = 10
    const initWithRetry = () => {
      attempts++
      initChart()

      // 如果图表没有正确初始化，稍后重试
      setTimeout(() => {
        if (chartInstance && chartRef.value) {
          const containerWidth = chartRef.value.offsetWidth
          const containerHeight = chartRef.value.offsetHeight
          if (containerWidth < 100 || containerHeight < 100) {
            if (attempts < maxAttempts) {
              console.log(`图表初始化重试 ${attempts}/${maxAttempts}`)
              initWithRetry()
            }
          } else {
            // 强制调整大小
            chartInstance.resize()
          }
        }
      }, 100 * attempts)
    }

    setTimeout(initWithRetry, 100)
  })
  window.addEventListener('resize', resizeChart)
})

onUpdated(() => {
  nextTick(() => {
    setTimeout(() => {
      if (chartInstance) {
        // 完全重新初始化图表
        initChart()
      }
    }, 200)
  })
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', resizeChart)
})

watch(() => props.data, () => {
  nextTick(() => {
    updateChart()
    // 数据更新后也调整大小
    setTimeout(() => {
      if (chartInstance) {
        chartInstance.resize()
      }
    }, 100)
  })
}, { deep: true })

watch(() => [props.xField, props.yField, props.seriesField, props.smooth, props.point, props.height], () => {
  nextTick(() => {
    if (props.height && chartRef.value) {
      // 高度变化时需要重新设置容器样式并重新初始化
      chartRef.value.style.height = props.height + 'px'
      initChart()
    } else {
      updateChart()
    }
  })
})
</script>

<style scoped>
.line-chart-container {
  width: 100% !important;
  height: 100% !important;
  min-height: 200px;
  display: block !important;
  position: relative !important;
}
</style>
