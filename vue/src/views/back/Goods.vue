<script setup>
import { ref, reactive, shallowRef} from 'vue'
import {Search, Plus, Delete, Edit, UploadFilled} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '../../utils/request'
//引入富文本组件
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
//网络请求
import axios from "axios"

import { worldRegionData } from '@/utils/areaData'
import { convertPrice, getCurrencySymbol } from '@/utils/currency'
import { onMounted, onUnmounted } from 'vue'

const currencies = [
  { label: '人民币 (CNY)', value: 'CNY' },
  { label: '美元 (USD)', value: 'USD' },
  { label: '英镑 (GBP)', value: 'GBP' },
  { label: '欧元 (EUR)', value: 'EUR' },
  { label: '港币 (HKD)', value: 'HKD' },
  { label: '日元 (JPY)', value: 'JPY' },
]

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


//定义富文本数据
const htmlContent = ref('');
const editorRefContent = shallowRef();

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
//定义数据
const contentViewVisible = ref(false)
const currentViewContent = ref('')

const viewContent = (content) => {
  currentViewContent.value = content || ''
  contentViewVisible.value = true
}

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  keyword: '',
  country: '',
})

// 表单数据
const form = ref({})
const dialogFormVisible = ref(false)
const multipleSelection = ref([])

// 加载数据
const load = () => {
  request.get("/goods/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      country: searchForm.country,
    }
  }).then(res => {
    if (res.data) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  })
}
load()

// 保存
const save = () => {

  if (imgList.value.length > 0) {
    form.value.imgList = imgList.value.join(',');
  } else {
    form.value.imgList = '';
  }

  form.value.content = htmlContent.value;

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
    // 价格存储统一为 CNY： 外币价格 / 汇率 = CNY价格
    submitForm.price = (submitForm.price / rate).toFixed(2)
    submitForm.rePrice = (submitForm.rePrice / rate).toFixed(2)
  }

  request.post("/goods", submitForm).then(res => {
    if (res.code === '200') {
      ElMessage.success("保存成功")
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error("保存失败")
    }
  })
}

// 添加
const handleAdd = () => {
  form.value = {}
  imgList.value = []
  htmlContent.value = '';
  dialogFormVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))

  // 如果发布时不是 CNY，回显时需要将数据库中的 CNY 价格转回发布时的原始币种价格
  if (form.value.currency && form.value.currency !== 'CNY' && exchangeRates.value[form.value.currency]) {
    const rate = exchangeRates.value[form.value.currency]
    // 原始价格 = CNY 价格 * 汇率
    form.value.price = (form.value.price * rate).toFixed(2)
    form.value.rePrice = (form.value.rePrice * rate).toFixed(2)
  }

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
      load()
    } else {
      ElMessage.error("删除失败")
    }
  })
}

// 批量删除
const delBatch = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录")
    return
  }

  const ids = multipleSelection.value.map(v => v.id)
  request.post("/goods/del/batch", ids).then(res => {
    if (res.code === '200') {
      ElMessage.success("批量删除成功")
      load()
    } else {
      ElMessage.error("批量删除失败")
    }
  })
}

// 重置搜索
const reset = () => {
  searchForm.keyword = ""
  searchForm.country = ""
  load()
}

// 表格选择变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

// 页码变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
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

