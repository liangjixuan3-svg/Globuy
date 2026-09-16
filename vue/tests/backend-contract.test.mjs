import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createDemoApi } from '../src/demo/api.js'
const original = JSON.parse(readFileSync(new URL('../src/demo/original-data.json', import.meta.url)))
const ids = rows => rows.map(row => row.id)

test('首页只返回浏览量最高的12件已上架商品', async () => {
  const api = createDemoApi(original)
  assert.deepEqual(ids((await api('get', '/goods/front')).data), [115,124,125,129,122,119,106,114,98,89,95,93])
})

test('热门分类仅返回启用的前4类，每类携带3件浏览量最高的在售商品', async () => {
  const seed = structuredClone(original)
  seed.type.forEach(t => { t.status = t.id !== 2 })
  seed.goods = [
    {id:1,typeId:1,status:'已上架',num:10}, {id:2,typeId:1,status:'已售出',num:100},
    {id:3,typeId:1,status:'已上架',num:30}, {id:4,typeId:1,status:'已上架',num:20},
    {id:5,typeId:1,status:'已上架',num:40}
  ]
  const rows = (await createDemoApi(seed)('get', '/type/front')).data
  assert.deepEqual(ids(rows), [1,3,4,5])
  assert.deepEqual(ids(rows[0].goodsList), [5,3,4])
  assert.deepEqual(rows[1].goodsList, [])
})

test('搜索按浏览量、发布日期和价格排序，国家字段精确筛选后分页', async () => {
  const seed = structuredClone(original)
  seed.goods = [
    {id:1,name:'商品',status:'已上架',num:2,date:'2026-09-15',price:30,country:'中国',place:'北京市/朝阳区'},
    {id:2,name:'商品',status:'已上架',num:10,date:'2026-09-13',price:10,country:'中国',place:'上海市'},
    {id:3,name:'商品',status:'已上架',num:5,date:'2026-09-16',price:20,country:'中国香港',place:'中国香港'},
    {id:4,name:'商品',status:'已售出',num:100,date:'2026-09-17',price:1,country:'中国'}
  ]
  const api = createDemoApi(seed)
  for (const [sortBy, want] of [['all',[2,3,1]], ['new',[3,1,2]], ['price',[2,3,1]]]) {
    assert.deepEqual(ids((await api('get','/goods/front/page',{}, {sortBy,pageSize:10})).data.records), want)
  }
  const result = (await api('get','/goods/front/page',{}, {sortBy:'all',country:'中国',pageSize:1,pageNum:2})).data
  assert.equal(result.total,2)
  assert.deepEqual(ids(result.records),[1])
})

test('下单锁定商品，不能重复下单，取消后重新上架', async () => {
  const api = createDemoApi(original)
  const order = (await api('post','/orders',{itemId:1,addressId:13})).data
  assert.equal((await api('get','/goods/1')).data.status,'已售出')
  assert.equal((await api('post','/orders',{itemId:1,addressId:13})).code,'400')
  await api('get','/orders/cancel/'+order.id)
  assert.equal((await api('get','/goods/1')).data.status,'已上架')
})

test('管理列表使用原字段搜索并按ID倒序，个人评价只返回有评分和评价的订单', async () => {
  const seed = structuredClone(original)
  seed.orders = [{id:1,no:'MATCH-001',fromId:1,toId:2,toRate:null,toReview:null}, {id:2,no:'MATCH-002',fromId:1,toId:2,toRate:5,toReview:'好评'}]
  const api = createDemoApi(seed)
  assert.deepEqual(ids((await api('get','/orders/page',{}, {keyword:'MATCH'})).data.records),[2,1])
  assert.deepEqual(ids((await api('get','/user/page',{}, {keyword:'迟遇'})).data.records),[1])
  assert.deepEqual(ids((await api('get','/orders/user/1')).data),[2])
})

test('聊天列表仅显示聊过的用户，未读数真实计算，清除未读和时间排序生效', async () => {
  const seed = structuredClone(original)
  seed.chat = [
    {id:1,fromUserId:2,toUserId:1,text:'后',time:'2026-09-16 12:00:00',isRead:0},
    {id:2,fromUserId:1,toUserId:2,text:'前',time:'2026-09-16 11:00:00',isRead:1},
    {id:3,fromUserId:3,toUserId:4,text:'无关',time:'2026-09-16 13:00:00',isRead:0}
  ]
  const api = createDemoApi(seed)
  const rows = (await api('get','/chat/user')).data
  assert.deepEqual(ids(rows),[2])
  assert.equal(rows[0].count,1)
  assert.deepEqual(ids((await api('get','/chat/message',{}, {fromUserId:1,toUserId:2})).data),[2,1])
  await api('get','/chat/clear',{}, {fromUserId:1,toUserId:2})
  assert.equal((await api('get','/chat/user')).data[0].count,0)
})

test('新发布商品及卖家认证修改后，商品读取实时带出卖家认证信息', async () => {
  const api = createDemoApi(original)
  const item = (await api('post','/goods',{name:'新发布',price:20,typeId:1,date:'2026-09-16'})).data
  assert.equal((await api('get','/goods/'+item.id)).data.school,'University of Pennsylvania')
  await api('post','/user',{id:1,isAuth:true,school:'更新学校'})
  assert.equal((await api('get','/goods/1')).data.school,'更新学校')
  assert.equal((await api('get','/goods/'+item.id)).data.isAuth,true)
})

test('新订单评价属于卖家，而不是收货地址所属的买家', async () => {
  const api = createDemoApi(original)
  const order = (await api('post','/orders',{itemId:2,addressId:13})).data
  await api('post','/orders',{id:order.id,toRate:5,toReview:'好评'})
  assert.ok((await api('get','/orders/user/10')).data.some(o=>o.id===order.id))
  assert.ok(!(await api('get','/orders/user/1')).data.some(o=>o.id===order.id))
})
