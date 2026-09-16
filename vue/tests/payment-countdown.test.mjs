import test from 'node:test'
import assert from 'node:assert/strict'
import { execFileSync } from 'node:child_process'
const source = `import { createDemoApi } from ${JSON.stringify(new URL('../src/demo/api.js', import.meta.url).href)};
const api = createDemoApi();
const order = (await api('post', '/orders', { itemId: 1, addressId: 1 })).data;
const start = new Date(order.time.replace(/-/g, '/')).getTime();
console.log(JSON.stringify({ remaining: start + 300000 - Date.now(), status: order.status }));`

for (const timezone of ['Asia/Hong_Kong', 'America/New_York', 'UTC']) {
  test(`取消支付保留的待支付订单在 ${timezone} 有完整五分钟支付期限`, () => {
    const result = JSON.parse(execFileSync(process.execPath, ['--input-type=module', '-e', source], { env: { ...process.env, TZ: timezone }, encoding: 'utf8' }))
    assert.equal(result.status, '待支付')
    assert.ok(result.remaining > 298000 && result.remaining <= 300000, `剩余时间异常: ${result.remaining}ms`)
  })
}
