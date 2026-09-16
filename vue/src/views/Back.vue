<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {User, Lock, SwitchButton, House, UserFilled, Picture, Bell, Grid, Goods, Van, ShoppingBag} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { projectName } from '../../config/config.default'
import { fetchRates } from '@/utils/currency'

// 币种相关
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const exchangeRates = ref(JSON.parse(localStorage.getItem('exchange_rates') || '{}'))

const currencies = [
  { label: '人民币 (CNY)', value: 'CNY' },
  { label: '美元 (USD)', value: 'USD' },
  { label: '英镑 (GBP)', value: 'GBP' },
  { label: '欧元 (EUR)', value: 'EUR' },
  { label: '港币 (HKD)', value: 'HKD' },
  { label: '日元 (JPY)', value: 'JPY' },
]

const handleCurrencyChange = (val) => {
  localStorage.setItem('currency', val)
  currentCurrency.value = val
  // 触发全局事件，通知其他组件汇率变动
  window.dispatchEvent(new CustomEvent('currency-changed', { detail: val }))
}

// 初始化汇率
onMounted(async () => {
  const rates = await fetchRates()
  exchangeRates.value = rates
  localStorage.setItem('exchange_rates', JSON.stringify(rates))
})

// 路由实例
const router = useRouter()
const route = useRoute()

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

// 侧边栏状态
const isCollapse = ref(false)
const sideWidth = computed(() => isCollapse.value ? 64 : 200)
const logoTextShow = computed(() => !isCollapse.value)

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 主题设置
const themeStatus = ref(parseInt(localStorage.getItem('theme') || '0'))
const themes = ref(['theme1', 'theme2', 'theme3', 'theme4', 'theme5', 'theme6', 'theme7', 'theme8'])

// 抽屉状态
const drawer = ref(false)

// 打开主题设置抽屉
const openThemeDrawer = () => {
  drawer.value = true
}

// 切换主题
const changeTheme = (index) => {
  localStorage.setItem('theme', index.toString())
  themeStatus.value = index
}

// 退出登录
const logout = () => {
  localStorage.removeItem('account')
  ElMessage.success('退出成功')
  router.push('/login')
}

// 刷新用户信息
const handleUpdateAccount = (updatedAccount) => {
  // 更新父组件中的用户信息
  account.value = updatedAccount
}



</script>

<template>
  <div class="admin-layout" :class="themes[themeStatus]">
    <!-- 顶部区域 -->
    <header class="admin-header">
      <div class="header-left">
        <div class="logo-container" :style="{ width: sideWidth + 'px' }" @click="openThemeDrawer">
          <img src="../../config/logo.svg" alt="Logo" class="logo-image" />
          <h1 class="logo-text" v-show="logoTextShow">{{ projectName }}</h1>
        </div>
      </div>

      <div class="header-right" style="display: flex; align-items: center; gap: 20px;">
        <el-select
            v-model="currentCurrency"
            placeholder="切换币种"
            style="width: 120px"
            @change="handleCurrencyChange"
        >
          <el-option
              v-for="item in currencies"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>

        <el-dropdown>
          <div class="user-info">
            <div class="user-avatar">
              <img :src="account.avatarUrl" />
            </div>
            <span class="user-name">{{ account.nickname}}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <!--个人信息页面-->
              <el-dropdown-item v-if="account.role==='ROLE_ADMIN'">
                <router-link to="/back/adminPerson" class="dropdown-link">
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </router-link>
              </el-dropdown-item>
              <!--个人信息页面-->
              <el-dropdown-item>
                <router-link to="/back/password" class="dropdown-link">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </router-link>
              </el-dropdown-item>
              <el-dropdown-item>
                <div @click="logout" class="dropdown-link">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!--内容区域-->
    <div class="admin-container">
      <!-- 左侧菜单区域 -->
      <aside class="admin-sidebar" :style="{ width: sideWidth + 'px'}">
        <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            router
            :collapse-transition="false"
        >

          <!--后台菜单-->

          <el-menu-item index="/back/home">
            <el-icon><House /></el-icon>
            <template #title>后台首页</template>
          </el-menu-item>

          <el-menu-item index="/back/notice">
            <el-icon><Bell /></el-icon>
            <template #title>公告管理</template>
          </el-menu-item>

          <el-menu-item index="/back/banner">
            <el-icon><Picture /></el-icon>
            <template #title>轮播图管理</template>
          </el-menu-item>

          <el-menu-item index="/back/type">
            <el-icon><Grid /></el-icon>
            <template #title>闲置物品分类管理</template>
          </el-menu-item>

          <el-menu-item index="/back/goods">
            <el-icon><Goods /></el-icon>
            <template #title>闲置物品管理</template>
          </el-menu-item>

          <el-menu-item index="/back/address">
            <el-icon><Van /></el-icon>
            <template #title>收货地址管理</template>
          </el-menu-item>

          <el-menu-item index="/back/orders">
            <el-icon><ShoppingBag /></el-icon>
            <template #title>订单管理</template>
          </el-menu-item>

          <el-sub-menu index="" v-if="account.role==='ROLE_ADMIN'">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>系统角色管理</span>
            </template>

            <!--系统角色菜单-->

            <el-menu-item index="/back/admin">
              <el-icon><User /></el-icon>
              <template #title>管理员管理</template>
            </el-menu-item>

            <el-menu-item index="/back/user">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>

            <!--系统角色菜单-->

          </el-sub-menu>

          <!--后台菜单-->

        </el-menu>
      </aside>

      <!-- 主要内容区域 -->
      <main class="admin-content">
        <router-view @update-account="handleUpdateAccount"></router-view>
      </main>
    </div>

    <!-- 主题设置抽屉 -->
    <el-drawer v-model="drawer" title="系统设置" direction="rtl" size="300px">
      <div class="drawer-content">
        <div class="drawer-section">
          <h3>侧边栏设置</h3>
          <div class="drawer-option">
            <span>折叠侧边栏</span>
            <el-switch v-model="isCollapse" active-color="var(--font-color-primary)" inactive-color="#dcdfe6" />
          </div>
        </div>

        <el-divider />

        <div class="drawer-section">
          <h3>主题设置</h3>
          <div class="theme-options">
            <div
                v-for="(theme, index) in themes"
                :key="index"
                class="theme-option"
                :class="[theme, { active: themeStatus === index }]"
                @click="changeTheme(index)"
            >
              <div class="theme-color"></div>
              <div class="theme-check" v-if="themeStatus === index">✓</div>
            </div>
          </div>
        </div>


      </div>
    </el-drawer>

  </div>
