<script setup>
import {ref, onMounted, onUnmounted} from 'vue'
import request from "@/utils/request.js";
import {useRouter} from "vue-router";
import {Medal} from '@element-plus/icons-vue'
import {convertPrice, getCurrencySymbol} from '@/utils/currency'

const router = useRouter()

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

/**
 * 首页
 */
const notices = ref([])
const loadNotice = () =>{
  request.get('/notice').then(res => {
    notices.value = res.data
  })
}
loadNotice()

const showNotice = ref(true)
const closeNotice = () => {
  showNotice.value = false
}

/**
 * 分类
 */
const types = ref([])
const loadType = () =>{
  request.get('/type').then(res => {
    types.value = res.data
  })
}
loadType()

/**
 * 轮播图
 */
const banners = ref([])
const loadBanner = () =>{
  request.get('/banner').then(res => {
    banners.value = res.data
  })
}
loadBanner()

/**
 * 热门分类
 */
const hotTypes = ref([])
const loadhotType = () =>{
  request.get('/type/front').then(res => {
    hotTypes.value = res.data
  })
}
loadhotType()

/**
 * 热门商品
 */
const goods = ref([])
const loadhotGoods = () =>{
  request.get('/goods/front').then(res => {
    goods.value = res.data
  })
}
loadhotGoods()

const getProvince = (place) =>{
  return place.split('/')[0]
}

const users = ref([])
const loadUser = () =>{
  request.get('/user').then(res => {
    users.value = res.data
  })
}
loadUser()

const shipment = ref([])
const loadShipment = () =>{
  request.get('/shipment').then(res => {
    shipment.value = res.data
  })
}
loadShipment()

const categoryColors = [
  'linear-gradient(135deg, orange 0%, #ffe610 100%)',
  'linear-gradient(45deg, #ffe610 0%, orange 100%)',
  'linear-gradient(45deg, orange 0%, #ffe610 100%)',
  'linear-gradient(135deg, #ffe610 0%, orange 100%)'
]

</script>



