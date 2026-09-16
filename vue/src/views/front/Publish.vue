<script setup>
import { ref, computed, shallowRef, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, UploadFilled, MagicStick, TrendCharts } from '@element-plus/icons-vue'
import request from '../../utils/request'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import axios from 'axios'
import {worldRegionData} from '@/utils/areaData'
import {serverHost} from "../../../config/config.default.js";
import {fetchRates} from "@/utils/currency.js";

const exchangeRates = ref({})
const formRef = ref(null)

onMounted(async () => {
  exchangeRates.value = await fetchRates()
})

const rules = {
  img: [
    { required: true, message: '请上传商品主图', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  currency: [
    { required: true, message: '请选择发布币种', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品售价', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入商品详情描述', trigger: 'blur' }
  ],
  place: [
    { required: true, message: '请选择所在城市', trigger: 'change' }
  ]
}

const currencies = [
  { label: '人民币 (CNY)', value: 'CNY' },
  { label: '美元 (USD)', value: 'USD' },
  { label: '英镑 (GBP)', value: 'GBP' },
  { label: '欧元 (EUR)', value: 'EUR' },
  { label: '港币 (HKD)', value: 'HKD' },
  { label: '日元 (JPY)', value: 'JPY' },
]

const editorRefContent = shallowRef();

//默认带出属性
const form = ref({
  quality: '全新',
  shipment: '包邮',
  currency: 'CNY',
  content: '',
})

// 保存
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      if (imgList.value.length > 0) {
        form.value.imgList = imgList.value.join(',');
      } else {
        form.value.imgList = '';
      }

      // 备份原始 place 以防验证失败后显示异常
      const originalPlace = JSON.parse(JSON.stringify(form.value.place))

      if (form.value.place && form.value.place.length > 0) {
        form.value.country = form.value.place[0];
        form.value.place = form.value.place.join('/');
      } else {
        form.value.place = '';
        form.value.country = '';
      }

      // 价格转换逻辑：如果不是 CNY，则根据汇率转换为 CNY 存储
      const submitForm = JSON.parse(JSON.stringify(form.value))
      if (submitForm.currency !== 'CNY' && exchangeRates.value[submitForm.currency]) {
        const rate = exchangeRates.value[submitForm.currency]
        submitForm.price = (submitForm.price / rate).toFixed(2)
        if (submitForm.rePrice) {
          submitForm.rePrice = (submitForm.rePrice / rate).toFixed(2)
        }
      }

      request.post("/goods", submitForm).then(res => {
        if (res.code === '200') {
          ElMessage.success("发布成功")
          // 发布成功后清空表单
          form.value = {
            quality: '全新',
            shipment: '包邮',
            currency: 'CNY',
            content: '',
          }
          imgList.value = []
          if (formRef.value) formRef.value.resetFields()
        } else {
          // 提交失败，还原 place 供用户修改
          form.value.place = originalPlace
          ElMessage.error(res.msg || "发布失败")
        }
      })
    } else {
      ElMessage.error("请完善表单信息")
      return false
    }
  })
}


// 图片上传
const handleImgUploadSuccess  = (res) => {
  form.value.img = res;
};

// 多图列表
const imgList = ref([])

// 多图片上传成功处理
const handleImgListUploadSuccess = (res) => {
  imgList.value.push(res);
};

// 删除已上传的图片
const removeImgList = (index) => {
  imgList.value.splice(index, 1);
};

// 获取表格中显示的图片列表
const getImageList = (imgString) => {
  if (!imgString) return [];
  return imgString.split(',');
};

const types = ref([])
const loadType = () =>{
  request.get('/type').then(res => {
    types.value = res.data
  })
}
loadType()


//富文本自定义上传方法
const customUpload = (file, insertFn) => {
  if (import.meta.env.VITE_DEMO_MODE === "true") { ElMessage.info("演示版不上传文件，请勿输入私人信息"); return }
  const formData = new FormData()
  formData.append('file', file)
  axios({
    url: `${serverHost}/web/upload`,
    method: 'post',
    data: formData,
    headers: {'Content-Type': 'multipart/form-data'},
  }).then(res => {
    insertFn(res.data)
  }).catch((error) => {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  })
}
//wangEditor 配置
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      customUpload: async (file, insertFn) => {
        customUpload(file, insertFn)
      },
    },
    uploadVideo: {
      customUpload: async (file, insertFn) => {
        customUpload(file, insertFn)
      },
    },
  }
}

