<script setup>
import { Shop, Document , Search } from '@element-plus/icons-vue';
import {reactive, ref, watch, onMounted, onUnmounted} from 'vue'
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {convertPrice, getCurrencySymbol} from '@/utils/currency'

const activeTab = ref('全部')
const selectedSidebar = ref('我卖出的')

// 倒计时相关
const now = ref(new Date())
let timer = null

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
  window.addEventListener('currency-changed', updateCurrency)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('currency-changed', updateCurrency)
})

const getCountdown = (order) => {
  if (!order.time) return ''
  const start = new Date(order.time.replace(/-/g, '/')).getTime()
  const end = start + 5 * 60 * 1000
  const remaining = end - now.value.getTime()

  if (remaining <= 0) {
    // 倒计时结束，如果状态仍为待支付，则自动触发取消
    if (order.status === '待支付' && !order.isAutoCancelling) {
      order.isAutoCancelling = true // 防止重复调用
      cancel(order.id, true) // 传入静默标识
    }
    return '00:00'
  }

  const minutes = Math.floor(remaining / (1000 * 60))
  const seconds = Math.floor((remaining / 1000) % 60)
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 币种相关
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const exchangeRates = ref(JSON.parse(localStorage.getItem('exchange_rates') || '{}'))

const updateCurrency = (e) => {
  currentCurrency.value = e.detail
  exchangeRates.value = JSON.parse(localStorage.getItem('exchange_rates') || '{}')
}


const sidebarItems = [
  { label: '我卖出的', value: '我卖出的' },
  { label: '我买到的', value: '我买到的' }
]


// 表格数据

const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const orders = ref([])
const form = ref({})
const dialogFormVisible = ref(false)
const shipmentDialogVisible = ref(false)
const shipmentLoading = ref(false)
const payVisible = ref(false)
const payType = ref('支付宝')
const currentOrder = ref({})
const serverHost = 'http://127.0.0.1:9090'
const shipmentForm = ref({
  id: null,
  carrier: '',
  trackingNo: '',
  address: '',
  name: '',
  phone: '',
  info: ''
})

// 搜索条件
const searchForm = reactive({
  keyword: '',
})

// 加载数据
const load = () => {
  request.get("/orders/front/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      status: activeTab.value,
      flag: selectedSidebar.value
    }
  }).then(res => {
    if (res.data) {
      orders.value = res.data.records
      total.value = res.data.total
    }
  })
}
load()

const getStatusClass = (status) => {
  const classes = {
    '交易关闭': 'status-closed',
    '交易完成': 'status-success',
    '待支付': 'status-payment',
    '待发货': 'status-shipment',
    '待收货': 'status-receipt',
  }
  return classes[status] || ''
}

const handleSearch = () => {
  pageNum.value = 1
  load()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

// 页码变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}

watch([activeTab, selectedSidebar], () => {
  pageNum.value = 1
  load()
})

const users = ref([]);
const loadUsers = () => {
  request.get('/user').then(res => {
    users.value = res.data;
  });
};
loadUsers()

const pay = (order) => {
  currentOrder.value = order
  payVisible.value = true
}

const doPay = () => {
  request.get('/orders/pay/' + currentOrder.value.id).then(res => {
    if (res.code === '200') {
      ElMessage.success("支付成功")
      payVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const cancelPay = () => {
  payVisible.value = false
  ElMessage.info("支付已取消")
}

const cancel = (id, silent = false) => {
  request.get('/orders/cancel/' + id).then(res => {
    if (res.code === '200') {
      if (!silent) {
        ElMessage.success("已取消")
      }
    } else {
      if (!silent) {
        ElMessage.error(res.msg)
      }
    }
    load()
  })
}

const del = (id) => {
  ElMessageBox.confirm('确认删除该交易记录吗？删除后将不再展示。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete("/orders/" + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg || "删除失败")
      }
    })
  }).catch(() => {})
}

const shipment = (order) => {
  shipmentForm.value = {
    id: order.id,
    carrier: '',
    trackingNo: '',
    address: order.address,
    name: order.name,
    phone: order.phone,
    info: order.info
  }
  shipmentDialogVisible.value = true
}

const confirmShipment = () => {
  if (!shipmentForm.value.carrier || !shipmentForm.value.trackingNo) {
    ElMessage.warning("请填写完整的物流信息")
    return
  }
  shipmentLoading.value = true
  request.post('/orders/shipment', shipmentForm.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("发货成功")
      shipmentDialogVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg || "发货失败")
    }
  }).finally(() => {
    shipmentLoading.value = false
  })
}

