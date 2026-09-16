<script setup>
import {ref, shallowRef, onBeforeUnmount, onMounted, onUnmounted} from "vue";
import {useRoute, useRouter} from "vue-router";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {UploadFilled, Delete, Medal} from '@element-plus/icons-vue'
import {convertPrice, getCurrencySymbol} from '@/utils/currency'

import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import axios from "axios"
import { worldRegionData } from '@/utils/areaData'
import {serverHost} from "../../../config/config.default.js";

const route = useRoute()
const router = useRouter()

const id = ref(route.query.id)

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)
// 加载用户信息
const user = ref({})
const loadUser = () => {
  request.get("/user/" + id.value).then(res => {
    user.value = res.data;
  })
}
loadUser()

const users = ref([])
const loadUsers = () => {
  request.get("/user").then(res => {
    users.value = res.data
  })
}
loadUsers()

const goods = ref([])
const loadGoods = () => {
  request.get("/goods/user/" + id.value).then(res => {
    goods.value = res.data
  })
}
loadGoods()

// 获取用户评价
const reviews = ref([])
const loadReviews = () => {
  request.get("/orders/user/" + id.value).then(res => {
    reviews.value = res.data
  })
}
loadReviews()

const activeTab = ref('宝贝')
// 切换标签
const handleTabChange = (tab) => {
  activeTab.value = tab
  if (tab === '历史评价') {
    loadReviews()
  }
}


// 保存
const save = () => {

  if (imgList.value.length > 0) {
    form.value.imgList = imgList.value.join(',');
  } else {
    form.value.imgList = '';
  }
  form.value.content = htmlContent.value;
  if (form.value.place) {
    form.value.place = form.value.place.join('/');
  } else {
    form.value.place = '';
  }

  request.post("/goods", form.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("保存成功")
      dialogFormVisible.value = false
      loadGoods()
    } else {
      ElMessage.error("保存失败")
    }
  })
}
// 表单数据
const form = ref({})
const dialogFormVisible = ref(false)
// 编辑
const handleEdit = (item) => {
  form.value = JSON.parse(JSON.stringify(item))

  if (form.value.imgList) {
    imgList.value = form.value.imgList.split(',');
  } else {
    imgList.value = [];
  }
  htmlContent.value = form.value.content || '';
  if (form.value.place) {
    form.value.place = form.value.place.split('/');
  } else {
    form.value.place = [];
  }

  dialogFormVisible.value = true
}
// 删除
const del = (id) => {
  request.delete("/goods/" + id).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功")
      loadGoods()
    } else {
      ElMessage.error("删除失败")
    }
  })
}
// 确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm(
      '确定要删除这条数据吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        del(id)
      })
}
// 商品主图上传
const handleImgUploadSuccess = (res) => {
  form.value.img = res;
};

// 更多图片列表
const imgList = ref([])

// 多图片上传成功处理
const handleImgListUploadSuccess = (res) => {
  imgList.value.push(res);
};

// 删除已上传的图片
const removeImgList = (index) => {
  imgList.value.splice(index, 1);
};

// 商品详情描述富文本
const htmlContent = ref('');
const editorRefContent = shallowRef();

// 获取表格中显示的图片列表
const getImageList = (imgString) => {
  if (!imgString) return [];
  return imgString.split(',');
};





// 自定义上传方法
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

// wangEditor 配置
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


// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRefContent.value
    if (editor == null) return
    editor.destroy()
})

const types = ref([]);
const loadType = () => {
  request.get('/type').then(res => {
    types.value = res.data;
  });
};
loadType()

// 币种相关
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')
const exchangeRates = ref(JSON.parse(localStorage.getItem('exchange_rates') || '{}'))

const updateCurrency = (e) => {
  currentCurrency.value = e.detail
  exchangeRates.value = JSON.parse(localStorage.getItem('exchange_rates') || '{}')
}

onMounted(() => {
  window.addEventListener('currency-changed', updateCurrency)
})

onUnmounted(() => {
  window.removeEventListener('currency-changed', updateCurrency)
})


</script>

