/**
 * 前端项目配置文件
 */
const projectName = 'Globuy' // 项目名称
const ip = '127.0.0.1'
const port= '9090'
const serverHost = import.meta.env.VITE_DEMO_MODE === 'true' ? '/demo-disabled' : 'http://'+ip+':'+port // Demo never connects to the visitor's local backend.

export { projectName, serverHost }