<template>

  <!-- 首页上半部分 -->
  <div style="width: 100%; min-height: 38.27vh; background-color: #fffce5; padding: 1.28vh 0.66vw;">
    <div style="width: 90%; margin: 0 auto; height: 100%;">

      <!--公告-->
      <div style="width: 100%; height: 5.10vh; padding-bottom: 1.28vh; cursor: pointer" v-if="showNotice">
        <el-carousel height="5.10vh" direction="vertical" :interval="3000" autoplay="true" indicator-position="none">
          <el-carousel-item v-for="notice in notices" :key="notice.id">
            <div style="margin-top: 1.28vh; display: flex; justify-content: space-between;">
              <span style="color:grey; font-size: 0.93vw">{{ notice.name }}:{{notice.info}}</span>
              <span style="color:grey; font-size: 0.93vw" @click="closeNotice">X</span>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!--大三块-->
      <div style="display: flex; gap: 1.33vw;height: 100%;">

        <!--分类-->
        <div style="height: 43.37vh; flex: 1; padding: 1.28vh 0.66vw;background: linear-gradient(135deg, #ffee44 0%, #fffbdb 100%);border: 1px solid #d1d1d1;border-radius: 1.33vw; box-shadow: 0 0.26vh 1.02vh rgba(0, 0, 0, 0.08);">
          <div style="width: 100%; height: 100%; display: flex; flex-direction: column; gap: 0.38vh;justify-content: space-around;">
            <div v-for="type in types" :key="type.id" style="display: flex" class="type-item">
              <div style="display: flex; gap: 0.66vw; text-align: left" @click="router.push('/front/search?typeId=' + type.id + '&keyword=')">

                <img :src="type.icon" alt="" style="width: 1.33vw; height: 1.33vw; object-fit: cover;margin-left: 0.33vw">
                <span style="font-weight: 500; font-size: 0.93vw">{{type.name}}</span>

              </div>
            </div>
          </div>
        </div>

        <!-- 轮播图 -->
        <div style="height: 43.37vh; flex: 1.7; border-radius: 1.33vw; box-shadow: 0 0.26vh 1.02vh rgba(0, 0, 0, 0.08);">
          <el-carousel height="43.37vh">
            <!-- 走马灯图片 -->
            <el-carousel-item v-for="banner in banners" :key="banner.id">

              <!-- 模糊效果 -->
              <div style="opacity: 0.9;height: 100%; width: 100%; position: relative;">
                <div style="position: absolute; top:0; bottom: 0;right:0;left:0; background: linear-gradient(to right, rgba(0, 0, 0, 0.5) 0%, transparent 70%); display: flex;flex-direction: column; justify-content: space-around;border-radius: 1.33vw">
                  <span style="font-size: 1.20vw; font-weight: bolder; color: white;margin-left: 0.66vw">{{banner.name}}</span>
                </div>
                <img :src="banner.img" alt="" style="height: 100%; width: 100%; border-radius: 1.33vw;object-fit: fill;">
              </div>

            </el-carousel-item>
          </el-carousel>
        </div>

        <!--热门分类-->
        <div style="height: 43.37vh; flex: 5; display: grid; grid-template-columns: repeat(2,1fr); gap: 1.33vw;">

          <div v-for="(type,index) in hotTypes" :key="type.id" class="hotType-item" :style="{background: categoryColors[index]}">
            <!--左侧-->
            <div @click="router.push('/front/search?keyword=&typeId=' + type.id)">

              <div>
                <span style="font-weight: 500;font-size: 0.86vw; color: #3a3a3a;">{{type.name}}</span>
              </div>
              <div>
                <span style="font-size: 0.66vw; color: #3a3a3a;">{{type.info}}</span>
              </div>
              <div style="margin-top: 0.64vh">
                <img :src="type.img" alt="" style="width: 3.32vw; height: 3.32vw; object-fit: cover;">
              </div>

            </div>

            <!--右侧-->
            <div style="height: 15.31vh; width: 100%; background: rgba(255,255,255,0.8);border-radius: 1.33vw;padding: 1.28vh;display: flex;gap: 0.66vw">

              <div v-for="item in type.goodsList" :key="item.id" style="flex: 1" class="hotType-item-goods" @click="router.push('/front/goodsDetail?id=' + item.id)">
                <div style="height: 10.20vh;width: 100%">
                  <img :src="item.img" alt="item.name" style="width:100%; height: 100%; object-fit: cover;;border-radius: 0.66vw">
                </div>
                <div style="text-align: center;margin-top: 0.64vh">
                  <span style="font-size: 1.00vw; color: red;font-weight: bolder">
                    {{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(item.price, currentCurrency, exchangeRates) }}
                  </span>
                </div>
              </div>
            </div>

          </div>


        </div>

      </div>

    </div>
  </div>

  <div style="width: 90%; margin: 0 auto; display: flex; justify-content: space-between;">
    <el-divider></el-divider>
  </div>

  <!-- 首页下半部分 -->
  <div>
    <div style="width: 90%; margin: 0 auto; height: 100%;">
      <!-- 精选好物 -->
      <div style="display: flex; justify-content: space-between;margin-top: 1.28vh;margin-bottom: 2.55vh">
        <div>
          <span style="font-size: 1.99vw; font-weight: bolder;">精选好物</span>
        </div>
        <div style="margin-top: 1.28vh">
          <el-button style="font-size: 0.86vw" link type="primary" @click="router.push('/front/search?keyword=&typeId=0')">查看全部 →</el-button>
        </div>
      </div>


      <!-- 商品列表 -->
      <div style="display: grid; grid-template-columns: repeat(6 ,1fr); gap: 0.66vw;margin-top: 3.83vh;">
        <div style="width: 95%; margin-left: 0.33vw; margin-right: 0.33vw; margin-top: 1.28vh; margin-bottom: 3.83vh; background-color: #fffbdb;overflow: hidden;border-radius: 1.33vw"
             v-for="item in goods" :key="item.id"
             class="hotType-goods"
             @click="router.push('/front/goodsDetail?id=' + item.id)">

          <!-- 商品图片 -->
          <div style="width: 100%; height: 29.34vh">
            <img :src="item.img" alt="" style="width: 100%; height: 100%; object-fit: cover;">
          </div>

          <!-- 商品信息 -->
          <div style="width: 100%;padding: 1.28vh 0.66vw;overflow: hidden">
            <!-- 第一行 -->
            <div style="display: flex;gap: 0.33vw">

              <div style="width: 2.66vw">
                <span style="font-size: 1.20vw; font-weight: bolder; overflow: hidden;  text-decoration: underline;text-decoration-color: #ffe610;text-decoration-thickness: 1.02vh;text-underline-offset: -0.51vh;">{{item.shipment}}</span>
              </div>
              <div style="line-height: 1.2; overflow: hidden;text-overflow: ellipsis;white-space: nowrap;flex: 1">
                <span style="font-size: 1.00vw;line-height: 1.4;">{{item.name}}</span>
              </div>

            </div>

            <!-- 第二行 -->
            <div style="display: flex;justify-content: space-between;padding: 0.64vh 0.33vw">
              <div>
                <span style="color: orangered;font-size: 1.20vw;font-weight: bolder">
                  {{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(item.price, currentCurrency, exchangeRates) }}
                </span>
              </div>
              <div>
                <el-tag type="primary" effect="plain"  style="font-size: 0.66vw">{{getProvince(item.place)}}</el-tag>
              </div>
            </div>

            <el-divider style="margin: 0.38vh 0"></el-divider>

            <!-- 第三行 -->
            <div style="display: flex;gap: 0.53vw;align-content: center;padding: 0.64vh 0.33vw;">

              <div>
                <el-avatar :src="users.find(user=>user.id === item.userId).avatarUrl" :size="25" ></el-avatar>
              </div>
              <div>
                <span style="font-size: 0.93vw;color: grey">{{users.find(user=>user.id === item.userId).nickname}}</span>
                <el-tooltip v-if="item.isAuth" :content="'校友认证：' + item.school" placement="top">
                  <el-icon style="color: #E6A23C; margin-left: 0.27vw; vertical-align: middle;"><Medal /></el-icon>
                </el-tooltip>
              </div>

            </div>

          </div>

        </div>

      </div>

    </div>
  </div>

</template>



<style scoped>
.type-item:hover{
  background-color: #f5f5f5;
}
.hotType-item{
  height: 100%;
  width: 100%;
  border-radius: 1.33vw;
  padding: 1.28vh 1.33vw;
  display: flex;
  gap: 1.33vw;
  transition: all 0.3s ease;
  box-shadow: 0 0.26vh 1.02vh rgba(0, 0, 0, 0.08);
}
/* 悬浮效果 */
.hotType-item:hover{
  transform: scale(1.05);
  box-shadow: 0 0.51vh 2.04vh rgba(0, 0, 0, 0.12);
}

.hotType-item-goods{
  transition: all 0.3s ease;
}
.hotType-item-goods:hover{
  transform: translateY(-0.64vh);

}

.hotType-goods{
  transition: all 0.3s ease;
  box-shadow: 0 0.26vh 1.02vh rgba(0, 0, 0, 0.08);
}
.hotType-goods:hover{
  transform: scale(1.05);
  box-shadow: 0 0.51vh 2.04vh rgba(0, 0, 0, 0.12);
}
</style>