// 确认批量删除
const confirmBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录")
    return
  }

  ElMessageBox.confirm(
      '确定要批量删除这些数据吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        delBatch()
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

//获取type
const types = ref([])
const loadType = () =>{
  request.get('/type').then(res => {
    types.value = res.data
  })
}
loadType()

//获取user
const users = ref([])
const loadUser = () =>{
  request.get('/user').then(res => {
    users.value = res.data
  })
}
loadUser()


</script>

<template>
  <div class="content-container">

    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input v-model="searchForm.keyword" placeholder="请输入商品名称" class="filter-input" :prefix-icon="Search" clearable/>
      <el-select v-model="searchForm.country" placeholder="请选择国家" clearable class="ml-10" style="width: 150px">
        <el-option
            v-for="item in worldRegionData"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作按钮区域 -->
    <div class="toolbar-section">
      <el-button plain type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
      <el-button plain type="danger" @click="confirmBatchDelete" :icon="Delete">批量删除</el-button>
    </div>

    <!-- 表格区域 -->
    <el-card>
      <el-table :data="tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="商品名称"/>

        <el-table-column label="图片" width="120" align="center">
          <template #default="scope">
            <el-image style="width: 80px; height: 80px" :src="scope.row.img" :preview-src-list="[scope.row.img]" :preview-teleported=true></el-image>
          </template>
        </el-table-column>

        <el-table-column label="多张图片" width="120" align="center">
          <template #default="scope">
            <el-image style="width: 80px; height: 80px" :src="getImageList(scope.row.imgList)[0]" :preview-src-list="getImageList(scope.row.imgList)" :preview-teleported=true></el-image>
          </template>
        </el-table-column>

        <el-table-column  label="分类">
          <template #default="scope">
            <span v-if="scope.row.typeId ">{{types.find(item=>item.id === scope.row.typeId).name}}</span>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="售价">
          <template #default="scope">
            <span style="color: orangered; font-weight: bold">{{ getCurrencySymbol(currentCurrency) }} {{ convertPrice(scope.row.price, currentCurrency, exchangeRates) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="rePrice" label="原价">
          <template #default="scope">
            <span style="color: grey; text-decoration: line-through">{{ getCurrencySymbol(currentCurrency) }} {{ convertPrice(scope.row.rePrice, currentCurrency, exchangeRates) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="country" label="国家">
          <template #default="scope">
            <el-tag v-if="scope.row.country" effect="plain">{{ scope.row.country }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="详情" width="100" align="center">
          <template #default="scope">
            <el-button type="primary" @click="viewContent(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>

        <el-table-column prop="place" label="所在地"/>

        <el-table-column label="发货设置">
          <template #default="scope">
            <el-tag type="primary">{{scope.row.shipment}}</el-tag>
          </template>
        </el-table-column>

        <el-table-column  label="用户">
          <template #default="scope">
            <span v-if="scope.row.userId ">{{users.find(item=>item.id === scope.row.userId).nickname}}</span>
          </template>
        </el-table-column>

        <el-table-column prop="num" label="浏览量"/>

        <el-table-column label="状态">
          <template #default="scope">
            <el-tag type="primary">{{scope.row.status}}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="商品成色">
          <template #default="scope">
            <el-tag type="primary">{{scope.row.quality}}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="date" label="发布日期"/>



        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top" :effect="'light'">
              <el-button circle type="primary" :icon="Edit" @click="handleEdit(scope.row)"/>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :effect="'light'">
              <el-button circle type="danger" :icon="Delete" @click="confirmDelete(scope.row.id)"/>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-section">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>





    <!-- 表单对话框 (新增/编辑) -->
    <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑' : '新增'" width="30%" destroy-on-close center>
      <el-form :model="form" label-width="100px">

        <el-form-item label="说明" required>
          <el-input v-model="form.name" placeholder="请输入说明" />
        </el-form-item>

        <!--图片上传组件 -->
        <el-form-item label="图片上传">
          <div class="upload-container">
            <el-avatar v-if="form.img" :src="form.img" :size="80" shape="square"/>
            <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :on-success="handleImgUploadSuccess" :show-file-list="false">
              <el-button type="primary" :icon="UploadFilled">{{ form.img ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="多张图片">
          <div class="upload-container">
            <div class="image-list" v-if="imgList.length > 0">
              <div v-for="(img, index) in imgList" :key="index" class="image-item">
                <el-avatar :src="img" :size="80" shape="square"/>
                <el-button type="danger" circle :icon="Delete" class="delete-btn" @click="removeImgList(index)"></el-button>
              </div>
            </div>
            <el-upload :disabled="$demoMode" :action="`${serverHost}/web/upload`" :on-success="handleImgListUploadSuccess" :show-file-list="false" multiple>
              <el-button type="primary" :icon="UploadFilled">上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="分类" required>
          <el-select v-model="form.typeId" placeholder="Select" style="width: 240px">
            <el-option
                v-for="item in types"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="币种" required>
          <el-select v-model="form.currency" placeholder="请选择币种" style="width: 240px">
            <el-option
                v-for="item in currencies"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="售价" required>
          <el-input v-model="form.price" type="number" placeholder="请输入售价">
            <template #prefix>{{ form.currency || 'CNY' }}</template>
          </el-input>
        </el-form-item>

        <el-form-item label="原价" required>
          <el-input v-model="form.rePrice" type="number" placeholder="请输入原价">
            <template #prefix>{{ form.currency || 'CNY' }}</template>
          </el-input>
        </el-form-item>

        <!-- 富文本的编辑 -->
        <el-form-item label="详情">
          <div style="border: 1px solid #ccc; z-index: 100;">
            <Toolbar style="border-bottom: 1px solid #ccc" :editor="editorRefContent" :defaultConfig="editorConfig" mode="default" />
            <Editor style="height: 300px; overflow-y: hidden;" v-model="htmlContent" :defaultConfig="editorConfig" mode="default" @onCreated="editorRefContent = $event" />
          </div>
        </el-form-item>

        <el-form-item label="所在城市">
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
            <el-radio value="包邮">包邮</el-radio>
            <el-radio value="面交">面交</el-radio>
            <el-radio value="线上">线上</el-radio>
            <el-radio value="到付">到付</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="用户" required>
          <el-select v-model="form.userId" placeholder="Select" style="width: 240px">
            <el-option
                v-for="item in users"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="浏览量" required>
          <el-input v-model="form.num" type="number" placeholder="请输入" />
        </el-form-item>

        <el-form-item label="状态" required>
          <el-input v-model="form.status" placeholder="请输入" />
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

        <el-form-item label="发布日期">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期"></el-date-picker>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 弹窗 -->
    <el-dialog v-model="contentViewVisible" title="详情" width="40%" center>
      <div v-html="currentViewContent"></div>
    </el-dialog>

  </div>
</template>

<style scoped>
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.image-item {
  position: relative;
}

.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  transform: scale(0.8);
}

</style>
