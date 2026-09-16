<script setup>
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '@/utils/request'

// 表单数据
const form = reactive({})

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

// 获取用户信息
const getAccount = () => {
  request.get('/web/userInfo').then(res => {
    if (res.code === '200' && res.data) {
      Object.assign(form, res.data)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
getAccount()

// 定义要发出的事件
const emit = defineEmits(['refreshUser'])

// 保存用户信息
const save = () => {
  request.post('/admin', form).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')

      // 只更新昵称和头像到 account 对象，其他属性保持不变
      if (form.nickname) account.value.nickname = form.nickname
      if (form.avatarUrl) account.value.avatarUrl = form.avatarUrl

      // 更新浏览器存储的用户信息
      localStorage.setItem('account', JSON.stringify(account.value))

      // 向父组件发送更新事件，传递更新后的用户信息
      emit('updateAccount', account.value)

    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  })
}

// 头像上传成功处理
const handleAvatarSuccess = (res) => {
  form.avatarUrl = res
}

</script>

<template>
  <div class="person-container">
    <el-card class="person-card">
      <h2 class="card-title">个人信息</h2>

      <el-form label-width="80px">
        <div class="avatar-container">
          <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :show-file-list="false" :on-success="handleAvatarSuccess">
            <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="avatar-tip">点击上传头像</div>
        </div>
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.person-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;

  .person-card {
    max-width: 600px;
    width: 100%;

    .card-title {
      text-align: center;
      margin-top: 0;
      margin-bottom: 20px;
      font-size: 22px;
      color: #333;
    }

    .avatar-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 20px;

      .avatar-tip {
        margin-top: 8px;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 138px;
  height: 138px;
  line-height: 138px;
  text-align: center;
}

.avatar {
  width: 138px;
  height: 138px;
  display: block;
  object-fit: cover;
}
</style>