// AI 助手逻辑
const aiLoading = ref(false)
const generateAIDescription = () => {
  if (!form.value.name) {
    ElMessage.warning('请先输入商品名称')
    return
  }
  if (!form.value.typeId) {
    ElMessage.warning('请先选择商品分类')
    return
  }
  if (!form.value.quality) {
    ElMessage.warning('请先选择商品成色')
    return
  }
  
  const typeName = types.value.find(t => t.id === form.value.typeId)?.name || '未分类'
  
  aiLoading.value = true
  request.get('/ai/generateDescription', {
    params: { 
      name: form.value.name, 
      typeName: typeName,
      quality: form.value.quality
    },
    timeout: 60000 // 为 AI 接口单独设置 60 秒超时
  }).then(res => {
    if (res.code === '200') {
      form.value.content = res.data
      ElMessage.success('描述生成成功')
    } else {
      ElMessage.error(res.msg || '生成失败，请稍后再试')
    }
  }).catch(err => {
    console.error('AI 生成出错:', err)
    if (err.code === 'ECONNABORTED' || err.message.includes('timeout')) {
      ElMessage.error('生成超时，请稍后再试（AI 响应较慢）')
    } else {
      ElMessage.error('网络异常，请检查后端服务是否启动')
    }
  }).finally(() => {
    aiLoading.value = false
  })
}

const suggestPrice = () => {
  if (!form.value.typeId) {
    ElMessage.warning('请先选择商品分类')
    return
  }
  
  request.get('/ai/suggestPrice', {
    params: { 
      typeId: form.value.typeId,
      name: form.value.name || ''
    },
    timeout: 30000 // 价格建议设置 30 秒超时
  }).then(res => {
    if (res.code === '200') {
      const data = res.data
      ElMessage.info({
        message: `建议售价：${data.suggestedPrice} ${form.value.currency}。${data.reason}`,
        duration: 5000,
        showClose: true
      })
      // 自动填入建议价格
      form.value.price = data.suggestedPrice
    } else {
      ElMessage.error(res.msg || '价格建议获取失败')
    }
  }).catch(err => {
    console.error('价格建议出错:', err)
    ElMessage.error('价格建议获取超时或网络异常')
  })
}

</script>

<template>
  <div style="width: 100%;height: 100% ;padding: 6.38vh 3.32vw;background: linear-gradient(135deg, #fffbdb 0%, rgba(255,196,0,0.53) 100%)">
    <div style="width: 65%; margin: 0 auto;min-height: 25.51vh">

      <el-card style="border-radius: 1.99vw">

        <h1>发布闲置</h1>

        <h3>请填写闲置物品信息</h3>
        <el-divider></el-divider>

        <!--表单-->
        <el-form :model="form" :rules="rules" ref="formRef" label-width="6.65vw">

          <!--图片上传组件 -->
          <el-form-item label="商品主图" prop="img">
            <div class="upload-container">
              <el-avatar v-if="form.img" :src="form.img" style="width: 5.32vw; height: 10.20vh" shape="square"/>
              <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :on-success="handleImgUploadSuccess" :show-file-list="false">
                <el-button type="primary" :icon="UploadFilled">{{ form.img ? '更换图片' : '上传图片' }}</el-button>
              </el-upload>
            </div>
          </el-form-item>

          <el-form-item label="更多图片">
            <div class="upload-container">
              <div class="image-list" v-if="imgList.length > 0">
                <div v-for="(img, index) in imgList" :key="index" class="image-item">
                  <el-avatar :src="img" style="width: 5.32vw; height: 10.20vh" shape="square"/>
                  <el-button type="danger" circle :icon="Delete" class="delete-btn" @click="removeImgList(index)"></el-button>
                </div>
              </div>
              <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :on-success="handleImgListUploadSuccess" :show-file-list="false" multiple>
                <el-button type="primary" :icon="UploadFilled">上传图片</el-button>
              </el-upload>
            </div>
          </el-form-item>

          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="简洁清晰的标题可以吸引更多人哦～" :maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="商品分类" prop="typeId">
            <div style="display: flex; gap: 0.66vw; width: 100%">
              <el-select v-model="form.typeId" placeholder="请选择分类" style="flex: 1">
                <el-option
                    v-for="item in types"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
              </el-select>
              <el-button type="success" plain :icon="TrendCharts" @click="suggestPrice">价格建议</el-button>
            </div>
          </el-form-item>

          <el-form-item label="发布币种" prop="currency">
            <el-select v-model="form.currency" placeholder="请选择币种" style="width: 100%">
              <el-option
                  v-for="item in currencies"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="商品价格" prop="price">
            <div style="display: flex; gap: 0.66vw; width: 100%">
              <el-input v-model="form.price" type="number" placeholder="售价">
                <template #prefix>{{ form.currency }}</template>
              </el-input>
              <el-input v-model="form.rePrice" type="number" placeholder="原价">
                <template #prefix>{{ form.currency }}</template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item label="商品成色">
            <el-radio-group v-model="form.quality">
              <el-radio :value="'全新'" border>全新</el-radio>
              <el-radio :value="'9成新'" border>9成新</el-radio>
              <el-radio :value="'8成新'" border>8成新</el-radio>
              <el-radio :value="'7成新'" border>7成新</el-radio>
              <el-radio :value="'6成新及以下'" border>6成新及以下</el-radio>
            </el-radio-group>
          </el-form-item>


          <!-- 富文本的编辑 -->
          <el-form-item label="商品详情描述" prop="content">
            <div style="width: 100%">
              <div style="margin-bottom: 1.28vh; display: flex; justify-content: flex-end">
                <div class="ai-btn-container" :class="{ 'is-loading-marquee': aiLoading }">
                  <el-button 
                    class="ai-fancy-btn"
                    :class="{ 'is-loading-fancy': aiLoading }"
                    type="primary" 
                    plain 
                    :icon="MagicStick" 
                    @click="generateAIDescription" 
                    :loading="aiLoading"
                  >
                    AI 一键生成描述 (基于名称、分类、成色)
                  </el-button>
                </div>
              </div>
              <div style="border: 0.07vw solid #ccc; z-index: 100; width: 100%">
                <Toolbar style="border-bottom: 0.07vw solid #ccc" :editor="editorRefContent" :defaultConfig="editorConfig" mode="default" />
                <Editor style="min-height: 12.76vh; overflow-y: hidden;" v-model="form.content" :defaultConfig="editorConfig" mode="default" @onCreated="editorRefContent = $event" />
              </div>
            </div>
          </el-form-item>

          <el-form-item label="所在城市" prop="place">
            <el-cascader
                v-model="form.place"
                :options="worldRegionData"
                :props="{ value: 'label' }"
                placeholder="请选择省市区"
                clearable
                style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="发货设置">
            <el-radio-group v-model="form.shipment">
              <el-radio value="包邮" border>包邮</el-radio>
              <el-radio value="面交" border>面交</el-radio>
              <el-radio value="线上" border>线上</el-radio>
              <el-radio value="到付" border>到付</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <div style="display: flex;justify-content: space-around;margin-top: 7.65vh">
          <el-button style="width: 19.95vw;height: 6.38vh;border: none;border-radius: 1.99vw;background-color: orange;color: white;font-size: 1.20vw" @click="save">发布商品</el-button>
        </div>

      </el-card>

    </div>

  </div>

