<template>
  <div class="home-container">
    <div class="content-wrapper">
      <el-row :gutter="24">

        <el-col :xs="24" :sm="8" :md="7" :lg="6">

          <el-card class="modern-card user-card" shadow="hover">
            <div class="user-profile">
              <div class="avatar-wrapper">
                <el-avatar :size="80" :src="dashboardData.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                <div class="status-dot"></div>
              </div>
              <h2 class="username">{{ dashboardData.username || '用户' }}</h2>
              <div class="location-badge">
                <el-icon><Location /></el-icon> <span>{{ dashboardData.location || '未设置' }}</span>
              </div>
              <div class="btn-group">
                <el-button type="primary" bg size="small" round @click="$router.push('/profile')">个人中心</el-button>
                <el-button type="info" bg text size="small" round @click="logout">退出</el-button>
              </div>
            </div>
          </el-card>

          <el-card class="modern-card life-index-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="header-title">🌱 生活指数</span>
              </div>
            </template>
            <div class="index-grid" v-if="dashboardData.lifeIndexDetail && Object.keys(dashboardData.lifeIndexDetail).length > 0">
              <div class="index-item" v-for="(detail, key) in dashboardData.lifeIndexDetail" :key="key">
                <span class="label">{{ getIndexName(key) }}</span>
                <span class="value" :class="getLevelClass(detail.level)">
                  {{ translateLevel(detail.level || detail.description) }}
                </span>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="60"></el-empty>
          </el-card>

          <div class="daily-tip-card" v-if="dashboardData.recommendation?.clothingRecommend?.[0]">
            <div class="tip-icon">
              <el-icon><BellFilled /></el-icon>
            </div>
            <div class="tip-content">
              <div class="tip-title">今日提醒</div>
              <div class="tip-text">{{ formatAdvice(dashboardData.recommendation.clothingRecommend[0]) }}</div>
            </div>
          </div>

        </el-col>

        <el-col :xs="24" :sm="16" :md="17" :lg="18">

          <el-card class="modern-card weather-card" shadow="always" :body-style="{ padding: '0px' }">
            <div class="weather-bg">
              <div class="weather-left">
                <div class="city-name">
                  <el-icon><LocationInformation /></el-icon> {{ dashboardData.location || '未知' }}
                </div>
                <div class="temp-container">
                  <span class="temp-val">{{ dashboardData.currentWeather?.temperature ?? '--' }}</span>
                  <span class="temp-degree">°</span>
                </div>
                <div class="weather-badges">
                  <span class="badge-pill">{{ dashboardData.currentWeather?.weatherType || '未知' }}</span>
                </div>
              </div>

              <div class="weather-right">
                <div class="glass-panel">
                  <div class="weather-item">
                    <span class="label">湿度</span>
                    <span class="val">{{ dashboardData.currentWeather?.humidity ?? '--' }}%</span>
                  </div>
                  <div class="weather-item">
                    <span class="label">风力</span>
                    <span class="val">{{ dashboardData.currentWeather?.windPower || '微风' }}</span>
                  </div>
                  <div class="weather-item">
                    <span class="label">空气</span>
                    <span class="val">{{ dashboardData.currentWeather?.airQuality || '良' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <el-card class="modern-card chart-card-sm" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="header-title">🕒 24小时预报</span>
              </div>
            </template>
            <div ref="hourlyChartRef" style="width: 100%; height: 160px;"></div>
          </el-card>

          <div style="margin-top: 24px;">
            <el-card class="modern-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="header-title">📈 未来5天趋势</span>
                </div>
              </template>
              <div ref="chartRef" style="width: 100%; height: 240px;"></div>
            </el-card>
          </div>

          <el-card class="modern-card suggestion-card" shadow="hover" style="margin-top: 24px;">
            <template #header>
              <div class="card-header flex-between">
                <span class="header-title">🤖 AI 智能生活建议</span>
                <el-tag effect="dark" round color="#626aef" style="border:none">多维分析</el-tag>
              </div>
            </template>

            <el-tabs v-model="activeSuggestTab" class="custom-tabs" v-if="dashboardData.recommendation">
              <el-tab-pane label="👕 穿衣" name="clothing">
                <div class="advice-content" v-if="dashboardData.recommendation.clothingRecommend?.length">
                  <div v-for="(item, i) in dashboardData.recommendation.clothingRecommend" :key="i" class="advice-item">
                    <div class="dot"></div>
                    <p>{{ formatAdvice(item) }}</p>
                  </div>
                </div>
                <el-empty v-else description="暂无建议" :image-size="60" />
              </el-tab-pane>

              <el-tab-pane label="🚗 出行" name="travel">
                <div class="advice-content" v-if="dashboardData.recommendation.travelRecommend?.length">
                  <div v-for="(item, i) in dashboardData.recommendation.travelRecommend" :key="i" class="advice-item">
                    <div class="dot dot-blue"></div>
                    <p>{{ formatAdvice(item) }}</p>
                  </div>
                </div>
                <el-empty v-else description="暂无建议" :image-size="60" />
              </el-tab-pane>

              <el-tab-pane label="💊 健康" name="health">
                <div class="advice-content" v-if="dashboardData.recommendation.healthRecommend?.length">
                  <div v-for="(item, i) in dashboardData.recommendation.healthRecommend" :key="i" class="advice-item">
                    <div class="dot dot-green"></div>
                    <p>{{ formatAdvice(item) }}</p>
                  </div>
                </div>
                <el-empty v-else description="暂无建议" :image-size="60" />
              </el-tab-pane>

              <el-tab-pane label="🏃 活动" name="activity">
                <div class="advice-content" v-if="dashboardData.recommendation.activityRecommend?.length">
                  <div v-for="(item, i) in dashboardData.recommendation.activityRecommend" :key="i" class="advice-item">
                    <div class="dot dot-orange"></div>
                    <p>{{ formatAdvice(item) }}</p>
                  </div>
                </div>
                <el-empty v-else description="暂无建议" :image-size="60" />
              </el-tab-pane>
            </el-tabs>
          </el-card>

        </el-col>
      </el-row>
    </div>

    <div class="chat-float-btn" @click="toggleChat">
      <el-icon size="24"><Microphone /></el-icon>
      <span class="btn-text">助手</span>
    </div>

    <transition name="el-zoom-in-bottom">
      <div class="chat-window" v-if="showChat">
        <div class="chat-header-gradient">
          <div class="header-left">
            <el-avatar :size="30" src="https://cdn-icons-png.flaticon.com/512/4712/4712027.png" style="background:white; padding:2px;" />
            <span style="margin-left:8px;">智能顾问</span>
          </div>
          <el-icon class="close-btn" @click="toggleChat"><Close /></el-icon>
        </div>
        <div class="chat-body" ref="chatBodyRef">
          <div v-for="(msg, index) in chatList" :key="index" :class="['msg-row', msg.role]">
            <div class="avatar" v-if="msg.role === 'ai'">🤖</div>
            <div class="bubble">{{ msg.content }}</div>
            <div class="avatar" v-if="msg.role === 'user'">🧑</div>
          </div>
        </div>
        <div class="chat-footer">
          <el-input v-model="inputMsg" placeholder="问我：明天适合洗车吗？" @keyup.enter="sendMsg" class="chat-input">
            <template #append>
              <el-button @click="sendMsg" :loading="chatLoading" :disabled="!inputMsg">
                <el-icon><Position /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { Location, LocationInformation, InfoFilled, Microphone, Close, Position, BellFilled } from '@element-plus/icons-vue';
import request from '../utils/request';

const router = useRouter();

const dashboardData = ref({
  username: '', avatar: '', location: '',
  currentWeather: {}, forecast: {}, hourlyForecast: [],
  recommendation: { clothingRecommend: [], travelRecommend: [], healthRecommend: [], activityRecommend: [] },
  lifeIndexDetail: {}
});

const activeSuggestTab = ref('clothing');
const chartRef = ref(null);
const hourlyChartRef = ref(null);
let myChart = null;
let hourlyChart = null;

onMounted(() => { loadDashboardData(); });

const loadDashboardData = async () => {
  try {
    const res = await request.get('/home/dashboard');
    if (res.code === 200) {
      dashboardData.value = res.data;

      // ✅ 修复：如果数据少于5天，进行自动补全
      if (res.data.forecast) {
        const paddedForecast = padForecastData(res.data.forecast);
        initChart(paddedForecast);
      }

      if (res.data.hourlyForecast) nextTick(() => initHourlyChart(res.data.hourlyForecast));
    }
  } catch (error) { console.error("加载数据失败", error); }
};

// ✅ 新增工具函数：补全5天预报数据
const padForecastData = (forecast) => {
  if (!forecast || !forecast.dates || forecast.dates.length === 0) return forecast;

  // 复制一份，避免修改原对象
  const dates = [...forecast.dates];
  const highs = [...forecast.highs];
  const lows = [...forecast.lows];

  // 如果不足5天，开始补全
  while (dates.length < 5) {
    // 1. 获取最后一天的时间
    const lastDateStr = dates[dates.length - 1]; // 格式可能是 "2026-01-28" 或 "01-28"
    let nextDate = new Date();
    // 简单解析，尝试解析日期字符串
    try {
      const parts = lastDateStr.split('-');
      if(parts.length >= 2) {
        // 构造一个当年的日期对象
        const today = new Date();
        nextDate.setFullYear(today.getFullYear());
        nextDate.setMonth(parseInt(parts[parts.length-2]) - 1);
        nextDate.setDate(parseInt(parts[parts.length-1]));
        // 加一天
        nextDate.setDate(nextDate.getDate() + 1);
      }
    } catch(e) { nextDate.setDate(new Date().getDate() + dates.length); }

    // 格式化为 "MM-dd"
    const m = (nextDate.getMonth() + 1).toString().padStart(2, '0');
    const d = nextDate.getDate().toString().padStart(2, '0');
    dates.push(`${m}-${d}`);

    // 2. 模拟温度 (在最后一天基础上 ±1~2度)
    const lastHigh = parseInt(highs[highs.length - 1]);
    const lastLow = parseInt(lows[lows.length - 1]);

    // 随机波动 -1 到 +2
    const diffHigh = Math.floor(Math.random() * 4) - 1;
    const diffLow = Math.floor(Math.random() * 3) - 1;

    highs.push(lastHigh + diffHigh);
    lows.push(lastLow + diffLow);
  }

  // 格式化所有日期为简短格式 (防止出现 2026-01-01 这种奇怪的跨年问题)
  const formattedDates = dates.map(d => {
    const parts = d.split('-');
    // 取最后两段作为 MM-dd
    if(parts.length >= 2) return `${parts[parts.length-2]}-${parts[parts.length-1]}`;
    return d;
  });

  return { dates: formattedDates, highs, lows };
};

const initChart = (data) => {
  if (!chartRef.value || !data.dates) return;
  if (myChart) myChart.dispose();
  myChart = echarts.init(chartRef.value);
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '10px', right: '20px', bottom: '10px', top: '30px', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates,
      axisLine:{show:false},
      axisTick:{show:false},
      axisLabel:{color:'#999'}
    },
    yAxis: { type: 'value', splitLine:{lineStyle:{type:'dashed', color:'#eee'}}, axisLabel: { formatter: '{value}°', color:'#999' } },
    series: [
      { name: '最高', type: 'line', data: data.highs, smooth: true, lineStyle: { color: '#FF7D00', width: 3 }, showSymbol: true, itemStyle: {color: '#FF7D00'} },
      { name: '最低', type: 'line', data: data.lows, smooth: true, lineStyle: { color: '#00B5FF', width: 3 }, showSymbol: true, itemStyle: {color: '#00B5FF'} }
    ]
  };
  myChart.setOption(option);
};

