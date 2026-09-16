<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {Camera, Star, Clock, Guide, Ticket, LocationInformation, ArrowLeft, StarFilled, Medal} from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {convertPrice, getCurrencySymbol} from '@/utils/currency'

const route = useRoute();
const router = useRouter();

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

const user = ref({});
const id = ref(route.query.id);
const img = ref('');
const imgList = ref([]);

const goods = ref({});
const loadGoods = () => {
  request.get("/goods/" + id.value).then(res => {
    goods.value = res.data;

    img.value = goods.value.img;

    imgList.value = goods.value.imgList ? goods.value.imgList.split(",") : [];
    imgList.value.unshift(goods.value.img)

    request.get("/user/" + goods.value.userId).then(res => {
      user.value = res.data;
    })
  })
}
loadGoods()

const changeImg = (item) => {
  img.value = item;
}


const buy = (id) => {
  if (account.value.id === null) {
    ElMessage.warning("请登录");
    return;
  }

  router.push(`/front/confirm?id=${id}`);
};

const storedAccount = localStorage.getItem('account')
const account = ref(storedAccount ? JSON.parse(storedAccount) : {})
const collect = (id) => {
  if (account.value.id == null) {
    ElMessage.warning("请登录")
    return;
  }
  let data = {
    itemId: id,
    userId: account.value.id
  }
  request.post("/collect", data).then(res => {
    if (res.code === '200') {
      goods.value.isCollected = true
      ElMessage.success("收藏成功")
    } else {
      goods.value.isCollected = false
      ElMessage.error(res.msg)
    }
  })
}



</script>


<template>

  <div style="min-height: 38.27vh;width: 75%;margin: 0 auto;padding: 3.83vh 1.99vw;background-color: #fffbdb">

    <!--用户信息-->
    <el-card style="border-radius: 0.66vw">
      <div style="display: flex;gap: 0.66vw" @click="router.push('/front/user?id='+goods.userId)">

        <!--左-->
        <div>
          <el-avatar :src="user.avatarUrl" style="width: 3.32vw; height: 6.38vh"></el-avatar>
        </div>

        <!--右-->
        <div style="margin-left: 0.33vw">
          <div style="display: flex; align-items: center;">
            <span style="font-size: 1.20vw;font-weight: bolder">{{user.nickname}}</span>
            <el-tag v-if="user.isAuth" size="small" effect="dark" style="margin-left: 0.66vw; border-radius: 0.80vw;border: none; padding: 0 0.53vw;height: 2.81vh;background-color: rgb(26,213,213);font-size: 0.86vw">
              {{ user.school }} 校友
            </el-tag>
          </div>
          <div style="margin-top: 1.02vh">
            <el-tag size="small" effect="plain" style="font-size: 0.93vw">{{goods.place}}</el-tag>
          </div>
        </div>

      </div>
    </el-card>

    <!--商品信息-->
    <div style="display: flex;gap: 1.33vw;margin-top: 2.55vh;">

      <!--左图片-->
      <el-card style="border-radius: 0.66vw;flex: 3">
        <!--商品图片-->
        <div style="display: flex;gap: 0.66vw;">
          <!--更多图片展示-->
          <div style="width: 7.98vw;padding: 0.64vh 0.33vw">
            <div v-for="item in imgList" class="selectImg" @click="changeImg(item)">
              <img :src="item" style="width: 100%;height: 100%;object-fit: fill;border-radius: 0.66vw">
            </div>

          </div>

          <!--主图展示-->
          <div style="flex: 1;height: 63.78vh">
            <img :src="img" style="width: 100%;height: 100%;object-fit: fill;border-radius: 0.66vw">
          </div>
        </div>

      </el-card>

      <!--右价格-->
      <el-card style="border-radius: 0.66vw;flex: 2">
        <div style="display: flex;justify-content: space-between">
          <div>
            <span style="font-size: 1.60vw;color: orangered">{{ getCurrencySymbol(currentCurrency) }}</span>
            <span style="font-size: 2.66vw;color: orangered;margin-left: 0.20vw">{{ convertPrice(goods.price, currentCurrency, exchangeRates) }}</span>
            <el-text style="margin-left: 0.66vw" tag="del">
              原价{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(goods.rePrice, currentCurrency, exchangeRates) }}
            </el-text>
          </div>
          <div>
            <el-tag type="danger" style="font-size: 0.93vw">{{goods.shipment}}</el-tag>
          </div>
        </div>

        <!--浏览量-->
        <div style="margin-top: 0.38vh">
          <span style="font-size: 0.93vw;color: #919191">{{goods.num}}浏览</span>
        </div>

        <el-divider style="margin-top: 1.91vh"></el-divider>

        <h4>{{goods.name}}</h4>

        <!--渲染富文本-->
        <div v-html="goods.content">

        </div>

        <el-divider></el-divider>

        <div style="display: flex;gap: 3.32vw">
          <span>地区:</span>
          <span>{{goods.place}}</span>
        </div>

        <el-divider></el-divider>

        <div style="display: flex;gap: 3.32vw">
          <span>成色:</span>
          <span>{{goods.quality}}</span>
        </div>

        <el-divider></el-divider>

        <!--三个按钮-->
        <div style="display: flex;gap: 0.66vw">
          <button class="chatButton"
                  @click="router.push('/front/chat?userId='+goods.userId)"
                  v-if="goods.status==='已上架'">聊一聊</button>
          <button class="buyButton"
                  @click="buy(goods.id)"
                  v-if="goods.status==='已上架'">立即购买</button>
          <button class="collectButton"
                  @click="collect(goods.id)"
                  v-if="goods.status==='已上架'">
            <el-icon v-if="goods.isCollected"><StarFilled/></el-icon>
            <el-icon v-else><Star/></el-icon>
            {{goods.isCollected ? '取消收藏' : '收藏'}}
          </button>

        </div>
        <!--如果商品已售出，禁用购买按钮-->
        <div style="display: flex;justify-content: center;">
          <el-button
              style="width: 13.30vw;"
              size="large"
              type="info"
              v-if="goods.status==='已售出'"
              disabled
          >
            已售出
          </el-button>
        </div>


      </el-card>

    </div>



  </div>

</template>

<style scoped>

.selectImg{
  width: 6.65vw;
  height: 12.76vh;
  margin-top: 2.55vh;
  border-radius: 0.80vw;
  border: 0.20vw solid transparent;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.selectImg:hover,
.selectImg:active{
  border-color: #ff6f00;
}

.chatButton{
  width: 9.97vw;
  height: 6.38vh;
  text-align: center;
  border-radius: 0.66vw;
  border: none;
  background-color: grey;
  color: white;
  font-size: 1.00vw;
  font-weight: bolder
}

.chatButton:hover{
  background-color: #6c6c6c;
}

.buyButton{
  width: 13.30vw;
  height: 6.38vh;
  text-align: center;
  border-radius: 0.66vw;
  border: 0.07vw solid #ddd;
  background-color: #ff6f00;
  color: white;
  font-size: 1.00vw;
  font-weight: bolder
}

.buyButton:hover{
  border-color: #ff4d00;
  background-color: #ff6200;
}


.collectButton{
  min-width: 6.65vw;
  height: 6.38vh;
  text-align: center;
  border-radius: 0.66vw;
  border: 0.07vw solid #ddd;
  background-color: orange;
  color: white;
  font-size: 1.00vw;
  font-weight: bolder
}

.collectButton:hover{
  border-color: #ff8800;
  background-color: #ff9900;
}

</style>