</template>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-header {
  height: 7.65vh;
  background-color: var(--font-color-primary);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  box-shadow: 0 0.13vh 0.51vh rgba(0, 0, 0, 0.1);
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
    padding: 0 0.66vw;

    .logo-container {
      height: 7.65vh;
      display: flex;
      align-items: center;
      justify-content: center;

      .logo-image {
        width: 1.99vw;
        height: 3.83vh;
        margin-right: 0.66vw;
      }

      .logo-text {
        font-size: 1.20vw;
        font-weight: 600;
        color: #fff;
        margin: 0;
        white-space: nowrap;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    margin-right: 1.33vw;

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 0.64vh 0.66vw;
      border-radius: 0.27vw;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .user-avatar {
        width: 2.13vw;
        height: 4.08vh;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 0.53vw;
        background-color: #fff;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          outline: none !important;
        }
      }

      .user-name {
        font-size: 0.93vw;
        color: #fff;
      }
    }
  }
}

.admin-container {
  display: flex;
  flex: 1;
  gap: 0.66vw;
  padding: 1.28vh 0.66vw;
  background-color: #f9f9f9;
}

.admin-sidebar {
  min-height: calc(100vh - 10.2vh);
  background-color: #fff;
  box-shadow: 0.27vw 0 1.06vw 0 rgba(29, 35, 41, 0.05);
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  background-color: #fff;
  border-radius: 0.33vw;
}

.dropdown-link {
  display: flex;
  align-items: center;
  color: inherit;
  text-decoration: none;

  .el-icon {
    margin-right: 8px;
  }
}

.drawer-content {
  padding: 2.55vh 1.33vw;

  .drawer-section {
    margin-bottom: 2.55vh;

    h3 {
      margin-top: 0;
      margin-bottom: 2.55vh;
      font-size: 1.06vw;
      color: #333;
    }

    .drawer-option {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.91vh;

      span {
        font-size: 0.93vw;
        color: #606266;
      }
    }
  }

  .theme-options {
    display: flex;
    flex-wrap: wrap;
    gap: 1vw;

    .theme-option {
      position: relative;
      width: 3.99vw;
      height: 7.65vh;
      border-radius: 0.27vw;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 0.13vw solid transparent;

      &.active {
        border-color: #333;
      }

      .theme-color {
        width: 2.66vw;
        height: 5.10vh;
        border-radius: 0.27vw;
        background-color: var(--font-color-primary);
      }

      .theme-check {
        position: absolute;
        bottom: 0.64vh;
        right: 0.33vw;
        width: 1.06vw;
        height: 2.04vh;
        background-color: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.8vw;
        color: var(--font-color-primary);
        font-weight: bold;
      }
    }
  }
}
</style>

<style scoped>

/*/
  menu菜单 整体样式
 */
.el-menu {
  background-color: white !important;
  border: none !important;
}

/*/
  menu菜单 鼠标指针放到具体的某一项上时候的样式
 */
.el-menu-item:hover {
  color: var(--font-color-primary) !important;
  background-color: var(--back-color-primary) !important;
}

/*/
  menu菜单 当前被选择项的样式
 */
.el-menu-item.is-active {
  background-color: var(--back-color-primary) !important;
  color: var(--font-color-primary) !important;
  border-right-style: solid !important;
  border-right-width: 3px !important;
  border-right-color: var(--font-color-primary) !important;
}

/* 使用Vue 3的深度选择器语法 */

/*/
  menu菜单 标题 展开时候的样式
 */
:deep(.el-sub-menu .el-sub-menu__title) {
  background-color: white !important;
}

/*/
  menu菜单 标题 鼠标放到展开时候的样式
 */
:deep(.el-sub-menu .el-sub-menu__title:hover) {
  color: var(--font-color-primary) !important;
  background-color: var(--back-color-primary) !important;
}

/*/
  menu菜单 标题 被展开时候，这个展开标题的样式
 */
:deep(.el-sub-menu.is-opened .el-sub-menu__title) {
  color: var(--font-color-primary) !important;
}

/*/
  去除下拉框的鼠标悬浮边框效果
 */
:deep(.el-dropdown *) {
  outline: none !important;
}

</style>
