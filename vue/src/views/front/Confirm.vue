<script setup>
import {useRoute,useRouter} from "vue-router";
import {ref} from "vue";
import request from "@/utils/request.js";
import {Location, LocationFilled, Money, Wallet} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";
import {convertPrice, getCurrencySymbol} from '@/utils/currency'
import {onMounted, onUnmounted} from 'vue'
import { serverHost } from "../../../config/config.default"

const route = useRoute()
const router = useRouter()
const id = ref(route.query.id)

// 支付相关
const payVisible = ref(false)
const payType = ref('支付宝') // 默认支付宝
const currentOrderId = ref(null)

// 币种相关
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const exchangeRates = ref(JSON.parse(localStorage.getItem('exchange_rates') || '{}'))

const updateCurrency = (e) => {
  currentCurrency.value = e.detail
  exchangeRates.value = JSON.parse(localStorage.getItem('exchange_rates') || '{}')
}

onMounted(() => {
  window.addEventListener('currency-changed', updateCurrency)
})

onUnmounted(() => {
  window.removeEventListener('currency-changed', updateCurrency)
})

const goods = ref([])
const loadGoods = () => {
  request.get("/goods/" + id.value).then(res => {
    goods.value = res.data;
  })
}
loadGoods()

const selectedAddressId = ref('')
const address = ref({})
const loadAddress = () => {
  request.get("/address").then(res => {
    address.value = res.data;
    if (address.value.length > 0) {
      selectedAddressId.value = address.value[0].id
    }
  })
}
loadAddress()

// 切换地址
const changeAddress = (id) => {
  selectedAddressId.value = id
}

const storedAccount = localStorage.getItem('account')
const account = ref(storedAccount ? JSON.parse(storedAccount) : {})

