import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import originalData from './demo/original-data.json'
const app = createApp(App)
app.config.globalProperties.$demoMode = import.meta.env.VITE_DEMO_MODE === 'true'
if (import.meta.env.VITE_DEMO_MODE === 'true') {
  // Do not reuse local-backend credentials in the demo build.
  localStorage.setItem('account', JSON.stringify({ ...originalData.sysUser[0], role: 'ROLE_USER', token: 'demo-only-not-a-real-token' }))
}
app.use(router)


//引入element-plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
//引入element-plus中文库
import zhCn from 'element-plus/es/locale/lang/zh-cn'
app.use(ElementPlus, {locale: zhCn,size: 'small'})

//引入全局样式
import './style/index.scss'

app.mount('#app')
