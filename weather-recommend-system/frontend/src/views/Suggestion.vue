<template>
  <div class="suggestion-container">
    <div class="nav-header">
      <el-button class="back-btn" link @click="$router.push('/home')">
        <el-icon><ArrowLeft /></el-icon> 返回看板
      </el-button>
      <h2>生活指数详情</h2>
      <div style="width: 60px;"></div>
    </div>

    <div class="ai-brief-card animate-up">
      <div class="ai-header">
        <span class="ai-icon">🤖</span>
        <span class="ai-title">AI 气象助理 · 每日简报</span>
      </div>
      <div class="typewriter">
        <p>{{ detailData.brief || '正在生成智能分析...' }}</p>
      </div>
    </div>

    <h3 class="section-title animate-up" style="animation-delay: 0.1s">各项生活指标</h3>
    <div class="grid-container">
      <div
          v-for="(item, index) in detailData.indices"
          :key="index"
          class="grid-item animate-up"
          :style="{ animationDelay: (0.1 + index * 0.05) + 's' }"
      >
        <div class="grid-icon" :class="getIconClass(item.name)">
          <el-icon><component :is="getIcon(item.name)" /></el-icon>
        </div>
        <div class="grid-info">
          <div class="grid-name">{{ item.name }}</div>
          <div class="grid-value">{{ item.value }}</div>
          <div class="grid-desc">{{ item.desc }}</div>
        </div>
      </div>
    </div>

    <h3 class="section-title animate-up" style="animation-delay: 0.4s">人体舒适度预估 (24h)</h3>
    <div class="chart-card animate-up" style="animation-delay: 0.5s">
      <div id="comfortChart" style="width: 100%; height: 250px;"></div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../api/request'
import * as echarts from 'echarts'
import { ArrowLeft, Sunny, Van, FirstAidKit, Bicycle,  Umbrella, MagicStick } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const detailData = ref({ indices: [] })
const city = route.query.city || '北京' // 接收参数

const fetchDetail = async () => {
  try {
    const res = await request.get('/weather/today', { params: { city } })
    if (res.code === 200 && res.data.suggestionDetail) {
      detailData.value = res.data.suggestionDetail
      initChart()
    }
  } catch (e) { console.error(e) }
}

const getIcon = (name) => {
  const map = { '紫外线': Sunny, '洗车': Van, '穿衣': MagicStick, '感冒': FirstAidKit, '运动': Bicycle, '过敏': Umbrella }
  return map[name] || Sunny
}

const getIconClass = (name) => {
  const map = { '紫外线': 'orange', '洗车': 'blue', '穿衣': 'purple', '感冒': 'red', '运动': 'green', '过敏': 'pink' }
  return map[name] || 'blue'
}

const initChart = () => {
  nextTick(() => {
    const chartDom = document.getElementById('comfortChart')
    if(!chartDom) return
    const myChart = echarts.init(chartDom)

    // 模拟24小时舒适度
    const hours = Array.from({length: 24}, (_, i) => `${i}:00`)
    const data = hours.map(() => 50 + Math.random() * 30) // 模拟数据

    myChart.setOption({
      grid: { top: 30, right: 20, bottom: 20, left: 30, containLabel: true },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: hours, axisLine: {show: false}, axisTick: {show: false} },
      yAxis: { show: false },
      series: [{
        type: 'line', smooth: true, data: data,
        itemStyle: { color: '#007aff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 122, 255, 0.4)' },
            { offset: 1, color: 'rgba(0, 122, 255, 0)' }
          ])
        }
      }]
    })
    window.addEventListener('resize', () => myChart.resize())
  })
}

onMounted(() => fetchDetail())
</script>

<style scoped>
.suggestion-container {
  min-height: 100vh;
  background-color: #f2f2f7;
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.nav-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.nav-header h2 { margin: 0; font-size: 18px; color: #1c1c1e; }
.back-btn { font-size: 16px; color: #007aff; font-weight: 500; }

.ai-brief-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 30px;
  box-shadow: 0 10px 20px rgba(118, 75, 162, 0.3);
}
.ai-header { display: flex; align-items: center; margin-bottom: 15px; opacity: 0.9; }
.ai-icon { font-size: 24px; margin-right: 10px; }
.ai-title { font-weight: bold; letter-spacing: 1px; font-size: 14px; text-transform: uppercase; }
.typewriter p { font-size: 16px; line-height: 1.6; margin: 0; }

.section-title { font-size: 18px; margin: 0 0 15px 5px; color: #333; font-weight: 700; }

.grid-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}
.grid-item {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  transition: transform 0.2s;
}
.grid-item:hover { transform: scale(1.02); }

.grid-icon {
  width: 50px; height: 50px; border-radius: 12px; display: flex; justify-content: center; align-items: center;
  font-size: 24px; margin-right: 15px; color: #fff;
}
.orange { background: #ff9500; } .blue { background: #007aff; } .purple { background: #af52de; }
.red { background: #ff3b30; } .green { background: #34c759; } .pink { background: #ff2d55; }

.grid-name { font-size: 14px; color: #8e8e93; margin-bottom: 2px; }
.grid-value { font-size: 18px; font-weight: bold; color: #1c1c1e; margin-bottom: 2px; }
.grid-desc { font-size: 12px; color: #aeaeb2; }

.chart-card { background: #fff; border-radius: 20px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.02); }

.animate-up { animation: fadeInUp 0.5s ease-out backwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>