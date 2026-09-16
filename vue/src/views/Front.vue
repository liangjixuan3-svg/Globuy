<script setup>
import {ref, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {projectName} from '../../config/config.default'
import {ElMessage} from 'element-plus'
import {SUPPORTED_CURRENCIES, fetchRates} from '@/utils/currency'
import {
  Search,
  Star,
  ChatDotRound,
  User,
  Lock,
  SwitchButton,
  Plus,
  Document,
  Money
} from '@element-plus/icons-vue'
// 路由实例
const router = useRouter()
const route = useRoute()

// 币种管理
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const rates = ref(null)

const handleCurrencyChange = (code) => {
  currentCurrency.value = code
  localStorage.setItem('currency', code)
  // 触发全局事件或刷新页面
  window.dispatchEvent(new CustomEvent('currency-changed', { detail: code }))
}

onMounted(async () => {
  rates.value = await fetchRates()
  // 将汇率存入全局或 localStorage 供子组件使用
  localStorage.setItem('exchange_rates', JSON.stringify(rates.value))
})

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

// 退出登录
const logout = () => {
  localStorage.removeItem('account')
  ElMessage.success('退出成功')
  router.push('/login')
}

const handleUpdateAccount = (updatedAccount) => {
  // 更新父组件中的用户信息
  account.value = updatedAccount
}


const keyword = ref('')

const search = () => {
  if (import.meta.env.VITE_DEMO_MODE === 'true') {
    router.push({ path: '/front/search', query: { keyword: keyword.value, typeId: 0 } })
    return
  }
  location.href = '/front/search?keyword=' + keyword.value + '&typeId=0';
}

const goToHome = () => {
  router.push('/front/home')
}

const isActive = (path) => {
  return route.path === path
}

</script>

<template>
  <div class="front-container">
    <!-- 顶部导航栏 -->
    <header class="header-nav">
      <div class="header-left-warp">
        <div class="logo-warp" @click="goToHome" style="cursor: pointer">
          <div class="logo">
            <img src="../../config/logo.svg" alt="Logo"/>
          </div>
          <!--          <div class="logo-text">{{ projectName }}</div>-->
        </div>

        <div class="search-warp">
          <div class="search-container">
            <input
                v-model="keyword"
                placeholder="搜索你想要的闲置商品..."
                class="search-input"
            />
            <el-button
                class="search-btn"
                type="info"
                @click="search"
            >
              <el-icon class="search-icon">
                <Search/>
              </el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </div>

      <div class="user-warp">
        <!-- 币种切换 -->
        <el-dropdown @command="handleCurrencyChange" class="currency-dropdown" style="margin-right: 20px">
          <span class="el-dropdown-link" style="cursor: pointer; display: flex; align-items: center; gap: 4px; color: #606266; font-size: 14px">
            <el-icon><Money /></el-icon>
            {{ currentCurrency }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in SUPPORTED_CURRENCIES" :key="item.code" :command="item.code">
                {{ item.symbol }} {{ item.name }} ({{ item.code }})
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 未登录状态显示登录注册按钮 -->
        <template v-if="!account.id">
          <div class="btn-login">
            <el-button @click="router.push('/login')">登录</el-button>
          </div>
          <div class="btn-login" style="margin-left: 10px">
            <el-button @click="router.push('/register')">注册</el-button>
          </div>
        </template>

        <!-- 已登录状态显示用户头像和下拉菜单 -->
        <el-dropdown v-else class="custom-dropdown">
          <div class="user-avatar">
            <img :src="account.avatarUrl"/>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>{{ account.nickname }}</el-dropdown-item>
              <el-dropdown-item>
                <router-link to="/front/person" class="dropdown-link">
                  <el-icon>
                    <User/>
                  </el-icon>
                  <span>个人信息</span>
                </router-link>
              </el-dropdown-item>
              <el-dropdown-item>
                <router-link to="/front/password" class="dropdown-link">
                  <el-icon>
                    <Lock/>
                  </el-icon>
                  <span>修改密码</span>
                </router-link>
              </el-dropdown-item>
              <el-dropdown-item>
                <div @click="logout" class="dropdown-link">
                  <el-icon>
                    <SwitchButton/>
                  </el-icon>
                  <span>退出登录</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主内容区域 -->
    <div class="main-content">
      <router-view @update-account="handleUpdateAccount"></router-view>
    </div>

    <!-- 右侧固定操作按钮 -->
    <!-- 为每个按钮添加active类判断 -->
    <div class="floating-sidebar">
      <div
          class="floating-btn publish-btn"
          :class="{ active: isActive('/front/publish') }"
          @click="router.push('/front/publish')"
      >
        <el-icon :size="22">
          <Plus/>
        </el-icon>
        <span class="btn-text">发布</span>
      </div>

      <div class="divider"></div>

      <div
          class="floating-btn"
          :class="{ active: isActive('/front/chat') }"
          @click="router.push('/front/chat')"
      >
        <el-icon :size="22">
          <ChatDotRound/>
        </el-icon>
        <span class="btn-text">消息</span>
      </div>

      <div class="divider"></div>

      <div
          class="floating-btn"
          :class="{ active: isActive('/front/orders') }"
          @click="router.push('/front/orders')"
      >
        <el-icon :size="22">
          <Document/>
        </el-icon>
        <span class="btn-text">订单</span>
      </div>

      <div class="divider"></div>

      <div
          class="floating-btn"
          :class="{ active: isActive('/front/collect') }"
          @click="router.push('/front/collect')"
      >
        <el-icon :size="22">
          <Star/>
        </el-icon>
        <span class="btn-text">收藏</span>
      </div>

      <div class="divider"></div>

      <div
          class="floating-btn"
          :class="{ active: isActive('/front/user') }"
          @click="router.push('/front/user?id='+account.id)"
      >
        <el-icon :size="22">
          <User/>
        </el-icon>
        <span class="btn-text">我的</span>
      </div>

      <div class="divider"></div>

    </div>

  </div>
</template>

<style lang="scss" scoped>

/*定义前台头部 背景 主题色*/
$front-back-color: #ffc400;

/*定义前台头部 字体 主题色*/
$front-font-color: #151111;

.front-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header-nav {
  z-index: 1800;
  position: sticky;
  top: 0;
  height: 8.93vh;
  background-color: $front-back-color;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2.66vw;
  box-shadow: 0 0.26vh 1.28vh 0 rgba(0, 0, 0, 0.1);
  overflow: visible;

  .header-left-warp {
    display: flex;
    align-items: center;
    height: 100%;
    flex: 1;
    justify-content: space-between;

    .logo-warp {
      display: flex;
      align-items: center;
      margin-left: 1.33vw;
      transition: opacity 0.3s;
      min-width: 13.30vw;

      &:hover {
        opacity: 0.8;
      }

      .logo {
        width: 4.65vw;
        height: 8.93vh;
        margin-right: 0.66vw;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .logo-text {
        font-size: 1.46vw;
        font-weight: 500;
        color: $front-font-color;
      }

    }

    .search-warp {
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      width: 33.24vw;
      max-width: 33.24vw;
      margin: 0;

      .search-container {
        position: relative;
        display: flex;
        align-items: center;
        background: #ffffff;
        border-radius: 1.60vw;
        padding: 1.02vh 0.80vw;
        transition: all 0.3s ease;
        border: 0.13vw solid transparent;

        &:hover {
          background: #fff;
          border-color: #151111;
          box-shadow: 0 0.26vh 1.53vh rgba(0, 0, 0, 0.08);
        }

        &:focus-within {
          background: #fff;
          border-color: $front-font-color;
          box-shadow: 0 0.26vh 2.04vh rgba(64, 132, 217, 0.15);
        }

        .search-icon {
          font-size: 1.20vw;
          color: #151111;
          margin-right: 0.53vw;
          transition: color 0.3s;
        }

        .search-input {
          flex: 1;
          border: none;
          outline: none;
          background: transparent;
          font-size: 0.93vw;
          color: #303133;
          padding: 0.51vh 0.53vw;

          &::placeholder {
            color: #a8abb2;
          }
        }

        .search-btn {
          margin-left: 0.53vw;
          border-radius: 1.20vw;
          padding: 1.02vh 1.60vw;
          font-weight: 500;
          background: $front-back-color;
          color: black;
          border: none;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-0.13vh);
            box-shadow: 0 0.51vh 1.53vh rgba(64, 132, 217, 0.3);
          }

          &:active {
            transform: translateY(0);
          }
        }
      }
    }
  }

  .user-warp {
    display: flex;
    align-items: center;
    margin-right: 1.33vw;
    height: 100%;
    min-width: 13.30vw;
    justify-content: flex-end;

    .btn-login {
      margin-top: 0;
    }

    .user-avatar {
      width: 2.66vw;
      height: 5.10vh;
      border-radius: 50%;
      overflow: hidden;
      border: 0.07vw solid $front-font-color;
      padding: 0.26vh;
      cursor: pointer;
      outline: none !important;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
      }

    }

    .dropdown-link {
      display: flex;
      align-items: center;
      color: inherit;
      text-decoration: none;

      .el-icon {
        margin-right: 0.53vw;
      }
    }

  }
}

