<script setup>

// 表单数据
import {ref} from "vue";
import request from "@/utils/request.js";
import {worldRegionData} from "@/utils/areaData.js";
import {ElMessage, ElMessageBox} from "element-plus";

const form = ref({})
const dialogFormVisible =  ref(false)

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
// 分页大小-1  显示所有数据
const pageSize = ref(-1)

// 加载数据
const load = () => {
   request.get("/address/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: ' ',
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

  request.post("/address", form.value).then(res => {
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

  if (form.value.address) {
    let addressArr = form.value.address.split('/');
    // 兼容旧数据：如果第一项不是国家名且只有三级，补全“中国”
    const countries = worldRegionData.map(c => c.value);
    if (!countries.includes(addressArr[0]) && addressArr.length === 3) {
      addressArr.unshift('中国');
    }
    form.value.address = addressArr;
  } else {
    form.value.address = [];
  }

  dialogFormVisible.value = true
}

// 删除
const del = (id) => {
  request.delete("/address/" + id).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功")
      load()
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

</script>

<template>

  <div style="height: 100vh;width: 70%;background:linear-gradient(135deg, #fffbdb 0%, rgba(255,196,0,0.53) 100%);padding: 2.55vh 1.33vw;margin: 0 auto">
    <!-- 标题 -->
    <el-card style="border-radius: 1.33vw;height: 12.76vh">
      <div style="display: flex;justify-content: space-between;align-items: center;height: 100%">
        <div>
          <h1 style="font-size: 1.60vw">编辑收货地址</h1>
        </div>
        <div>
          <el-button class="add-btn" @click="handleAdd">+ 新增收货地址</el-button>
        </div>
      </div>

    </el-card>

    <!-- 收货地址列表 -->
    <div style="margin-top: 2.55vh;display: flex;flex-direction: column;gap: 2.55vh">
      <el-card v-for="item in tableData" :key="item.id" style="border-radius: 1.33vw">

        <div style="display: flex;justify-content: space-between">
          <div style="display: flex;gap: 0.66vw;align-items: center">
            <span style="font-size: 1.06vw;font-weight: bolder">{{item.name}}</span>
            <span style="font-size: 0.93vw">{{item.phone}}</span>
          </div>
          <div style="display: flex;gap: 0.66vw">
            <button @click="handleEdit(item)" class="action-btn edit">编辑</button>
            <button @click="confirmDelete(item.id)" class="action-btn delete">删除</button>
          </div>
        </div>

        <div style="display: flex;gap: 0.66vw;margin-top: 1.28vh;align-items: center">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
            <circle cx="12" cy="10" r="3"></circle>
          </svg>
          <span style="font-size: 0.93vw">{{item.address}}</span>
          <span style="font-size: 0.93vw">{{item.info}}</span>
        </div>



      </el-card>
    </div>

    <div v-if="tableData.length === 0" class="empty-state">
      <p>暂无收货地址</p>
      <button @click="handleAdd" class="add-btn-secondary">添加地址</button>
    </div>

  </div>


  <!-- 表单对话框 -->
  <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑' : '新增'" width="33.24vw" center>
    <el-form :model="form" label-width="6.65vw">
      <el-form-item label="收货人名">
        <el-input v-model="form.name" placeholder="请输入收货人名"/>
      </el-form-item>
      <el-form-item label="地址">
        <el-cascader v-model="form.address" :options="worldRegionData" :props="{ value: 'label' }" clearable style="width: 100%" placeholder="请输入地址"/>
      </el-form-item>
      <el-form-item label="具体地址">
        <el-input v-model="form.info" type="textarea" placeholder="请输入具体地址"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" placeholder="请输入联系电话"/>
      </el-form-item>

    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false" style="font-size: 0.86vw; width: 6.65vw; height: 3.83vh; border-radius: 1.99vw">取消</el-button>
        <el-button type="primary" @click="save" style="font-size: 0.86vw; width: 6.65vw; height: 3.83vh; border-radius: 1.99vw">确定</el-button>
      </div>
    </template>

  </el-dialog>

</template>

<style scoped>

.add-btn {
  width: 9.97vw;
  height: 5.74vh;
  background: #ff6b00;
  color: white;
  border: none;
  padding: 1.28vh 1.60vw;
  border-radius: 0.66vw;
  font-size: 1.00vw;
  cursor: pointer;
  transition: background 0.3s;
}

.add-btn:hover {
  background: #e55f00;
}

.action-btn {
  border: none;
  background: none;
  color: #666;
  cursor: pointer;
  font-size: 0.93vw;
  padding: 0.51vh 0.27vw;
  transition: color 0.3s;
}

.action-btn.edit:hover {
  color: #ff6b00;
}

.action-btn.delete:hover {
  color: #ff4444;
}

.icon {
  width: 1.20vw;
  height: 2.30vh;
  flex-shrink: 0;
  margin-top: 0.26vh;
  color: #ff6b00;
}

.empty-state {
  text-align: center;
  padding: 7.65vh 1.33vw;
  background: white;
  border-radius: 1.33vw;
}

.add-btn-secondary {
  background: #ff6b00;
  color: white;
  border: none;
  padding: 1.28vh 2.13vw;
  border-radius: 0.40vw;
  font-size: 1.00vw;
  font-weight: bolder;
  cursor: pointer;
}

.empty-state p {
  color: #999;
  font-size: 1.06vw;
  margin-bottom: 3.06vh;
}
</style>