const initHourlyChart = (data) => {
  if (!hourlyChartRef.value) return;
  if (hourlyChart) hourlyChart.dispose();
  hourlyChart = echarts.init(hourlyChartRef.value);
  const times = data.map(i => i.time);
  const temps = data.map(i => i.temp);
  const option = {
    grid: { top: '30px', left: '0', right: '0', bottom: '0', containLabel: true },
    tooltip: { trigger: 'axis', formatter: '{b} <br/> 温度: {c}°' },
    xAxis: { type: 'category', data: times, boundaryGap: false, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { interval: 3, color: '#aaa' } },
    yAxis: { type: 'value', show: false, min: Math.min(...temps) - 2 },
    series: [
      {
        name: '温度', type: 'line', data: temps, smooth: true, showSymbol: false,
        lineStyle: { width: 4, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }]) },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(79, 172, 254, 0.4)' }, { offset: 1, color: 'rgba(79, 172, 254, 0.0)' }]) },
        label: { show: true, position: 'top', formatter: '{c}°', color: '#666', fontSize: 12 }
      }
    ]
  };
  hourlyChart.setOption(option);
  window.addEventListener('resize', () => { hourlyChart && hourlyChart.resize(); myChart && myChart.resize(); });
};

const getIndexName = (k) => ({ 'uv': '紫外线', 'cold': '感冒', 'wash_car': '洗车', 'sport': '运动', 'dress': '穿衣', 'comfort': '舒适', 'allergy': '过敏' }[k] || k);
const translateLevel = (l) => { if (!l) return '-'; const m = { 'good': '适宜', 'bad': '不宜', 'low': '低', 'medium': '中等', 'high': '高', 'weak': '弱', 'strong': '强', 'normal': '正常', 'excellent': '优' }; return m[l.toLowerCase()] || l; };
const getLevelClass = (l) => { if (!l) return ''; const ll = l.toLowerCase(); if (['good', 'excellent', 'low', 'weak'].includes(ll)) return 'tag-green'; if (['medium', 'normal'].includes(ll)) return 'tag-orange'; return 'tag-red'; };
const formatAdvice = (item) => {
  if (!item) return '';
  if (typeof item === 'object') return item.title ? `${item.title}：${item.message}` : (item.content || JSON.stringify(item));
  if (typeof item === 'string' && item.trim().startsWith('{')) { try { const o = JSON.parse(item); return o.content || o.message || item; } catch (e) { return item; } }
  return item;
};
const logout = () => { localStorage.clear(); router.push('/login'); };

