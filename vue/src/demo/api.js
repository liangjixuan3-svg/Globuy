// In-memory demo only; the optional seed contains author-approved fictional business records.
const illustration = (label, icon = '✦', color = '#fff2b3') => 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="900" height="600" viewBox="0 0 900 600"><rect width="900" height="600" fill="${color}"/><circle cx="450" cy="250" r="150" fill="white" opacity=".7"/><text x="450" y="290" text-anchor="middle" font-size="120">${icon}</text><text x="450" y="490" text-anchor="middle" font-family="sans-serif" font-size="42" fill="#343434">${label}</text></svg>`)

export function createDemoApi(seed, baseUrl = '/') {
  let account = { id: 1, username: 'demo', nickname: '演示买家', role: 'ROLE_USER', token: 'demo-only-not-a-real-token', avatarUrl: illustration('DEMO', '☺'), email: 'demo@example.invalid', phone: '', school: '示例大学', isAuth: true }
  const users = [account, { ...account, id: 2, username: 'seller', nickname: '校园好物分享者', email: 'seller@example.invalid', avatarUrl: illustration('SELLER', '☺', '#d9efed') }]
  const admins = [{ ...account, id: 100, username: 'admin', nickname: '演示管理员', role: 'ROLE_ADMIN' }]
  const types = ['数码电子', '图书教材', '生活家居', '运动出行'].map((name, i) => ({ id: i + 1, name, info: '让闲置好物继续发光', status: '启用', icon: illustration(name, ['⌨', '▤', '⌂', '★'][i]), img: illustration(name) }))
  let goods = [
    ['无线降噪耳机', '♫', 260, 1], ['轻薄笔记本电脑', '⌨', 3200, 1], ['英语原版教材', '▤', 90, 2], ['人体工学台灯', '☀', 120, 3], ['校园通勤自行车', '◎', 450, 4], ['便携蓝牙音箱', '♫', 180, 1], ['留学生活指南', '▤', 45, 2], ['收纳置物架', '⌂', 80, 3], ['羽毛球拍套装', '★', 150, 4], ['咖啡手冲器具', '☕', 110, 3]
  ].map(([name, icon, price, typeId], i) => ({ id: i + 1, name, price, rePrice: price * 1.8, typeId, typeName: types[typeId - 1].name, userId: i === 9 ? 1 : 2, img: illustration(name, icon, i % 2 ? '#ddebf5' : '#fff2b3'), imgList: '', content: '<p>爱惜使用，功能完好，适合校园生活。支持当面检查与校园面交。</p><p>这是虚拟演示商品，不提供真实交易。</p>', place: i % 2 ? 'United States/California/示例校区' : '中国香港/示例校区', quality: '九成新', shipment: '自提', status: '已上架', num: 28 + i * 17, school: '示例大学', isAuth: true, time: '2026-09-01 10:00:00' }))
  let orders = [{ id: 1, no: 'DEMO-001', itemId: 10, itemName: goods[9].name, itemImg: goods[9].img, fromId: 2, toId: 1, price: 110, status: '待收货', time: '2026-09-01 10:00:00', name: '虚拟收件人', address: '示例校区', info: '虚拟宿舍', phone: '', shipment: '自提' }]
  let collects = [2]
  const addresses = [{ id: 1, name: '虚拟收件人', address: '中国香港/示例大学/校区', info: '虚拟宿舍 A 栋', phone: '', userId: 1 }]
  const notices = [{ id: 1, name: '欢迎来到 Globuy', info: '本网站使用虚拟数据；交易、聊天、AI 与认证仅为模拟，不连接真实服务。' }]
  const banners = [{ id: 1, name: '让闲置好物继续发光', img: illustration('校园好物 · 让闲置继续发光', '✦') }]
  const chats = [{ id: 1, text: '你好，支持校园面交吗？（模拟消息）', type: '文字', time: '2026-09-01 10:00:00', fromUserId: 2, toUserId: 1, isRead: true }]
  let nextId = 1000
  if (seed) {
    const restore = value => typeof value === 'string' ? value.replace(/demo-assets\/(?:80f3266cae764cc89b5fdace97f09240\.JPG|65318436b3cd4bd2a469735ae7b3f122\.png)/g, 'demo-assets/missing.svg').replaceAll('demo-assets/', baseUrl + 'demo-assets/') : Array.isArray(value) ? value.map(restore) : value && typeof value === 'object' ? Object.fromEntries(Object.entries(value).map(([k, v]) => [k, restore(v)])) : value
    const data = restore(structuredClone(seed))
    for (const [target, source] of [[users, data.sysUser], [admins, data.sysAdmin], [types, data.type], [goods, data.goods], [orders, data.orders], [addresses, data.address], [notices, data.notice], [banners, data.banner], [chats, data.chat]]) target.splice(0, target.length, ...source)
    for (const u of users) Object.assign(u, { role: 'ROLE_USER', token: 'demo-only-not-a-real-token', isAuth: Boolean(u.isAuth) })
    for (const t of types) t.status = Boolean(t.status)
    for (const u of admins) Object.assign(u, { role: 'ROLE_ADMIN', token: 'demo-only-not-a-real-token' })
    account = users[0]
    collects = data.collect.filter(c => c.userId === account.id).map(c => c.itemId)
    for (const g of goods) Object.assign(g, { typeName: types.find(t => t.id === g.typeId)?.name, school: users.find(u => u.id === g.userId)?.school, isAuth: users.find(u => u.id === g.userId)?.isAuth })
    for (const o of orders) Object.assign(o, { itemName: goods.find(g => g.id === o.itemId)?.name, itemImg: goods.find(g => g.id === o.itemId)?.img })
  }
  const success = data => ({ code: '200', msg: '模拟操作成功', data: structuredClone(data) })
  const failure = msg => ({ code: '400', msg: msg || '此操作未在演示版开放', data: null })
  const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

  return async function api(method, url, body = {}, params = {}) {
    body ||= {}
    params ||= {}
    const path = url.split('?')[0]
    if (path === '/web/login') {
      const candidates = body.role === 'ROLE_ADMIN' ? admins : users
      const selected = ['demo', 'admin'].includes(body.username) ? candidates[0] : candidates.find(u => u.username === body.username)
      if (body.password !== 'demo' || !selected) return failure('演示账号：demo 或 admin；密码：demo')
      account = selected
      if (seed) collects = seed.collect.filter(c => c.userId === account.id).map(c => c.itemId)
      return success(account)
    }
    if (path === '/web/userInfo') return success(account)
    if (['/web/register', '/web/password', '/web/auth'].includes(path)) return failure('注册、密码修改和邮箱认证仅展示界面，不操作真实账号')
    if (path.startsWith('/ai/')) return failure('在线演示不调用真实 AI 服务，请手动填写商品描述和价格')
    if (path === '/shipment') return success(['自提', '快递'])
    if (path === '/echarts/count') return success(types.map(t => ({ name: t.name, value: goods.filter(g => g.typeId === t.id).length })))
    if (path === '/collect' && method === 'post') {
      const id = Number(body.itemId)
      if (!goods.some(g => g.id === id)) return failure('商品不存在')
      collects = collects.includes(id) ? collects.filter(i => i !== id) : [...collects, id]
      return success(null)
    }
    if (path.startsWith('/collect/') && method === 'delete') { collects = collects.filter(id => id !== Number(path.split('/').at(-1))); return success(null) }
    if (path === '/chat/user') return success(users.filter(u => u.id !== account.id).map(u => ({ ...u, count: 1, online: false })))
    if (path.startsWith('/chat/user/')) return success({ ...users.find(u => u.id === Number(path.split('/').at(-1))), count: 0 })
    if (path === '/chat/message') return success(chats.filter(c => (c.fromUserId === Number(params.fromUserId) && c.toUserId === Number(params.toUserId)) || (c.fromUserId === Number(params.toUserId) && c.toUserId === Number(params.fromUserId))))
    if (path === '/chat/clear') return success(null)
    if (path === '/chat' && method === 'post') { chats.push({ ...body, id: nextId++ }); return success(null) }
    const action = path.match(/^\/orders\/(pay|cancel|receipt)\/(\d+)$/)
    if (action || path === '/orders/shipment') {
      const order = orders.find(o => o.id === Number(action ? action[2] : body.id))
      if (!order) return failure('订单不存在')
      order.status = action ? ({ pay: '待发货', cancel: '交易关闭', receipt: '交易完成' })[action[1]] : '待收货'
      if (!action) Object.assign(order, body)
      return success(order)
    }
    if (path === '/orders' && method === 'post' && !body.id) {
      const item = goods.find(g => g.id === Number(body.itemId))
      if (!item || item.status !== '已上架') return failure('商品不可购买')
      const address = body.addressId ? addresses.find(a => a.id === Number(body.addressId) && a.userId === account.id) : addresses.find(a => a.userId === account.id)
      if (body.addressId && !address) return failure('收货地址不存在')
      const order = { ...address, ...body, id: nextId++, no: 'DEMO-' + nextId, itemName: item.name, itemImg: item.img, fromId: item.userId, toId: account.id, price: item.price, status: '待支付', time: now(), shipment: item.shipment }
      orders.push(order)
      return success(order)
    }
    const collection = path.split('/')[1]
    const tables = { goods, orders, user: users, admin: admins, type: types, notice: notices, banner: banners, address: addresses }
    const table = tables[collection]
    if (!table) return failure()
    if (method === 'delete' || path.endsWith('/del/batch')) {
      const ids = method === 'delete' ? [Number(path.split('/').at(-1))] : body.map(Number)
      for (let i = table.length - 1; i >= 0; i--) if (ids.includes(table[i].id)) table.splice(i, 1)
      return success(null)
    }
    if (method === 'post') {
      const item = table.find(row => row.id === Number(body.id))
      if (item) Object.assign(item, body)
      else table.unshift({ ...body, id: nextId++, userId: account.id, time: now(), img: body.img || illustration('新发布商品') })
      return success(item || table[0])
    }
    if (method !== 'get') return failure()
    if (/^\/(goods|user|admin)\/\d+$/.test(path)) {
      const item = table.find(row => row.id === Number(path.split('/').at(-1)))
      return item ? success({ ...item, isCollected: collects.includes(item.id) }) : failure('记录不存在')
    }
    let rows = [...table]
    if (collection === 'address' && account.role !== 'ROLE_ADMIN') rows = rows.filter(a => a.userId === account.id)
    if (path === '/goods/front' || path === '/goods/front/page') rows = rows.filter(g => g.status === '已上架')
    if (path === '/goods/collect/page') rows = rows.filter(g => collects.includes(g.id)).map(g => ({ ...g, collectId: g.id }))
    if (/\/(goods|orders)\/user\//.test(path)) rows = rows.filter(g => (g.userId ?? g.fromId) === Number(path.split('/').at(-1)))
    if (path === '/orders/front/page') rows = rows.filter(o => (params.flag === '我卖出的' ? o.fromId : o.toId) === account.id)
    if (params.keyword?.trim()) rows = rows.filter(g => String(g.name || g.itemName || '').includes(params.keyword.trim()))
    if (Number(params.typeId)) rows = rows.filter(g => g.typeId === Number(params.typeId))
    if (params.country) rows = rows.filter(g => g.place?.startsWith(params.country))
    if (params.status && params.status !== '全部') rows = rows.filter(g => g.status === params.status)
    if (params.sortBy === 'price') rows.sort((a, b) => a.price - b.price)
    if (params.sortBy === 'new') rows.reverse()
    if (path.endsWith('/page')) {
      if (Number(params.pageSize) === -1) return success({ records: rows, total: rows.length })
      const pageNum = Math.max(1, Number(params.pageNum) || 1), pageSize = Math.max(1, Number(params.pageSize) || 10)
      return success({ records: rows.slice((pageNum - 1) * pageSize, pageNum * pageSize), total: rows.length })
    }
    return success(rows)
  }
}
