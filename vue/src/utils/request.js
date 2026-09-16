import axios from 'axios'
import { createDemoApi } from '../demo/api.js'
import router from '@/router'
import { serverHost } from '../../config/config.default'
// 使用Element Plus组件进行错误信息提示
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: serverHost,
    timeout: 5000
})

// Demo builds use only synthetic, in-memory data; normal builds keep the original backend.
if (import.meta.env.VITE_DEMO_MODE === 'true') {
    const demoApi = createDemoApi()
    request.defaults.adapter = async config => ({
        data: await demoApi(config.method, config.url,
            typeof config.data === 'string' ? JSON.parse(config.data) : config.data, config.params),
        status: 200, statusText: 'OK', headers: {}, config
    })
}

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(
    (config) => {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
        let account = localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : null
        if (account) {
            config.headers['token'] = account.token // 设置请求头
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    (response) => {
        let res = response.data
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 当权限验证不通过的时候给出提示
        if (res.code === '401') {
            ElMessage.error(res.msg)
            // 跳转到登录页面
            router.push('/login')
        }
        return res
    },
    (error) => {
        console.log('error' + error) // for debug
        return Promise.reject(error)
    }
)

export default request
