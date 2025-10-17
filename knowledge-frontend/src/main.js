// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import store from './store'
import api from './utils/request'

// 创建应用实例
const app = createApp(App)

// 全局挂载api
app.config.globalProperties.$api = api;

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 捕获 ResizeObserver 错误
window.addEventListener('error', (e) => {
    if (e.message.includes('ResizeObserver loop completed with undelivered notifications')) {
        e.stopImmediatePropagation()
    }
})
// 在 main.js 或相关文件中检查
console.log('BASE_API:', process.env.VUE_APP_BASE_API)
// 关键修复：先使用插件，再挂载应用
app.use(ElementPlus)
app.use(router)  // 这行是解决router-view找不到的关键
app.use(store)

// 只挂载一次应用
app.mount('#app')