// 弹出支付确认框
const confirmOrder = () => {
  if (account.value.id == null) {
    ElMessage.warning("请登录")
    return;
  }
  if (selectedAddressId.value === '') {
    ElMessage.error('请选择您的收货地址')
    return;
  }
  if (goods.value.status !== '已上架') {
    ElMessage.error('商品未上架或已卖出，请联系卖家确认')
    return;
  }
  
  // 点击确认购买即创建订单
  request.post('/orders', {
    itemId: id.value,
    addressId: selectedAddressId.value
  }).then(res => {
    if (res.code === '200') {
      currentOrderId.value = res.data.id
      payVisible.value = true
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 真实执行下单逻辑（模拟支付成功后调用）
const doPay = () => {
  if (!currentOrderId.value) return
  
  // 调用后端的支付接口，将订单状态改为“待发货”
  request.get('/orders/pay/' + currentOrderId.value).then(payRes => {
    if (payRes.code === '200') {
      ElMessage.success('支付成功！')
      payVisible.value = false
      router.push('/front/orders')
    } else {
      ElMessage.error(payRes.msg)
    }
  })
}

// 取消支付的处理
const cancelPay = () => {
  payVisible.value = false
  ElMessage.info('订单已保存至“我的订单”，请在5分钟内完成支付')
  router.push('/front/orders')
}


</script>

<template>

  <div style="height: 100vh;width: 100%;background:linear-gradient(135deg, #fffbdb 0%, rgba(255,196,0,0.53) 100%)">
    <div style="display: flex;gap: 1.33vw;width: 85%;margin: 0 auto;padding: 2.55vh 1.33vw">

      <!--左侧区域-->
      <div style="flex: 1">

        <!--左上区域-->
        <el-card style="border-radius: 1.33vw">

          <div style="display: flex;justify-content: space-between;align-items: center">
            <div>
              <h2>收货地址</h2>
            </div>
            <div>
              <span style="font-size: 0.93vw;color: grey" @click="router.push('/front/address')">管理地址</span>
            </div>
          </div>

          <!-- 地址列表 -->
          <div style="margin-top: 1.28vh;display: grid;grid-template-columns: repeat(3, 1fr);gap: 1.00vw">
            <div class="address-card" :class="{'selected': item.id === selectedAddressId}"
                 v-for="item in address" :key="item.id"
                 @click="changeAddress(item.id)">

              <div style="font-size: 1.46vw;">
                <el-icon >
                  <LocationFilled v-if="selectedAddressId === item.id" />
                  <Location v-else />
                </el-icon>
              </div>

              <div style="flex: 1">
                <div style="font-size: 1.06vw;font-weight: bolder">{{item.address}}</div>
                <div style="font-size: 0.93vw;color: grey">{{item.info}}</div>
                <div style="font-size: 0.93vw;color: grey">{{item.name}} {{item.phone}}</div>
              </div>

            </div>
          </div>
        </el-card>

        <!--左下区域-->
        <el-card style="border-radius: 1.33vw;margin-top: 2.55vh">
          <h2>订单信息</h2>

          <div style="display: flex;gap: 0.66vw">
            <div style="width: 6.65vw;height: 12.76vh">
              <img :src="goods.img" alt="" style="width: 100%;height: 100%;border-radius: 0.66vw">
            </div>

            <div style="flex: 1;display: flex;flex-direction: column;justify-content: space-between">
              <div style="display: flex;gap: 0.66vw;align-content: center">
                <span style="font-weight: bolder;font-size: 1.13vw">{{goods.name}}</span>
                <el-tag>{{goods.shipment}}</el-tag>
              </div>

              <div>
                <span style="font-size: 0.93vw;color: #3867a5">{{goods.place}}</span>
              </div>
            </div>  
          </div>

        </el-card>
      </div>

      <!--右侧价格区域-->
      <div style="width: 19.95vw">
        <el-card style="border-radius: 1.33vw">

          <h3>价格明细</h3>

          <div style="display: flex;justify-content: space-between;">
            <div>
              <span style="font-size: 0.93vw;color: grey">商品价格</span>
            </div>
            <div>
              <span style="font-size: 1.06vw;font-weight: bolder">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(goods.price, currentCurrency, exchangeRates) }}</span>
            </div>
          </div>

          <el-divider></el-divider>

          <div style="display: flex;justify-content: space-between;text-align: center">
            <div>
              <span style="font-size: 0.93vw">合计</span>
            </div>
            <div>
              <span style="font-size: 1.33vw;font-weight: bolder;color: orangered">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(goods.price, currentCurrency, exchangeRates) }}</span>
            </div>
          </div>

          <div style="width: 100%;display: flex;justify-content: center;margin-top: 2.55vh">
            <el-button class="confirm-btn" type="primary" @click="confirmOrder">
              确认购买
            </el-button>
          </div>


        </el-card>
      </div>
    </div>

    <!-- 模拟支付弹窗 -->
    <el-dialog v-model="payVisible" title="收银台" width="26.60vw" center destroy-on-close>
      <div style="text-align: center">
        <div style="font-size: 1.20vw; margin-bottom: 2.55vh">订单金额：
          <span style="color: orangered; font-size: 1.60vw; font-weight: bold">
            {{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(goods.price, currentCurrency, exchangeRates) }}
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
        <span class="dialog-footer">
          <el-button @click="cancelPay">取消支付</el-button>
          <el-button type="primary" @click="doPay" class="pay-confirm-btn">我已支付</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped lang="scss">

.address-card {
  width: 100%;
  min-height: 12.76vh;
  border-radius: 0.66vw;
  background: white;
  border: 0.13vw solid rgba(224, 224, 224, 0.82);
  padding: 2.04vh 0.80vw;
  display: flex;
  gap: 0.80vw;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.address-card:hover {
  border-color: #ff9966;
}

.address-card.selected {
  border-color: #ff6633;
  background: #fff9f5;
}

.confirm-btn {
  width: 90%;
  height: 5.10vh;
  background: linear-gradient(135deg, #ff6633 0%, #ff3300 100%);
  border: none;
  border-radius: 1.99vw;
  padding: 1.79vh 0.93vw;
  color: white;
  font-size: 1.06vw;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  transform: translateY(-0.26vh);
  box-shadow: 0 0.77vh 1.33vw rgba(255, 51, 0, 0.3);
}

.confirm-btn:active {
  transform: translateY(0);
}

.pay-confirm-btn {
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%) !important;
  border: none !important;
  border-radius: 1.33vw !important;
}

.pay-confirm-btn:hover {
  filter: brightness(1.1);
  box-shadow: 0 0.51vh 0.80vw rgba(76, 175, 80, 0.3);
}
</style>
