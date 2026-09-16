<script setup>
import { useRouter } from 'vue-router'
import { projectName } from '../../config/config.default'

// 路由实例
const router = useRouter()
// 返回登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="not-found-container">
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-message">抱歉，您访问的页面不存在或已被移除</p>
      <el-button
          class="back-button"
          type="primary"
          size="large"
          @click="goToLogin">
        返回登录页
      </el-button>
      <div class="brand-footer">
        <p>© {{ new Date().getFullYear() }} {{ projectName }}. 保留所有权利</p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.not-found-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0f4f8 0%, #d7e3f3 100%);
  position: relative;
  overflow: hidden;
}

.background-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 0;

  .shape {
    position: absolute;
    border-radius: 50%;

    &-1 {
      width: 19.95vw;
      height: 38.27vh;
      background: linear-gradient(45deg, rgba(255, 165, 0, 0.1) 0%, rgba(255, 165, 0, 0.05) 100%);
      top: -12.76vh;
      left: -6.65vw;
    }

    &-2 {
      width: 200px;
      height: 200px;
      background: linear-gradient(45deg, rgba(126, 87, 194, 0.1) 0%, rgba(126, 87, 194, 0.05) 100%);
      bottom: -50px;
      right: 10%;
    }

    &-3 {
      width: 150px;
      height: 150px;
      background: linear-gradient(45deg, rgba(66, 165, 245, 0.1) 0%, rgba(66, 165, 245, 0.05) 100%);
      top: 20%;
      right: -50px;
    }

    &-4 {
      width: 250px;
      height: 250px;
      background: linear-gradient(45deg, rgba(239, 83, 80, 0.08) 0%, rgba(239, 83, 80, 0.04) 100%);
      bottom: 10%;
      left: 10%;
    }
  }
}

.not-found-content {
  width: 33.24vw;
  text-align: center;
  padding: 7.65vh 3.99vw;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(1.28vh);
  border-radius: 2.66vw;
  box-shadow: 0 2.55vh 5.10vh rgba(0, 0, 0, 0.1);
  border: 0.07vw solid rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 1;
}

.error-code {
  font-size: 7.98vw;
  font-weight: 800;
  color: #ff8800;
  line-height: 1;
  margin-bottom: 2.55vh;
  text-shadow: 0 1.28vh 2.55vh rgba(255, 136, 0, 0.2);
}

.error-title {
  font-size: 2.13vw;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.91vh;
}

.error-message {
  font-size: 1.20vw;
  color: #666;
  margin-bottom: 5.10vh;
}

.back-button {
  width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(to right, #4084d9, #1a4f94);
  border: none;

  &:hover {
    background: linear-gradient(to right, #3a76c4, #164785);
  }
}

.brand-footer {
  margin-top: 40px;
  font-size: 12px;
  color: #666;
  opacity: 0.7;
}
</style>