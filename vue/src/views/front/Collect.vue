<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {StarFilled, ChatDotSquare, Search} from '@element-plus/icons-vue';
import {convertPrice, getCurrencySymbol} from '@/utils/currency'

const router = useRouter()
const goods = ref([])

// 币种相关
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const exchangeRates = ref(JSON.parse(localStorage.getItem('exchange_rates') || '{}'))

const updateCurrency = (e) => {
  currentCurrency.value = e.detail
  exchangeRates.value = JSON.parse(localStorage.getItem('exchange_rates') || '{}')
}

// 响应式数据
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const load = () => {
  request.get("/goods/collect/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
    }
  }).then(res => {
    if (res.code === '200') {
      goods.value = res.data.records || []
      total.value = res.data?.total || 0
    }
  })
}


const reset = () => {
  keyword.value = ""
  load()
}

const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize
  load()
}

const handleCurrentChange = (newPageNum) => {
  pageNum.value = newPageNum
  load()
}

const cancel = (id) => {
  request.delete('/collect/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('取消收藏成功')
      load()
    }
  })
}


onMounted(() => {
  load()
  window.addEventListener('currency-changed', updateCurrency)
})

onUnmounted(() => {
  window.removeEventListener('currency-changed', updateCurrency)
})
</script>

<template>
  <div class="collect-page">
    <div class="header">
      <h1>我的收藏</h1>
    </div>
    <div style="margin: 1.28vh 0">
      <el-input style="width: 26.60vw;height: 5.10vh;border-radius: 1.33vw;border: 0.07vw solid #ededed" placeholder="输入商品名称进行搜索" v-model="keyword" :prefix-icon="Search" clearable></el-input>
      <button class="search-btn" plain @click="load">搜索</button>
      <el-button class="re-search-btn" plain @click="reset">重置</el-button>
    </div>
    <el-divider></el-divider>
    <div v-if="goods.length === 0" class="empty">
      <p>暂无收藏商品</p>
    </div>

    <div v-else class="goods-grid">
      <div
          v-for="item in goods"
          :key="item.id"
          class="goods-card"
      >
        <div class="image-container">
          <img :src="item.img || '/placeholder.svg?height=200&width=200'" :alt="item.name"/>

          <!-- 已售出标签 -->
          <div v-if="item.status === '已售出'" class="sold-overlay">
            <div class="sold-badge">
              <img src="../../assets/已售出.png" alt="已售出" />
            </div>
          </div>

          <div class="hover-actions">
            <button class="action-btn uncollect" @click="cancel(item.id)">
              <el-icon><StarFilled /></el-icon>
              取消收藏
            </button>
            <!-- 如果商品未下架，显示"去聊聊"按钮 -->
            <button
                v-if="item.status !== '已售出'"
                class="action-btn chat"
                @click.stop="router.push('/front/chat?userId='+item.userId)"
            >
              <el-icon><ChatDotSquare /></el-icon>
              我想要
            </button>
          </div>
        </div>

        <div class="goods-info"   @click="router.push('/front/goodsDetail?id=' + item.id)">
          <div class="goods-name" >{{ item.name || '商品名称' }}</div>
          <div class="price-section">
            <span class="current-price">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(item.price, currentCurrency, exchangeRates) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[4, 8, 12, 16]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>
  </div>
</template>


<style scoped>
.collect-page {
  padding: 2.55vh 1.33vw;
  max-width: 79.79vw;
  margin: 0 auto;
  background-color: #fffbdb;
}

.header {
  margin-left: 0.66vw;
  margin-bottom: 3.06vh;
}

.header h2 {
  font-size: 1.60vw;
  font-weight: 600;
  color: #333;
}

.empty {
  text-align: center;
  padding: 7.65vh 1.33vw;
  color: #999;
  font-size: 1.06vw;
}


.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(15.96vw, 1fr));
  gap: 1.06vw;
}

.goods-card {
  background: white;
  border-radius: 1.33vw;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 0.26vh 0.53vw rgba(0, 0, 0, 0.08);
}

.goods-card:hover {
  transform: translateY(-0.51vh);
  box-shadow: 0 0.51vh 1.06vw rgba(0, 0, 0, 0.12);
}

.image-container {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f5f5f5;
}

.image-container img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sold-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(150, 147, 147, 0.6);
  z-index: 1;
  pointer-events: none;
}

.sold-badge {
  position: absolute;
  top: 30%;
  left: 28%;
  width: 8.64vw;
  height: 16.58vh;
  z-index: 1;
  pointer-events: none;
}

.sold-badge img {
  opacity: 0.5;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.hover-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 0;
  opacity: 0;
  transform: translateY(1.28vh);
  transition: all 0.3s ease;
  z-index: 2;
}

.goods-card:hover .hover-actions {
  opacity: 1;
  transform: translateY(0);
}
.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.40vw;
  padding: 1.53vh 0.80vw;
  border: none;
  font-size: 0.93vw;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(0, 0, 0, 0.35);
  color: white;
  backdrop-filter: blur(0.53vw);
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.55);
}

.action-btn.uncollect svg {
  fill: currentColor;
}

.action-btn.chat {
  border-left: 0.07vw solid rgba(255, 255, 255, 0.2);
}

.goods-info {
  padding: 1.53vh 0.80vw;
  cursor: pointer;
}

.goods-name {
  font-size: 0.93vw;
  color: #333;
  margin-bottom: 1.02vh;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  min-height: 5.10vh;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 0.53vw;
}

.current-price {
  font-size: 1.20vw;
  font-weight: 600;
  color: #ff4d4f;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 4.08vh 0;
}

:deep(.el-input__wrapper) {
  border-radius: 1.99vw;
}

:deep(.el-input__wrapper:hover){
  border: 0.07vw solid #ff8800;
  --el-border-color: #ff8800;
  --el-input-border-color: #ff8800;
  box-shadow: 0 0 0 0.07vw var(--el-input-border-color, var(--el-border-color)) inset;
}

:deep(.el-input__wrapper.is-focus) {
  border: 0.07vw solid #ff8800;
  --el-border-color: #ff8800;
  --el-input-border-color: #ff8800;
  box-shadow: 0 0 0 0.07vw var(--el-input-border-color, var(--el-border-color)) inset;
}

.search-btn{
  margin-left: 0.66vw;
  height: 5.10vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ff8800;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 0.07vw solid #f4f4f4;
}

.search-btn:hover{
  background-color: #ff7300;
  border-color: #ff7300;
}

.search-btn:after{
  background-color: #ff7300;
  border-color: #ff7300;
}

.re-search-btn{
  margin-left: 0.66vw;
  height: 5.10vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #a6a6a6;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 0.07vw solid #f4f4f4;
}

.re-search-btn:hover{
  background-color: #959595;
  border-color: #959595;
}

.re-search-btn:hover{
  background-color: #858585;
  border-color: #858585;
}

</style>