</template>

<style scoped>

:deep(.el-card__body){
  padding: 3.83vh 1.99vw;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.66vw;
  margin-bottom: 1.28vh;
}

.image-item {
  position: relative;
}

/* 炫彩 AI 按钮样式 - 恢复初始多色调 */
.ai-btn-container {
  position: relative;
  padding: 0.38vh 0.20vw;
  border-radius: 1.99vw; /* 与按钮一致 */
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  transition: all 0.3s ease;
  width: fit-content;
}

/* 跑马灯特效 - 仅在加载时显示 */
.is-loading-marquee::before {
  content: "";
  position: absolute;
  width: 150%;
  height: 400%; /* 增加高度确保旋转覆盖 */
  background: conic-gradient(
    #ff00cc, #3333ff, #00d4ff, #00ff99, #ffff00, #ff8800, #ff00cc
  );
  animation: rotate-marquee 1.5s linear infinite;
  z-index: 0;
}

.is-loading-marquee::after {
  content: "";
  position: absolute;
  inset: 0.26vh; /* 边框厚度 */
  background: #fff;
  border-radius: 1.86vw;
  z-index: 1;
}

@keyframes rotate-marquee {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-fancy-btn {
  z-index: 2;
  border-radius: 1.99vw;
  height: 4.34vh; /* 稍微调高一点点 */
  width: 19.95vw;
  background: linear-gradient(90deg, #ff00cc, #3333ff, #00d4ff, #ff00cc) !important;
  background-size: 300% 100% !important;
  border: none !important;
  color: white !important;
  margin: 0 !important; /* 移除外边距确保在容器内居中 */
  box-shadow: none !important; /* 容器内不需要额外投影 */
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  font-weight: bold !important;
  text-shadow: 0 0.13vh 0.27vw rgba(0, 0, 0, 0.2);
  box-shadow: 0 0.51vh 1.00vw rgba(51, 51, 255, 0.3);
  animation: rainbow-flow 4s linear infinite;
  position: relative;
  overflow: hidden;
}

.ai-fancy-btn::after {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(45deg);
  transition: 0.5s;
  pointer-events: none;
}

.ai-fancy-btn:hover {
  transform: translateY(-0.38vh) scale(1.02);
  box-shadow: 0 1.02vh 1.66vw rgba(51, 51, 255, 0.5);
  filter: brightness(1.1);
}

.ai-fancy-btn:hover::after {
  left: 120%;
}

/* 处理中状态的动态炫彩效果 - 极速流动 */
.is-loading-marquee {
  box-shadow: 0 0 1.33vw rgba(0, 212, 255, 0.6);
}

.is-loading-fancy {
  animation: rainbow-flow 1s linear infinite !important; 
  pointer-events: none;
  filter: saturate(1.2);
}

@keyframes rainbow-flow {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 100% 50%;
  }
}

/* 调整加载图标颜色为白色 */
:deep(.ai-fancy-btn .el-icon.is-loading) {
  color: white !important;
}

.delete-btn {
  position: absolute;
  top: -1.02vh;
  right: -0.53vw;
  transform: scale(0.8);
}

.AI-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 9.97vw;
  background-color: #ff8800;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 0.07vw solid #f4f4f4;
}

.AI-btn:hover{
  background-color: #ff7300;
  border-color: #ff7300;
}

.AI-btn:after{
  background-color: #ff7300;
  border-color: #ff7300;
}
</style>