<template>

  <div style="min-height: 100vh;width: 80%;background-color: #fffbdb;margin: 0 auto;padding: 2.55vh 1.33vw">
    <!-- 基本信息 -->
    <el-card style="border-radius: 1.33vw;">
      <div style="display: flex;justify-content: space-between;align-items: center">

        <div style="display: flex;justify-content: space-between;align-items: center;gap: 1.33vw">
          <el-avatar :src="user.avatarUrl" style="width: 4.65vw; height: 8.93vh"></el-avatar>
          <div style="display: flex; flex-direction: column; gap: 0.51vh;">
            <div style="display: flex; align-items: center; gap: 0.66vw;">
              <span style="font-size: 1.66vw;font-weight: bolder">{{user.nickname}}</span>
              <el-tag v-if="user.isAuth"  effect="dark" style="border-radius: 0.80vw;border: none; margin-left: 0.66vw; padding: 0 0.53vw;height: 3.19vh;background-color: rgb(26,213,213);font-size: 0.86vw">
                {{ user.school }} 校友
              </el-tag>
            </div>
          </div>
        </div>

        <button class="chat-btn" v-if="user.id !== account.id" @click="router.push('/front/chat?userId='+id)" >去私聊</button>
        <button class="edit-btn-person" v-if="user.id === account.id" @click="router.push('/front/person')">编辑资料</button>
      </div>
    </el-card>

    <!-- 标签栏 -->
    <el-card style="border-radius: 1.33vw;display: flex;margin-top: 2.55vh;margin-bottom: 2.04vh;">
      <div style="display: flex;gap: 1.33vw">
        <div
            :class="['tab-item', { active: activeTab === '宝贝' }]"
            @click="handleTabChange('宝贝')"
        >
          宝贝 <span style="margin-left: 0.27vw;font-size: 0.93vw">{{ goods.length }}</span>
        </div>
        <div
            :class="['tab-item', { active: activeTab === '历史评价' }]"
            @click="handleTabChange('历史评价')"
        >
          历史评价 <span style="margin-left: 0.27vw;font-size: 0.93vw">{{ reviews.length }}</span>
        </div>
      </div>
    </el-card>

    <!-- 内容区域 -->
    <div style="min-height: 51.02vh">
      <div style="margin-top: 2.55vh;display: grid;grid-template-columns: repeat(4, 1fr);gap: 1.66vw;" v-if="activeTab === '宝贝'">
        <div class="item-grid"
             v-for="item in goods" :key="item.id">


          <div style="width: 100%;height: 35.71vh;overflow: hidden;position: relative;"
               @click="router.push('/front/goodsDetail?id=' + item.id)">
            <img :src="item.img" alt="" style="width: 100%;height: 100%;object-fit: fill;border-radius: 1.33vw 1.33vw 0 0" />

            <!-- 已售出标签 -->
            <div v-if="item.status === '已售出'" class="sold-overlay-user">
              <div class="sold-badge-user">
                <img src="../../assets/已售出.png" alt="已售出" />
              </div>
            </div>
          </div>

          <div style="width: 100%;height: 15.31vh;padding: 1.28vh 0.66vw">

            <!-- 第一行 -->
            <div style="display: flex;gap: 0.33vw;align-items: center">
              <div style="width: 2.66vw">
                <span style="font-size: 1.20vw; font-weight: bolder; overflow: hidden;  text-decoration: underline;text-decoration-color: #ffe610;text-decoration-thickness: 1.02vh;text-underline-offset: -0.51vh;">{{item.shipment}}</span>
              </div>
              <div style="line-height: 1.2; overflow: hidden;text-overflow: ellipsis;white-space: nowrap;flex: 1">
                <span style="font-size: 1.00vw;line-height: 1.4;">{{item.name}}</span>
              </div>
            </div>

            <!-- 第二行 -->
            <div style="display: flex;align-items: center;padding: 0.64vh 0.33vw">
              <div>
                <span style="color: orangered;font-size: 1.20vw;font-weight: bolder">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(item.price, currentCurrency, exchangeRates) }}</span>
              </div>
              <div>
                <span style="color: grey;font-size: 0.93vw;text-decoration: line-through;margin-left: 0.53vw">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(item.rePrice, currentCurrency, exchangeRates) }}</span>
              </div>
            </div>

            <el-divider style="margin: 0.38vh 0"></el-divider>

            <!-- 第三行 -->
            <div style="display: flex;gap: 0.53vw;align-content: center;padding: 0.64vh 0.33vw;margin-top: 0.51vh">
              <div>
                <el-avatar :src="user.avatarUrl" style="width: 1.66vw; height: 3.19vh" ></el-avatar>
              </div>
              <div style="display: flex; align-items: center; gap: 0.27vw;">
                <span style="font-size: 0.93vw;color: grey">{{user.nickname}}</span>
                <el-tooltip v-if="user.isAuth" :content="'校友认证：' + user.school" placement="top">
                  <el-icon style="color: #E6A23C;"><Medal /></el-icon>
                </el-tooltip>
              </div>
            </div>

          </div>
          <div class="goods-manager" v-if="user.id===account.id">
            <button class="edit-btn" @click="handleEdit(item)" v-if="item.status==='已上架'">编辑</button>
            <button class="cancel-btn"  @click="confirmDelete(item.id)" v-if="item.status==='已上架'">删除</button>
          </div>
        </div>

      </div>


      <!--评价-->
      <div style="display: flex;flex-direction: column;gap: 2.55vh" v-if="activeTab === '历史评价'">
        <el-card style="border-radius: 1.33vw;width: 100%" v-for="item in reviews" :key="item.id">
          <div style="width: 100%;min-height: 10.20vh;border: 0.07vw solid #fff99a;padding: 2.55vh 1.33vw">
            <!--评价人信息-->
            <div style="display: flex;justify-content: space-between;">

              <div style="display: flex;gap: 0.66vw;align-items: center">
                <div>
                  <el-avatar :src="users.find(u => u.id === item.toId).avatarUrl" style="width: 2.33vw; height: 4.46vh" ></el-avatar>
                </div>
                <div style="display: flex;flex-direction: column;">
                  <span style="font-size: 1.06vw">{{users.find(u => u.id === item.toId).nickname}}</span>
                  <span style="font-size: 0.80vw;color: grey">{{item.time}}</span>
                </div>
              </div>

              <div>
                <el-rate v-model="item.toRate" disabled></el-rate>
              </div>

            </div>
            <!--评价内容-->
            <div style="margin-top: 2.55vh;">
              <span style="font-size: 1.06vw">{{item.toReview}}</span>
            </div>
          </div>

        </el-card>
      </div>

    </div>


    <el-dialog v-model="dialogFormVisible" title="编辑商品" width="40vw" center>
      <el-form :model="form" label-width="6.65vw">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入名称"/>
        </el-form-item>
        <el-form-item label="商品主图">
          <div class="upload-container">
            <el-avatar v-if="form.img" :src="form.img" style="width: 5.32vw; height: 10.20vh" />
            <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :on-success="handleImgUploadSuccess" :show-file-list="false">
              <el-button type="primary" :icon="UploadFilled">{{ form.img ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="更多图片">
          <div class="upload-container">
            <div class="image-list" v-if="imgList.length > 0">
              <div v-for="(img, index) in imgList" :key="index" class="image-item">
                <el-avatar :src="img" style="width: 5.32vw; height: 10.20vh"/>
                <el-button type="danger" circle :icon="Delete" class="delete-btn" @click="removeImgList(index)"></el-button>
              </div>
            </div>
            <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :on-success="handleImgListUploadSuccess" :show-file-list="false" multiple>
              <el-button type="primary" :icon="UploadFilled">上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.typeId" placeholder="请选择分类" style="width: 14.63vw">
            <el-option v-for="item in types" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价">
          <el-input v-model="form.price" type="number" placeholder="请输入售价"/>
        </el-form-item>
        <el-form-item label="原价">
          <el-input v-model="form.rePrice" type="number" placeholder="请输入原价"/>
        </el-form-item>
        <el-form-item label="商品详情描述">
          <div style="border: 0.07vw solid #ccc; z-index: 100;">
            <Toolbar style="border-bottom: 0.07vw solid #ccc" :editor="editorRefContent" :defaultConfig="editorConfig" mode="default" />
            <Editor style="height: 38.27vh; overflow-y: hidden;" v-model="htmlContent" :defaultConfig="editorConfig" mode="default" @onCreated="editorRefContent = $event" />
          </div>
        </el-form-item>
        <el-form-item label="所在地">
            <el-cascader v-model="form.address" :options="worldRegionData" :props="{ value: 'label' }" clearable style="width: 100%" placeholder="请输入所在地"/>
          </el-form-item>
        <el-form-item label="发货设置">
          <el-radio-group v-model="form.shipment">
            <el-radio :value="'包邮'">包邮</el-radio>
            <el-radio :value="'邮费到付'">邮费到付</el-radio>
            <el-radio :value="'无须邮寄'">无须邮寄</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品成色">
          <el-radio-group v-model="form.quality">
            <el-radio :value="'全新'">全新</el-radio>
            <el-radio :value="'9成新'">9成新</el-radio>
            <el-radio :value="'8成新'">8成新</el-radio>
            <el-radio :value="'7成新'">7成新</el-radio>
            <el-radio :value="'6成新及以下'">6成新及以下</el-radio>
          </el-radio-group>
        </el-form-item>

      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </div>
      </template>

    </el-dialog>


  </div>

</template>

<style scoped>
.chat-btn{
  margin-left: 0.66vw;
  height: 5.10vh;
  border-radius: 2.66vw;
  width: 9.97vw;
  background-color: #ff8800;
  color: white;
  font-weight: bolder;
  font-size: 1.00vw;
  border: 0.07vw solid #f4f4f4;
}
.chat-btn:hover{
  background-color: #ff7300;
  border-color: #ff7300;
}

.edit-btn-person{
  margin-left: 0.66vw;
  height: 5.10vh;
  border-radius: 2.66vw;
  width: 9.97vw;
  background-color: #ffe610;
  color: black;
  font-weight: bolder;
  font-size: 1.00vw;
  border: 0.07vw solid #f4f4f4;
}
.edit-btn:hover{
  background-color: #ffdf10;
  border-color: #ffd710;
}


.tab-item {
  padding: 0.64vh 0;
  font-size: 1.06vw;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
}

.tab-item:hover {
  color: #333;
}

.tab-item.active {
  color: #333;
  font-size: 1.20vw;
  font-weight: bold;
  text-decoration: underline;
  text-decoration-color: #ffe610;
  text-decoration-thickness: 1.02vh;
  text-underline-offset: -0.51vh;
}

.item-grid{
  border-radius: 1.33vw;
  background-color: white;
  overflow: hidden;
  position: relative;
  box-shadow: 0 0.26vh 0.53vw rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}
.item-grid:hover{
  transform: scale(1.03);
  box-shadow: 0 0.51vh 1.06vw rgba(0, 0, 0, 0.12);
}

.sold-overlay-user {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(150, 147, 147, 0.6);
  z-index: 1;
  pointer-events: none;
}

.sold-badge-user {
  position: absolute;
  top: 28%;
  left: 28%;
  width: 7.98vw;
  height: 15.31vh;
  z-index: 1;
  pointer-events: none;
}

.sold-badge-user img {
  opacity: 0.5;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.sold-badge-user img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.goods-manager{
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 1.28vh;
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

.delete-btn {
  position: absolute;
  top: -1.02vh;
  right: -0.53vw;
  transform: scale(0.8);
}

.edit-btn{
  margin-left: 0.66vw;
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ff8800;
  color: white;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 0.07vw solid #f4f4f4;
}
.edit-btn:hover{
  background-color: #ff7300;
  border-color: #ff7300;
}

.cancel-btn{
  height: 3.83vh;
  border-radius: 1.99vw;
  width: 6.65vw;
  background-color: #ececec;
  color: black;
  font-weight: bolder;
  font-size: 0.86vw;
  border: 0.07vw solid #f4f4f4;
}
.cancel-btn:hover{
  color: white;
  background-color: #f5222d;
  border-color: #ff7300;
}
</style>