const receipt = (id) => {
  ElMessageBox.confirm('确认已收到商品吗？确认后款项将结算给卖家，该操作不可撤销。', '确认收货', {
    confirmButtonText: '确定收货',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.get('/orders/receipt/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("收货成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleEdit = (order) => {
  form.value = JSON.parse(JSON.stringify(order))
  dialogFormVisible.value = true
}

const save = () => {
  request.post("/orders", form.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("保存成功")
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error("保存失败")
    }
  })
}
</script>

<template>

  <div style="width: 100%;min-height: 100vh;background:linear-gradient(135deg, #fffbdb 0%, rgba(255,196,0,0.53) 100%)">
    <div style="width: 85%;margin: 0 auto;gap: 1.99vw;display: flex;padding: 1.28vh 0.66vw">

      <!--左我的交易-->
      <div style="width: 13.30vw;margin-top: 1.28vh">
        <el-card style="border-radius: 1.33vw">

          <div style="display: flex;align-items: center;gap: 0.66vw;margin-bottom: 0.64vh">
            <el-icon style="font-size: 1.33vw"><Shop/></el-icon>
            <span style="font-weight: 600;font-size: 1.20vw;">我的交易</span>
          </div>

          <div style="display: flex;flex-direction: column;">
            <button
                v-for="item in sidebarItems"
                :key="item.value"
                :class="['category-item', { active: selectedSidebar === item.value }]"
                @click="selectedSidebar = item.value"
            >
              {{ item.label }}
            </button>
          </div>

        </el-card>
      </div>


      <!--右-->
      <div style="flex: 1;display: flex;flex-direction: column;gap: 1.28vh;margin-top: 1.28vh">

        <!--搜索-->
        <el-card style="border-radius: 1.33vw;display: flex;align-items: center;">
          <el-input v-model="searchForm.keyword"
                    placeholder="请输入订单号"
                    class="filter-input" :prefix-icon="Search" clearable
                    @keyup.enter="handleSearch"
                    style="width: 19.95vw;height: 3.83vh;border-radius: 3.83vh"/>
          <button class="search-btn"  @click="load">搜索</button>
        </el-card>

        <!--状态-->
        <el-card style="border-radius: 1.33vw;display: flex;align-items: center;">
          <el-tabs v-model="activeTab"  @tab-change ="load">
            <el-tab-pane label="全部" name="全部"></el-tab-pane>
            <el-tab-pane label="待支付" name="待支付"></el-tab-pane>
            <el-tab-pane label="待发货" name="待发货"></el-tab-pane>
            <el-tab-pane label="待收货" name="待收货"></el-tab-pane>
          </el-tabs>
        </el-card>

        <div style="display: flex;flex-direction: column;gap: 1.28vh">
          <el-card style="border-radius: 1.33vw;" v-for="order in orders" :key="order.id">

            <!-- 订单信息行 -->
            <div style="display: flex;justify-content: space-between;align-items: center;">
              <div>
                <span style="font-size: 0.93vw">订单号：{{order.no}}</span>
              </div>

              <div style="display: flex;align-items: center;">
                <h3 style="font-size: 1.00vw">{{ selectedSidebar === '我卖出的' ? '买家：' : '卖家：' }}</h3>
                <el-avatar
                    :src="selectedSidebar === '我卖出的'
                      ? users.find(item => item.id === order.toId)?.avatarUrl
                      : users.find(item => item.id === order.fromId)?.avatarUrl"
                    :size="32"
                    alt="头像"
                ></el-avatar>

                <span style="font-size: 0.93vw;margin-left: 0.33vw;">
                  {{ selectedSidebar === '我卖出的'
                    ? users.find(item => item.id === order.toId)?.nickname
                    : users.find(item => item.id === order.fromId)?.nickname }}
                </span>
              </div>

            </div>

            <el-divider style="margin-top: 0.38vh"></el-divider>

            <!--商品信息-->
            <div style="display: flex;">
              <div style="width: 6.65vw;height: 12.76vh;">
                <img :src="order.itemImg" alt="商品图片" style="width: 100%;height: 100%;object-fit: fill;border-radius: 0.66vw">
              </div>

              <div style="flex: 1;display: flex;flex-direction: column;margin-left: 0.80vw;justify-content: space-between">
                <div style="display: flex;flex-direction: column;gap: 0.64vh">
                  <span style="font-size: 1.06vw;font-weight: bolder">{{order.itemName}}</span>
                  <span style="font-size: 0.86vw">下单时间：{{order.time}}</span>
                </div>
                <!-- 展示物流信息 -->
                <div v-if="order.carrier && order.trackingNo" style="margin-top: 0.64vh; font-size: 0.86vw; color: #666; background: #f9f9f9; padding: 0.64vh 0.66vw; border-radius: 0.40vw;">
                  <div>物流公司：{{ order.carrier }}</div>
                  <div>物流单号：{{ order.trackingNo }}</div>
                  <div v-if="order.deliveryTime">发货时间：{{ order.deliveryTime }}</div>
                </div>
                <div>
                  <span style="font-size: 1.33vw;font-weight: bolder;color: orangered">{{ getCurrencySymbol(currentCurrency) }} {{ convertPrice(order.price, currentCurrency, exchangeRates) }}</span>
                </div>
              </div>
            </div>

            <el-divider style="margin: 1.28vh 0"></el-divider>

            <div class="order-footer">
              <div class="btn-more" style="display: flex; align-items: center; gap: 0.66vw;">
                <span :class="['order-status', getStatusClass(order.status)]" style="font-size: 1.06vw">{{ order.status }}</span>
                <span v-if="order.status === '待支付'" style="color: #f5222d; font-size: 0.93vw; font-family: monospace; background: #fff1f0; padding: 0.26vh 0.53vw; border-radius: 0.27vw; border: 1px solid #ffccc7;">
                  倒计时: {{ getCountdown(order) }}
                </span>
              </div>
              <div class="action-buttons">
                <button class="pay-btn"  v-if="order.status === '待支付' && selectedSidebar === '我买到的'"  @click="pay(order)">立即支付</button>
                <button class="cancel-btn"  v-if="order.status ==='待支付'" @click="cancel(order.id)">取消订单</button>
                <button class="shipment-btn"  v-if="order.status==='待发货' && selectedSidebar==='我卖出的'" @click="shipment(order)">立即发货</button>
                <button class="receipt-btn"  v-if="order.status==='待收货' && selectedSidebar==='我买到的'" @click="receipt(order.id)">确认收货</button>
                <button class="review-btn"  v-if="order.status === '交易完成' && selectedSidebar === '我买到的' && !order.toReview" @click="handleEdit(order)">去评价</button>
                <button class="delete-btn" v-if="order.status === '交易关闭'" @click="del(order.id)">删除记录</button>
              </div>
            </div>

          </el-card>

          <div v-if="orders.length === 0" style="background-color: white;border-radius: 0.53vw;padding: 6.12vh 1.59vw;text-align: center;">
            <el-icon style="font-size: 3.99vw;margin: 0 auto 2.04vh;color: #d9d9d9;">
              <Document/>
            </el-icon>
            <p style="font-size: 0.93vw">暂无订单</p>
          </div>

        </div>

        <!-- 分页 -->
        <el-card style="border-radius: 1.33vw">
          <!-- 分页区域 -->
          <div style="display: flex;justify-content: space-around">
            <el-pagination
                v-model:current-page="pageNum"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </div>

    </div>
    <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑' : '新增'" width="30%" center>
      <el-form :model="form" label-width="6.65vw">
        <el-form-item label="评分">
          <el-rate v-model="form.toRate"/>
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="form.toReview" type="textarea" placeholder="请输入买家评价"/>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer" style="display: flex; justify-content: center; gap: 1.33vw; padding-bottom: 1.28vh;">
          <el-button @click="dialogFormVisible = false" type="danger" style="font-size: 0.86vw; min-width: 6.65vw; height: 3.83vh; border-radius: 1.99vw; color: white;">取消</el-button>
          <el-button type="primary" @click="save" style="font-size: 0.86vw; min-width: 6.65vw; height: 3.83vh; border-radius: 1.99vw">确定</el-button>
        </div>
      </template>

    </el-dialog>

    <!-- 发货信息对话框 -->
    <el-dialog v-model="shipmentDialogVisible" title="填写发货信息" width="35%" center>
      <div style="padding: 0 1.33vw">
        <div style="background: #fdf6ec; padding: 1.28vh 0.66vw; border-radius: 0.53vw; margin-bottom: 2.55vh; border: 1px solid #faecd8;">
          <h4 style="margin: 0 0 1.02vh 0; color: #e6a23c; font-size: 1.00vw">买家收货信息</h4>
          <div style="font-size: 0.93vw; color: #666; line-height: 1.8;">
            <div>收货人：{{ shipmentForm.name }}</div>
            <div>联系电话：{{ shipmentForm.phone }}</div>
            <div>收货地址：{{ shipmentForm.address }} {{ shipmentForm.info }}</div>
          </div>
        </div>
        <el-form :model="shipmentForm" label-width="6.65vw">
          <el-form-item label="物流公司" required>
            <el-select v-model="shipmentForm.carrier" placeholder="请选择物流公司" style="width: 100%">
              <el-option label="FedEx" value="FedEx" />
              <el-option label="UPS" value="UPS" />
              <el-option label="DHL" value="DHL" />
              <el-option label="USPS" value="USPS" />
              <el-option label="顺丰国际" value="顺丰国际" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="物流单号" required>
            <el-input v-model="shipmentForm.trackingNo" placeholder="请输入物流单号" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer" style="display: flex; justify-content: center; gap: 1.33vw; padding-bottom: 1.28vh;">
          <el-button @click="shipmentDialogVisible = false" type="danger" style="font-size: 0.86vw; min-width: 6.65vw; height: 3.83vh; border-radius: 1.99vw; color: white;">取消</el-button>
          <el-button type="primary" :loading="shipmentLoading" @click="confirmShipment" style="font-size: 0.86vw; min-width: 6.65vw; height: 3.83vh; border-radius: 1.99vw">确认发货</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 模拟支付弹窗 -->
    <el-dialog v-model="payVisible" title="收银台" width="26.60vw" center destroy-on-close>
      <div style="text-align: center">
        <div style="font-size: 1.20vw; margin-bottom: 2.55vh">订单金额：
          <span style="color: orangered; font-size: 1.60vw; font-weight: bold">
            {{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(currentOrder.price, currentCurrency, exchangeRates) }}
          </span>
        </div>

        <el-radio-group v-model="payType" style="margin-bottom: 2.55vh">
          <el-radio-button label="支付宝">
            <el-icon><Money /></el-icon> 支付宝
          </el-radio-button>
          <el-radio-button label="微信">
            <el-icon><Wallet /></el-icon> 微信支付
          </el-radio-button>
        </el-radio-group>

        <p v-if="$demoMode">模拟支付：不会收款，请勿扫码或转账。</p>
        <div v-if="!$demoMode" style="margin: 1.28vh 0">
          <img v-if="payType === '支付宝'" :src="serverHost + '/web/download/支付宝支付.JPG'"
               style="width: 16.62vw; height: 31.89vh; border: 0.07vw solid #eee; padding: 1.28vh 0.66vw; border-radius: 0.66vw" />
          <img v-if="payType === '微信'" :src="serverHost + '/web/download/微信支付.JPG'"
               style="width: 16.62vw; height: 31.89vh; border: 0.07vw solid #eee; padding: 1.28vh 0.66vw; border-radius: 0.66vw" />
        </div>

        <div style="color: #999; font-size: 0.86vw; margin-top: 1.28vh">
          {{ $demoMode ? '点击下方按钮体验模拟订单流转' : '请使用' + payType + '扫码完成支付' }}
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer" style="display: flex; justify-content: center; gap: 1.33vw; padding-bottom: 1.28vh;">
          <el-button @click="cancelPay" type="danger" style="font-size: 0.86vw; min-width: 6.65vw; height: 3.83vh; border-radius: 1.99vw; color: white;">取消支付</el-button>
          <el-button type="primary" @click="doPay" style="font-size: 0.86vw; min-width: 6.65vw; height: 3.83vh; border-radius: 1.99vw">我已支付</el-button>
        </div>
      </template>
    </el-dialog>
  </div>

</template>

<style scoped>

.category-item {
  padding: 1.79vh 1.59vw;
  margin-top: 1.28vh;
  text-align: left;
  background: none;
  border: 2px solid rgba(224, 224, 224, 0.82);
  cursor: pointer;
  font-size: 1.00vw;
  border-radius: 0.66vw;
  transition: all 0.3s ease;
}

.category-item:hover {
  background: #ff8800;
  color: white;
  border-color: #ff7300;
  font-weight: bolder;
  transform: scale(1.05);
}

.category-item.active {
  background: #ff8800;
  border-color: #ff7300;
  color: white;
  font-weight: bolder;
  border-radius: 0.66vw;
}

:deep(.el-input__wrapper) {
  border-radius: 1.99vw;
}

:deep(.el-input__wrapper:hover){
  --el-border-color: #ff8800;
  --el-input-border-color: #ff8800;
  box-shadow: 0 0 0 1px var(--el-input-border-color, var(--el-border-color)) inset;
}

:deep(.el-input__wrapper.is-focus) {
  --el-border-color: #ff8800;
  --el-input-border-color: #ff8800;
  box-shadow: 0 0 0 1px var(--el-input-border-color, var(--el-border-color)) inset;
}

.search-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ff8800;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #f4f4f4;
}

