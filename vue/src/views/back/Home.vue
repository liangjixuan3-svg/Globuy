<script setup>

import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import * as echarts from 'echarts';

const userCount = ref(0)
const loadUserCount = ()=>{
  request.get('/user').then(res=>{
    userCount.value = res.data.length
  })
}
loadUserCount()

const goodsCount = ref(0)
const loadGoodsCount = ()=>{
  request.get('/goods').then(res=>{
    goodsCount.value = res.data.length
  })
}
loadGoodsCount()


//当这个页面上所有元素加载完成后 再执行的事情
onMounted(()=>{

  const chartDom = document.getElementById('main');
  const myChart = echarts.init(chartDom);

  const option = {
    title: {
      text: '不同商品分类闲置商品数量统计',
      subtext: '饼图',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      left: 'center',
      top: 'bottom',
      data: [
        'rose1',
        'rose2',
        'rose3',
        'rose4',
        'rose5',
        'rose6',
        'rose7',
        'rose8'
      ]
    },
    toolbox: {
      show: true,
      feature: {
        mark: { show: true },
        dataView: { show: true, readOnly: false },
        restore: { show: true },
        saveAsImage: { show: true }
      }
    },
    series: [

      {
        name: 'Area Mode',
        type: 'pie',
        radius: [20, 140],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 5
        },
        data: [
          { value: 30, name: 'rose 1' },
          { value: 28, name: 'rose 2' },
          { value: 26, name: 'rose 3' },
          { value: 24, name: 'rose 4' },
          { value: 22, name: 'rose 5' },
          { value: 20, name: 'rose 6' },
          { value: 18, name: 'rose 7' },
          { value: 16, name: 'rose 8' }
        ]
      }
    ]
  };



  request.get('/echarts/count').then(res=>{
    option.series[0].data = res.data.sort(function (a, b) {
      return a.value - b.value;
    });
    option && myChart.setOption(option);
  })

})

</script>

<template>

  <div style="padding: 10px">
    <div style="display: flex;gap: 20px">
      <el-card style="flex: 1;border-radius: 30px;height: 100px">
        <h3>用户数量：{{userCount}}</h3>
      </el-card>
      <el-card style="flex: 1;border-radius: 30px;height: 100px">
        <h3>商品数量：{{goodsCount}}</h3>
      </el-card>
    </div>
  </div>

  <div style="width: 100%;height: 600px;margin-top: 20px" id="main">

  </div>

</template>

<style scoped>

</style>