.main-content {
  flex: 1;
  background-color: #fff;
}

.floating-sidebar {
  width: 3.32vw;
  height: 42.09vh;
  position: fixed;
  right: 0.66vw;
  bottom: 25%;
  z-index: 1000;
  background: #ffffff;
  border-radius: 1.00vw;
  box-shadow: 0 1.02vh 4.08vh rgba(0, 0, 0, 0.12), 0 0.26vh 1.02vh rgba(0, 0, 0, 0.08);
  padding: 1.79vh 0;
  backdrop-filter: blur(1.28vh);
  border: 0.07vw solid rgba(64, 132, 217, 0.1);


  .floating-btn {
    position: relative;
    width: 3.32vw;
    height: 7.65vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    color: #606266;

    .btn-text {
      font-size: 0.80vw;
      margin-top: 0.89vh;
      font-weight: 500;
    }

    &:hover {
      color: $front-font-color;
      background: $front-back-color;
    }

    &:active {
      transform: scale(0.95);
    }

    &.publish-btn {
      color: $front-font-color;
      font-weight: 600;
    }

    &.active {
      color: $front-font-color;
      background: $front-back-color;
      font-weight: 600;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 0.27vw;
        height: 5.10vh;
        background: $front-font-color;
        border-radius: 0 4px 4px 0;
      }
    }
  }

  .divider {
    height: 1px;
    background: linear-gradient(to right, transparent, #e4e7ed 20%, #e4e7ed 80%, transparent);
    margin: 0 12px;
  }
}

</style>
