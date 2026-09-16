import test from 'node:test'
import assert from 'node:assert/strict'
const module = await import('./extract-demo.mjs').catch(() => ({}))

test('从 SQL 提取虚构展示记录，保留文本和价格，去除密码，重写图片路径', () => {
  assert.equal(typeof module.extractDemoData, 'function', '数据提取尚未实现')
  const sql = "INSERT INTO `goods` (`id`, `name`, `price`, `content`, `img`, `user_id`) VALUES (7, '原商品', 12.50, '<p>描述;换行\\n引号\\\'保留</p>', 'http://127.0.0.1:9090/web/download/商品.jpg', 2);\nINSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`) VALUES (2, 'test', 'private-secret', '测试用户');"
  const data = module.extractDemoData(sql)
  assert.equal(data.goods[0].name, '原商品')
  assert.equal(data.goods[0].price, 12.5)
  assert.equal(data.goods[0].content, "<p>描述;换行\n引号'保留</p>")
  assert.equal(data.goods[0].userId, 2)
  assert.equal(data.goods[0].img, 'demo-assets/商品.jpg')
  assert.equal(data.sysUser[0].nickname, '测试用户')
  assert.ok(!JSON.stringify(data).includes('private-secret'))
  assert.ok(!Object.hasOwn(data.sysUser[0], 'password'))
})
