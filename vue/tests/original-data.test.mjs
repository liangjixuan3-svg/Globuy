import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, existsSync } from 'node:fs'
import { createDemoApi } from '../src/demo/api.js'
const seed = JSON.parse(readFileSync(new URL('../src/demo/original-data.json', import.meta.url)))

test('公开演示保留原虚构商品及图片、用户、订单、聊天，不包含原密码', async () => {
  const api = createDemoApi(seed, '/Globuy/')
  assert.equal(seed.goods.length, 70)
  assert.equal(seed.sysUser.length, 10)
  assert.ok(!JSON.stringify(seed).includes('"password"'))
  const item = (await api('get', '/goods/1')).data
  assert.equal(item.name, seed.goods[0].name)
  assert.equal(item.price, 188.88)
  assert.equal(item.content, seed.goods[0].content)
  assert.equal(item.img, '/Globuy/' + seed.goods[0].img)
  const account = (await api('post', '/web/login', { username: 'demo', password: 'demo', role: 'ROLE_USER' })).data
  assert.equal(account.nickname, seed.sysUser[0].nickname)
  const messages = (await api('get', '/chat/message', {}, { fromUserId: 1, toUserId: 2 })).data
  assert.deepEqual(messages, seed.chat.filter(c => (c.fromUserId === 1 && c.toUserId === 2) || (c.fromUserId === 2 && c.toUserId === 1)).map(c => ({ ...c, text: c.text.replaceAll('demo-assets/', '/Globuy/demo-assets/') })))
  const refs = [...new Set([...JSON.stringify(seed).matchAll(/demo-assets\/([^"<>\s,]+?)(?=\\?"|<|\s|,|$)/g)].map(m => m[1]))]
  assert.deepEqual(refs.filter(n => !existsSync(new URL('../public/demo-assets/' + n, import.meta.url))).sort(), ['65318436b3cd4bd2a469735ae7b3f122.png', '80f3266cae764cc89b5fdace97f09240.JPG'])
})

test('原用户的多个地址能完整展示，并使用实际选中的地址下单', async () => {
  const api = createDemoApi(seed)
  const rows = (await api('get', '/address/page', {}, { pageSize: -1, keyword: ' ' })).data.records
  assert.deepEqual(rows, seed.address.filter(a => a.userId === seed.sysUser[0].id))
  const address = rows[1]
  const order = (await api('post', '/orders', { itemId: seed.goods[1].id, addressId: address.id })).data
  assert.equal(order.address, address.address)
  assert.equal(order.phone, address.phone)
})