.search-btn:hover{
  background-color: #ff7300;
  border-color: #ff7300;
}

.search-btn:after{
  background-color: #ff7300;
  border-color: #ff7300;
}

:deep(.el-button--small){
  border-radius: 1.99vw;
  width: 6.65vw;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border-color: #f4f4f4;
}


:deep(.el-tabs__nav-wrap:after ){
  background-color: #efefef;
}

:deep(.el-tabs__active-bar){
  background-color: #ff8800;
}

:deep(.el-tabs__item.is-active){
  color: #ff8800;
}

:deep(.el-tabs__item:hover){
  color: #ff8800;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0px;


}

.action-buttons {
  display: flex;
  gap: 0.80vw;
}

.order-status {
  margin-left: 0.66vw;
  margin-top: 0px;
  font-size: 1.06vw;
  font-weight: 500;
}

.order-status.status-closed {
  color: #8c8c8c;
}

.order-status.status-success {
  color: #52c41a;
}

.order-status.status-payment {
  color: #f5222d;
}

.order-status.status-shipment {
  color: #1890ff;
}

.order-status.status-receipt {
  color: #faad14;
}

.order-status.status-review {
  color: #ff6b00;
}

.pay-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ff8800;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #f4f4f4;
}
.pay-btn:hover{
  background-color: #ff7300;
  border-color: #ff7300;
}

.cancel-btn{
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ececec;
  color: black;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #f4f4f4;
}
.cancel-btn:hover{
  color: white;
  background-color: #f5222d;
  border-color: #ff7300;
}

.shipment-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #1890ff;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #f4f4f4;
}
.shipment-btn:hover{
  background-color: #409eff;
  border-color: #409eff;
}

.receipt-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ffe610;
  color: black;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #f4f4f4;
}
.receipt-btn:hover{
  background-color: #ffdf10;
  border-color: #ffcf10;
}

.review-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ffe610;
  color: black;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #f4f4f4;
}
.review-btn:hover{
  background-color: #ffdf10;
  border-color: #ffd710;
}

.delete-btn {
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #f5f5f5;
  color: #666;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 1px solid #d9d9d9;
  transition: all 0.3s;
}

.delete-btn:hover {
  background-color: #ff4d4f;
  color: white;
  border-color: #ff4d4f;
}
</style>
