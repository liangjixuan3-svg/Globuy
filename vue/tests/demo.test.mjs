import test from 'node:test'
import assert from 'node:assert/strict'

const module = await import('../src/demo/api.js').catch(() => ({}))

test('演示接口支持登录、搜索、收藏、订单流转，未知接口不伪装成功', async () => {
  assert.equal(typeof module.createDemoApi, 'function', '演示接口尚未实现')
  const api = module.createDemoApi()
  assert.equal((await api('post', '/web/login', { username: 'demo', password: 'wrong' })).code, '400')
  const login = await api('post', '/web/login', { username: 'demo', password: 'demo', role: 'ROLE_USER' })
  assert.equal(login.data.nickname, '演示买家')
  const search = await api('get', '/goods/front/page', null, { keyword: '耳机' })
  assert.equal(search.data.total, 1)
  const id = search.data.records[0].id
  await api('post', '/collect', { itemId: id })
  assert.equal((await api('get', '/goods/collect/page')).data.total, 2)
  await api('delete', '/collect/' + id)
  assert.equal((await api('get', '/goods/collect/page')).data.total, 1)
  const order = await api('post', '/orders', { itemId: id, addressId: 1 })
  assert.equal(order.data.status, '待支付')
  assert.equal(order.data.fromId, 2)
  assert.equal(order.data.toId, 1)
  await api('get', '/orders/pay/' + order.data.id)
  assert.equal((await api('get', '/orders')).data.find(o => o.id === order.data.id).status, '待发货')
  assert.equal((await api('get', '/not-implemented')).code, '400')
  const messages = await api('get', '/chat/message', null, { fromUserId: 1, toUserId: 2 })
  assert.equal(messages.data[0].type, '文字')
})

test('模拟订单使用原界面的终态名称，买卖双方和列表一致', async () => {
  assert.equal(typeof module.createDemoApi, 'function')
  const api = module.createDemoApi()
  const order = await api('post', '/orders', { itemId: 1, addressId: 1 })
  const bought = await api('get', '/orders/front/page', null, { flag: '我买到的' })
  assert.ok(bought.data.records.some(o => o.id === order.data.id))
  const sold = await api('get', '/orders/front/page', null, { flag: '我卖出的' })
  assert.ok(!sold.data.records.some(o => o.id === order.data.id))
  await api('get', '/orders/cancel/' + order.data.id)
  assert.equal((await api('get', '/orders')).data.find(o => o.id === order.data.id).status, '交易关闭')
  await api('get', '/orders/receipt/1')
  assert.equal((await api('get', '/orders')).data.find(o => o.id === 1).status, '交易完成')
})
