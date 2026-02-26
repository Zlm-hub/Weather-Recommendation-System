import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { use } from 'echarts/core'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册ECharts组件
use([LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')