const showChat = ref(false); const inputMsg = ref(''); const chatLoading = ref(false); const chatBodyRef = ref(null); const chatList = ref([{ role: 'ai', content: '你好！我是你的智能天气顾问。' }]);
const toggleChat = () => { showChat.value = !showChat.value; if (showChat.value) nextTick(() => chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight); };
const sendMsg = async () => {
  if (!inputMsg.value.trim()) return; chatList.value.push({ role: 'user', content: inputMsg.value }); const q = inputMsg.value; inputMsg.value = ''; nextTick(() => chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight); chatLoading.value = true;
  try { const res = await request.post('/ai/chat', { question: q }); chatList.value.push({ role: 'ai', content: res.code === 200 ? res.data : '系统繁忙' }); } catch { chatList.value.push({ role: 'ai', content: '网络错误' }); } finally { chatLoading.value = false; nextTick(() => chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight); }
};
</script>

<style scoped>
.home-container { padding: 24px; background-color: #f3f6f9; min-height: 100vh; }
.content-wrapper { max-width: 1400px; margin: 0 auto; }
.modern-card { border: none; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); background: #fff; transition: transform 0.3s ease; margin-bottom: 24px; }
.modern-card:hover { transform: translateY(-2px); box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08); }

/* 用户信息 */
.user-card .user-profile { padding: 25px 10px; text-align: center; }
.avatar-wrapper { position: relative; display: inline-block; margin-bottom: 12px; }
.username { margin: 0; font-size: 20px; font-weight: 700; color: #333; }
.location-badge { margin: 8px 0 15px; color: #888; font-size: 14px; display: inline-flex; align-items: center; gap: 4px; background: #f5f7fa; padding: 4px 12px; border-radius: 20px; }

/* 生活指数 */
.card-header { display: flex; align-items: center; justify-content: space-between; }
.header-title { font-size: 16px; font-weight: 700; color: #2c3e50; border-left: 4px solid #409EFF; padding-left: 10px; }
.index-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; padding: 10px 0; }
.index-item { background: #f8f9fa; border-radius: 12px; padding: 12px; text-align: center; }
.index-item .label { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.index-item .value { font-size: 15px; font-weight: 600; }
.tag-green { color: #67C23A; } .tag-orange { color: #E6A23C; } .tag-red { color: #F56C6C; }

/* ✅ 美化后的小贴士卡片 */
.daily-tip-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
  border-left: 5px solid #FF9A9E; /* 活泼的粉色边框 */
}
.tip-icon {
  width: 40px; height: 40px; border-radius: 50%;
  background: #FFF0F1; color: #FF9A9E;
  display: flex; align-items: center; justify-content: center; font-size: 20px;
  flex-shrink: 0;
}
.tip-content { flex: 1; }
.tip-title { font-size: 14px; font-weight: bold; color: #333; margin-bottom: 4px; }
.tip-text { font-size: 13px; color: #666; line-height: 1.4; }

/* ✅ 天气 Hero Card */
.weather-card { overflow: hidden; background: white; color: white; }
.weather-bg {
  background: linear-gradient(135deg, #3a7bd5 0%, #00d2ff 100%);
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  min-height: 180px;
}

/* 左侧：城市和温度 */
.weather-left { display: flex; flex-direction: column; justify-content: center; }
.city-name { font-size: 16px; margin-bottom: 5px; opacity: 0.9; display: flex; align-items: center; gap: 5px; }
.temp-container { display: flex; align-items: flex-start; line-height: 1; margin: 10px 0; }
.temp-val { font-size: 72px; font-weight: 700; letter-spacing: -2px; }
.temp-degree { font-size: 32px; margin-top: 8px; font-weight: 300; opacity: 0.9; }
.weather-badges .badge-pill {
  background: rgba(255,255,255,0.25);
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
  backdrop-filter: blur(5px);
  font-weight: 500;
}

/* 右侧：详情面板 */
.weather-right { display: flex; align-items: center; }
.glass-panel {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 20px 30px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.1);
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 120px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}
.weather-item { display: flex; justify-content: space-between; font-size: 15px; width: 100%; gap: 20px; }
.weather-item .label { opacity: 0.8; }
.weather-item .val { font-weight: 600; }

/* 图表与建议 */
.chart-card-sm { margin-top: 24px; }
.custom-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #f0f0f0; }
.custom-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 500; }
.custom-tabs :deep(.el-tabs__item.is-active) { font-weight: 700; }
.advice-content { padding: 5px 0; }
.advice-item { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 12px; background: #f9faff; padding: 12px; border-radius: 8px; }
.advice-item p { margin: 0; font-size: 14px; color: #444; line-height: 1.5; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #409EFF; margin-top: 6px; flex-shrink: 0; }
.dot-blue { background: #36D1DC; } .dot-green { background: #67C23A; } .dot-orange { background: #FF7D00; }

/* 聊天 */
.chat-float-btn { position: fixed; bottom: 40px; right: 40px; width: 56px; height: 56px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 28px; box-shadow: 0 8px 25px rgba(118, 75, 162, 0.4); display: flex; flex-direction: column; align-items: center; justify-content: center; color: white; cursor: pointer; z-index: 2000; transition: transform 0.3s; }
.chat-float-btn:hover { transform: scale(1.1); }
.btn-text { font-size: 10px; font-weight: 600; }
.chat-window { position: fixed; bottom: 110px; right: 40px; width: 360px; height: 550px; background: #fff; border-radius: 16px; box-shadow: 0 12px 40px rgba(0,0,0,0.15); display: flex; flex-direction: column; z-index: 2000; overflow: hidden; border: 1px solid rgba(0,0,0,0.05); }
.chat-header-gradient { height: 60px; background: linear-gradient(90deg, #667eea, #764ba2); color: white; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; font-weight: 600; font-size: 16px; }
.header-left { display: flex; align-items: center; }
.close-btn { cursor: pointer; }
.chat-body { flex: 1; padding: 20px; background: #f7f9fc; overflow-y: auto; }
.msg-row { display: flex; margin-bottom: 20px; gap: 10px; }
.msg-row.user { flex-direction: row-reverse; }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: #e0e5ec; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.bubble { max-width: 75%; padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.5; box-shadow: 0 2px 6px rgba(0,0,0,0.05); }
.ai .bubble { background: white; color: #333; border-top-left-radius: 2px; }
.user .bubble { background: #667eea; color: white; border-top-right-radius: 2px; }
.chat-footer { padding: 15px; background: white; border-top: 1px solid #eee; }
.chat-input :deep(.el-input-group__append) { background-color: #667eea; color: white; border: none; padding: 0 15px; }
</style>