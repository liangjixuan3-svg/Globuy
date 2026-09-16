<script setup>
import { ref, reactive } from 'vue'
import { Plus, Medal } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '@/utils/request'

// 表单数据
const form = reactive({})

// 认证表单
const authForm = reactive({
  email: '',
  school: ''
})
const authDialogVisible = ref(false)

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

// 获取用户信息
const getAccount = () => {
  request.get('/web/userInfo').then(res => {
    if (res.code === '200' && res.data) {
      Object.assign(form, res.data)
      authForm.email = res.data.email
      authForm.school = res.data.school
    } else {
      ElMessage.error(res.msg)
    }
  })
}
getAccount()

// 提交认证
const submitAuth = () => {
  if (!authForm.email.endsWith('.edu')) {
    ElMessage.warning('请使用以 .edu 结尾的校友邮箱进行认证')
    return
  }
  if (!authForm.school) {
    ElMessage.warning('请填写所属学校名称')
    return
  }
  request.post('/web/auth', authForm).then(res => {
    if (res.code === '200') {
      ElMessage.success('校友认证成功！')
      authDialogVisible.value = false
      getAccount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 定义要发出的事件
const emit = defineEmits(['refreshUser'])

// 保存用户信息
const save = () => {
  request.post('/user', form).then(res => {
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

      <el-form label-width="5.32vw">
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
        <el-form-item label="校友认证">
          <div style="display: flex; align-items: center;">
            <el-tag v-if="form.isAuth" type="success" size="large" style="margin-right: 0.66vw">
              <el-icon><Medal /></el-icon> {{ form.school }} 已认证
            </el-tag>
            <el-tag v-else type="info" size="large" style="margin-right: 0.66vw">未认证</el-tag>
            <el-button type="warning" plain @click="authDialogVisible = true">{{ form.isAuth ? '重新认证' : '去认证' }}</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="authDialogVisible" title="校友身份认证" width="26.60vw" center>
      <div style="padding: 1.28vh 0.66vw">
        <div style="margin-bottom: 2.55vh; color: #666; font-size: 0.93vw; text-align: center;">
          使用您的 <b>.edu</b> 结尾的校友邮箱即可快速完成认证
        </div>
        <el-form :model="authForm" label-width="5.32vw">
          <el-form-item label="所属学校">
            <el-input v-model="authForm.school" placeholder="例如：南加州大学"></el-input>
          </el-form-item>
          <el-form-item label="校友邮箱">
            <el-input v-model="authForm.email" placeholder="example@usc.edu"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAuth">提交认证</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.person-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2.55vh 1.33vw;

  .person-card {
    border-radius: 1.99vw;
    background:linear-gradient(135deg, #fffbdb 0%, rgba(255,196,0,0.53) 100%);
    max-width: 39.89vw;
    width: 100%;

    .card-title {
      text-align: center;
      margin-top: 0;
      margin-bottom: 2.55vh;
      font-size: 1.46vw;
      color: #333;
    }

    .avatar-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 2.55vh;

      .avatar-tip {
        margin-top: 1.02vh;
        font-size: 0.80vw;
        color: #909399;
      }
    }
  }
}

.avatar-uploader-icon {
  font-size: 1.86vw;
  color: #8c939d;
  width: 9.18vw;
  height: 17.60vh;
  line-height: 17.60vh;
  text-align: center;
}

.avatar {
  width: 9.18vw;
  height: 17.60vh;
  display: block;
  object-fit: cover;
}
</style>
