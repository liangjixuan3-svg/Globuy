<script setup>
import {ref, onMounted, onUnmounted, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import request from '@/utils/request.js'
import {Medal} from '@element-plus/icons-vue'
import {convertPrice, getCurrencySymbol} from '@/utils/currency'
import { worldRegionData } from '@/utils/areaData.js'

const route = useRoute()
const router = useRouter()

// 币种相关
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const exchangeRates = ref(JSON.parse(localStorage.getItem('exchange_rates') || '{}'))

const updateCurrency = (e) => {
  currentCurrency.value = e.detail
  exchangeRates.value = JSON.parse(localStorage.getItem('exchange_rates') || '{}')
}

// 响应式数据
const typeId = ref(route.query.typeId ? Number(route.query.typeId) : 0)
const country = ref('')
const keyword = ref(route.query.keyword || '')
const goods = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortBy = ref('all')
const types = ref([])


const load = () => {
  request.get("/goods/front/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      typeId: typeId.value,
      country: country.value,
      sortBy: sortBy.value
    }
  }).then(res => {
    if (res.code === '200') {
      goods.value = res.data.records || []
      total.value = res.data?.total || 0
    }
  })
}

const updateSort = (sortCriterion) => {
  sortBy.value = sortCriterion
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

const loadType = () => {
  request.get('/type').then(res => {
    types.value = res.data
  })
}

const getProvince = (place) => {
  if (!place) return ''
  return place.split('/')[0]
}


const handleTypeChange = (id) => {
  typeId.value = id
  pageNum.value = 1
  load()
}

const handleCountryChange = (c) => {
  country.value = c
  pageNum.value = 1
  load()
}

const users = ref([]);
const loadUser = () => {
  request.get('/user').then(res => {
    users.value = res.data;
  });
};

// 生命周期
if (import.meta.env.VITE_DEMO_MODE === 'true') {
  watch(() => route.query, query => {
    keyword.value = query.keyword || ''
    typeId.value = Number(query.typeId) || 0
    pageNum.value = 1
    load()
  })
}
onMounted(() => {
  load()
  loadType()
  loadUser()
  window.addEventListener('currency-changed', updateCurrency)
})

onUnmounted(() => {
  window.removeEventListener('currency-changed', updateCurrency)
})

</script>

<template>
  <div class="home-container">
    <!-- 分类筛选 -->
    <div class="category-filter-section">
      <div class="filter-header">
        <span class="filter-title">商品分类</span>
        <span v-if="typeId !== 0" class="current-type-hint">
          当前分类: {{ types.find(t => t.id === typeId)?.name || '未知' }}
        </span>
      </div>
      <div class="category-buttons">
        <button
            :class="['category-btn', { 'active': typeId === 0 }]"
            @click="handleTypeChange(0)">
          <span class="btn-text">全部分类</span>
        </button>
        <button
            v-for="type in types"
            :key="type.id"
            :class="['category-btn', { 'active': typeId === type.id, 'highlighted': typeId === type.id }]"
            @click="handleTypeChange(type.id)">
          <span class="btn-text">{{ type.name }}</span>
        </button>
      </div>
    </div>

    <!-- 国家筛选 -->
    <div class="category-filter-section" style="margin-top: 1.91vh;">
      <div class="filter-header">
        <span class="filter-title">所在国家</span>
        <span v-if="country !== ''" class="current-type-hint">
          当前国家: {{ country }}
        </span>
      </div>
      <div class="category-buttons">
        <button
            :class="['category-btn', { 'active': country === '' }]"
            @click="handleCountryChange('')">
          <span class="btn-text">全部国家</span>
        </button>
        <button
            v-for="item in worldRegionData"
            :key="item.value"
            :class="['category-btn', { 'active': country === item.value, 'highlighted': country === item.value }]"
            @click="handleCountryChange(item.value)">
          <span class="btn-text">{{ item.label }}</span>
        </button>
      </div>
    </div>

    <!-- 优化筛选排序区域 -->
    <div class="filters-section">
      <div class="sort-options">
        <span class="sort-label">排序方式:</span>
        <button :class="['sort-btn', { active: sortBy === 'all' }]" @click="updateSort('all')">
          <span>综合排序</span>
        </button>
        <button :class="['sort-btn', { active: sortBy === 'new' }]" @click="updateSort('new')">
          <span>最新发布</span>
        </button>
        <button :class="['sort-btn', { active: sortBy === 'price' }]" @click="updateSort('price')">
          <span>价格优先</span>
        </button>
      </div>


      <div v-if="keyword" class="keyword-display">
        <span class="keyword-label">搜索:</span>
        <span class="keyword-value">"{{ keyword }}"</span>
      </div>
    </div>

    <el-divider></el-divider>


    <!-- 商品列表 -->
    <div class="goods-grid">
      <div
          v-for="good in goods"
          :key="good.id"
          class="good-card"
          @click="router.push('/front/goodsDetail?id=' + good.id)">

        <!-- 商品图片 -->
        <div class="good-image-wrapper">
          <img :src="good.img" class="good-image" :alt="good.name">
        </div>

        <!--商品信息-->
        <div class="good-content">
          <!-- 第一行 -->
          <div style="display: flex;gap: 0.33vw;">

            <div style="width: 2.66vw">
              <span style="font-size: 1.20vw; font-weight: bolder; overflow: hidden;  text-decoration: underline;text-decoration-color: #ffe610;text-decoration-thickness: 1.02vh;text-underline-offset: -0.51vh;">{{good.shipment}}</span>
            </div>
            <div style="line-height: 1.2; overflow: hidden;text-overflow: ellipsis;white-space: nowrap;flex: 1">
              <span style="font-size: 1.06vw;line-height: 1.4;">{{good.name}}</span>
            </div>

          </div>

          <!-- 第二行 -->
          <div class="good-footer">
            <span class="price">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(good.price, currentCurrency, exchangeRates) }}</span>
          </div>

          <!-- 第三行 -->
          <div class="goods-user">
            <div style="display: flex; align-items: center; gap: 0.27vw;">
              <el-avatar
                  style="width: 2.13vw; height: 4.08vh"
                  :src="users.find(i => i.id === good.userId)?.avatarUrl"
                  class="user-avatar"
              >
              </el-avatar>
              <el-tooltip v-if="good.isAuth" :content="'校友认证：' + good.school" placement="top">
                <el-icon style="color: #E6A23C;"><Medal /></el-icon>
              </el-tooltip>
            </div>
            <el-tag size="small" effect="plain">{{ getProvince(good.place) }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 30, 50, 70]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>

  </div>
