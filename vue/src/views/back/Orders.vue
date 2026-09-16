<script setup>
import { ref, reactive } from 'vue'
import { Search, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { worldRegionData } from '@/utils/areaData'
import { convertPrice, getCurrencySymbol } from '@/utils/currency'
import { onMounted, onUnmounted } from 'vue'


// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

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

// 搜索条件
const searchForm = reactive({
  keyword: '',
})

// 表单数据
const form = ref({})
const dialogFormVisible = ref(false)
const multipleSelection = ref([])

// 用户信息
const account = ref(localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {})

// 加载数据
const load = () => {
  request.get("/orders/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
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

  if (form.value.address) {
    form.value.address = form.value.address.join('/');
  } else {
    form.value.address = '';
  }

  // 价格转换逻辑：如果当前币种不是 CNY，则根据汇率转换为 CNY 存储
  const submitForm = JSON.parse(JSON.stringify(form.value))
  if (currentCurrency.value !== 'CNY' && exchangeRates.value[currentCurrency.value]) {
    const rate = exchangeRates.value[currentCurrency.value]
    // 价格存储统一为 CNY： 当前币种价格 / 汇率 = CNY价格
    submitForm.price = (submitForm.price / rate).toFixed(2)
  }

  request.post("/orders", submitForm).then(res => {
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
  dialogFormVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))

  // 将数据库中的 CNY 价格转换为当前显示的币种价格
  if (currentCurrency.value !== 'CNY' && exchangeRates.value[currentCurrency.value]) {
    const rate = exchangeRates.value[currentCurrency.value]
    form.value.price = (form.value.price * rate).toFixed(2)
  }

  if (form.value.address) {
    form.value.address = form.value.address.split('/');
  } else {
    form.value.address = [];
  }

  dialogFormVisible.value = true
}

// 删除
const del = (id) => {
  request.delete("/orders/" + id).then(res => {
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
  request.post("/orders/del/batch", ids).then(res => {
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



const goodss = ref([]);
const loadGoods = () => {
  request.get('/goods').then(res => {
    goodss.value = res.data;
  });
};
loadGoods();

const users = ref([]);
const loadUser = () => {
  request.get('/user').then(res => {
    users.value = res.data;
  });
};
loadUser();



</script>

<template>
  <div class="content-container">

    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input v-model="searchForm.keyword" placeholder="请输入订单号" class="filter-input" :prefix-icon="Search" clearable/>
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
        <el-table-column type="selection" width="60" align="center"/>
        <el-table-column prop="id" label="ID" width="80" align="center"/>
        <el-table-column prop="no" label="订单号"/>
        <el-table-column label="商品" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.itemId">{{ goodss.find(item => item.id === scope.row.itemId)?.name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="itemName" label="商品名称(快照)" width="100" align="center"></el-table-column>

        <el-table-column label="商品图片(快照)" width="100" align="center">
          <template #default="scope">
            <el-image style="width: 80px; height: 80px" :src="scope.row.itemImg" :preview-src-list="[scope.row.itemImg]" :preview-teleported=true></el-image>
          </template>
        </el-table-column>

        <el-table-column label="卖家" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.fromId">{{ users.find(item => item.id === scope.row.fromId)?.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column label="买家" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.toId">{{ users.find(item => item.id === scope.row.toId)?.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold">{{ getCurrencySymbol(currentCurrency) }}{{ convertPrice(scope.row.price, currentCurrency, exchangeRates) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="支付时间" width="140" align="center"/>
        <el-table-column prop="status" label="状态"/>
        <el-table-column prop="carrier" label="物流公司"/>
        <el-table-column prop="trackingNo" label="物流单号"/>
        <el-table-column prop="deliveryTime" label="发货时间" width="140" align="center"/>
        <el-table-column label="买家评分" width="100" align="center">
          <template #default="scope">
            <el-rate v-model="scope.row.toRate" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="toReview" label="买家评价"/>
        <el-table-column prop="address" label="省市区"/>
        <el-table-column prop="info" label="详细地址"/>
        <el-table-column prop="name" label="收货人姓名"/>
        <el-table-column prop="phone" label="收货人联系方式"/>

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

    <!-- 表单对话框 -->
    <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑' : '新增'" width="30%" center>
      <el-form :model="form" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="form.no" placeholder="请输入订单号"/>
        </el-form-item>
        <el-form-item label="商品">
          <el-select v-model="form.itemId" placeholder="请选择商品" style="width: 220px">
            <el-option v-for="item in goodss" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="卖家">
          <el-select v-model="form.fromId" placeholder="请选择卖家" style="width: 220px">
            <el-option v-for="item in users" :key="item.id" :label="item.nickname" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="买家">
          <el-select v-model="form.toId" placeholder="请选择买家" style="width: 220px">
            <el-option v-for="item in users" :key="item.id" :label="item.nickname" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="form.price" type="number" placeholder="请输入价格">
            <template #prepend>{{ getCurrencySymbol(currentCurrency) }}</template>
          </el-input>
        </el-form-item>
        <el-form-item label="支付时间">
          <el-date-picker v-model="form.time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="form.status" placeholder="请输入状态"/>
        </el-form-item>
        <el-form-item label="物流公司">
          <el-input v-model="form.carrier" placeholder="请输入物流公司"/>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="form.trackingNo" placeholder="请输入物流单号"/>
        </el-form-item>
        <el-form-item label="发货时间">
          <el-date-picker v-model="form.deliveryTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="买家评分">
          <el-rate v-model="form.toRate"/>
        </el-form-item>
        <el-form-item label="买家评价">
          <el-input v-model="form.toReview" type="textarea" placeholder="请输入买家评价"/>
        </el-form-item>
        <el-form-item label="省市区">
          <el-cascader v-model="form.address" :options="worldRegionData" :props="{ value: 'label' }" clearable style="width: 100%" placeholder="请输入省市区"/>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.info" type="textarea" placeholder="请输入详细地址"/>
        </el-form-item>
        <el-form-item label="收货人姓名">
          <el-input v-model="form.name" placeholder="请输入收货人姓名"/>
        </el-form-item>
        <el-form-item label="收货人联系方式">
          <el-input v-model="form.phone" placeholder="请输入收货人联系方式"/>
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


</style>