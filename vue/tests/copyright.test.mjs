import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { parse } from '@vue/compiler-sfc'
import { compile } from '@vue/compiler-dom'
import * as Vue from 'vue'
import { renderToString } from '@vue/server-renderer'

test('全站入口在页面内容之后显示作者署名，不显示面试演示', async () => {
  const source = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
  const { descriptor } = parse(source)
  const { code } = compile(descriptor.template.content, { mode: 'function', prefixIdentifiers: true })
  const render = new Function('Vue', code)(Vue)
  const app = Vue.createSSRApp({ render, setup: () => ({ isDemo: false }) })
  // 隔离依赖后端的路由页面，测试真实的全站入口渲染。
  app.component('RouterView', { render: () => Vue.h('main', '页面内容') })
  const html = await renderToString(app)
  assert.match(html, /<footer\b[^>]*>\s*© 2026 良暨宣 · Globuy\s*<\/footer>/)
  assert.ok(html.indexOf('</main>') < html.indexOf('<footer'))
  assert.ok(!html.includes('面试演示'))
})