</template>

<style lang="scss" scoped>
.home-container {
  width: 85%;
  margin: 0 auto;
  padding: 0 0;
}

.category-filter-section {
  margin-bottom: 1.79vh;
  padding: 2.55vh 1.60vw;
  border-radius: 0.80vw;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.filter-title {
  font-size: 1.06vw;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.03vw;
}

.current-type-hint {
  font-size: 0.86vw;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.2);
  padding: 0.51vh 0.80vw;
  border-radius: 1.33vw;
  backdrop-filter: blur(0.66vw);
}

.category-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.66vw;
}

.category-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.40vw;
  padding: 1.28vh 1.33vw;
  background-color: rgba(255, 255, 255, 0.9);
  border: 0.13vw solid transparent;
  border-radius: 1.60vw;
  font-size: 0.93vw;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0.26vh 0.27vw rgba(0, 0, 0, 0.1);

  &:hover {
    transform: translateY(-0.26vh);
    box-shadow: 0 0.51vh 0.80vw rgba(0, 0, 0, 0.15);
    background-color: #ffffff;
  }

  &.active {
    background-color:#ffe610;
    border-color: #ffffff;
    box-shadow: 0 0.77vh 1.33vw rgba(47, 46, 34, 0.4);
  }

  &.highlighted {
    animation: highlight-pulse 2s ease-in-out infinite;
  }
}



