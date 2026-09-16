import test from 'node:test'
import assert from 'node:assert/strict'
const module = await import('./security-audit.mjs').catch(() => ({}))

test('上传审计拦截私有文件、密钥、个人信息及本地配置中的秘密', () => {
  assert.equal(typeof module.auditEntries, 'function', '上传审计尚未实现')
  const audit = module.auditEntries
  assert.ok(audit([{ path: 'sql/secondhand.sql', content: '' }]).length)
  assert.ok(audit([{ path: 'files/photo.jpg', content: '' }]).length)
  assert.ok(audit([{ path: 'vue/.env.production', content: '' }]).length)
  assert.ok(audit([{ path: 'src/main/resources/application-local.properties', content: '' }]).length)
  assert.ok(audit([{ path: 'src/test.txt', content: 'api-key: sk-' + 'x'.repeat(32) }]).length)
  assert.ok(audit([{ path: 'src/test.txt', content: 'someone' + '@' + 'personal.test' }]).length)
  assert.ok(audit([{ path: 'src/test.txt', content: 'private-password-value' }], ['private-password-value']).length)
  assert.equal(audit([{ path: 'README.md', content: '虚拟邮箱 demo@example.invalid' }]).length, 0)
})
