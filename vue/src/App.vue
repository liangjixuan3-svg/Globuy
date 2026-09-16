<script setup>
const isDemo = import.meta.env.VITE_DEMO_MODE === 'true'
</script>

<template>
  <aside v-if="isDemo" class="demo-notice">
    虚拟数据演示 · 支付、聊天、AI 和认证不连接真实服务 · 修改仅在当前页面会话生效
    <a href="#/login">切换账号（demo / admin，密码 demo）</a>
  </aside>
  <RouterView />
  <footer class="copyright-footer">© 2026 良暨宣 · Globuy</footer>
</template>

<style scoped>
.demo-notice {
  padding: 10px 16px;
  background: #fff5d6;
  color: #584400;
  text-align: center;
  font-size: 12px;
  line-height: 1.6;
}
.demo-notice a { margin-left: 12px; text-decoration: underline; }
.copyright-footer {
  padding: 16px 24px;
  text-align: center;
  background-color: #fff;
  color: #666;
  font-size: 12px;
  line-height: 1.5;
  border-top: 1px solid #eee;
}
</style>