.btn-icon {
  font-size: 1.06vw;
}

.btn-text {
  white-space: nowrap;
}

.active-indicator {
  font-size: 0.80vw;
  font-weight: bold;
}


.filters-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2.04vh 1.33vw;
  background-color: #f8f9fa;
  border-radius: 0.53vw;
  margin-bottom: 2.55vh;
  flex-wrap: wrap;
  gap: 1.06vw;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 0.66vw;
  flex-wrap: wrap;
}

.sort-label {
  font-size: 1.06vw;
  color: #4a5568;
  font-weight: 500;
  margin-right: 0.53vw;
}

.sort-btn {
  padding: 1.02vh 1.66vw;
  background-color: #ffffff;
  border: 0.07vw solid #e2e8f0;
  border-radius: 0.66vw;
  font-size: 0.93vw;
  font-weight: bolder;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: #fff5f5;
    border-color: #ff8000;
    color: #ff8000;
  }

  &.active {
    background-color: #ff8000;
    border-color: #ff8000;
    color: #ffffff;
    font-weight: 600;
  }
}

.keyword-display {
  display: flex;
  align-items: center;
  gap: 0.53vw;
  padding: 1.02vh 1.06vw;
  background-color: #ffffff;
  border-radius: 0.40vw;
  border: 0.07vw solid #e2e8f0;
}

.keyword-label {
  font-size: 0.86vw;
  color: #718096;
  font-weight: 500;
}

.keyword-value {
  font-size: 0.93vw;
  color: #2d3748;
  font-weight: 600;
}

.goods-grid {
  display: grid;/*采用grid布局*/
  grid-template-columns: repeat(5, 1fr);/*一行展示n个，修改第一个参数即可*/
  gap: 1.33vw;/*元素间隔宽度*/
  margin-bottom: 4.08vh;
}

.good-card {
  width: 95%; height: 48.47vh;
  background-color: #fffbdb;
  border-radius: 1.99vw;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0.26vh 0.53vw rgba(0, 0, 0, 0.08);

  &:hover {
    transform: translateY(-1.02vh);
    box-shadow: 0 1.53vh 1.60vw rgba(0, 0, 0, 0.15);

    .image-overlay {
      opacity: 1;
    }
  }
}

.good-image-wrapper {
  position: relative;
  width: 100%;
  height: 31.89vh;
  background-color: #f7fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.good-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;

  .good-card:hover & {
    transform: scale(1.05);
  }
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.view-detail {
  color: #ffffff;
  font-size: 1.06vw;
  font-weight: 600;
  padding: 1.28vh 1.60vw;
  background-color: #ff6700;
  border-radius: 1.60vw;
  box-shadow: 0 0.51vh 0.80vw rgba(255, 103, 0, 0.4);
}

.good-content {
  min-height: 25.51vh;
  width: 100%;
  padding: 1.28vh 0.66vw;
  overflow: hidden;
}

.good-title {
  font-size: 1.00vw;
  font-weight: 500;
  color: #2d3748;
  margin: 0 0 1.53vh 0;
  height: 5.61vh;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.good-shipment{
  font-size: 1.33vw;
  font-weight: bold;
  text-decoration: underline;
  text-decoration-color: #ffe610;
  text-decoration-thickness: 1.02vh;
  text-underline-offset: -0.51vh;
}

.good-footer {
  display: flex;
  justify-content: space-between;
  padding: 0.64vh 0.33vw;

}

.price {
  font-size: 1.33vw;
  font-weight: bolder;
  color: orangered;
}


.goods-user {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.40vw;
  margin-top: 0px;
  padding-top: 1.53vh;
  border-top: 0.13vh solid #f0f0f0;
}

.user-avatar {
  flex-shrink: 0;
}



.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 4.08vh 0;
}


</style>
