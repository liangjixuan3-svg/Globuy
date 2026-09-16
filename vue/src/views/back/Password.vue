<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 路由实例
const router = useRouter()

// 表单引用
const passFormRef = ref(null)

// 表单数据
const form = reactive({
  password: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  password: [
    {required: true, message: '请输入原密码', trigger: 'blur'},
    {min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请输入确认密码', trigger: 'blur'},
    {min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur'}
  ]
}

// 保存方法
const save = () => {
  // 表单校验合法
  passFormRef.value.validate((valid) => {
    // 如果合法
    if (valid) {
      // 检查两次密码是否一致
      if (form.newPassword !== form.confirmPassword) {
        ElMessage.error('两次输入的新密码不相同')
        return false
      }

      // 发送请求
      request.post('/web/password', form).then(res => {
        if (res.code === '200') {
          ElMessage.success('密码修改成功')
          localStorage.removeItem('account')
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>

<template>
  <div class="password-container">

    <el-card class="password-card">
      <el-form
          ref="passFormRef"
          :model="form"
          :rules="rules"
          label-width="120px"
      >
        <el-form-item label="原密码" prop="password">
          <el-input
              v-model="form.password"
              placeholder="请输入当前密码"
              autocomplete="off"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="form.newPassword"
              placeholder="请输入新密码"
              autocomplete="off"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              placeholder="请再次输入新密码"
              autocomplete="off"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="save">确认修改</el-button>
          <el-button @click="passFormRef.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="password-tips">
        <p> 密码安全提示：</p>
        <ul>
          <li>密码长度至少3位字符</li>
          <li>建议使用字母、数字和特殊字符的组合</li>
          <li>定期更换密码可以提高账户安全性</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.password-container {
  padding: 20px;

  .password-card {
    max-width: 600px;
    margin: 0 auto;

    .el-form {
      margin-top: 20px;
    }

    .password-tips {
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px dashed #eee;

      p {
        font-size: 14px;
        color: #606266;
        margin-bottom: 10px;
        display: flex;
        align-items: center;

      }

      ul {
        padding-left: 20px;
        margin: 0;

        li {
          font-size: 13px;
          color: #909399;
          line-height: 1.8;
        }
      }
    }
  }
}
</style>