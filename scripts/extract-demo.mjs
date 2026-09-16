import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const camel = s => s.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
const tables = ['address', 'banner', 'chat', 'collect', 'goods', 'notice', 'orders', 'sys_admin', 'sys_user', 'type']
// Only explicitly approved fictional business tables; never export configuration or passwords.
export function extractDemoData(sql) {
  const result = Object.fromEntries(tables.map(t => [camel(t), []]))
  const statements = []
  let start = 0, quoted = false
  for (let i = 0; i < sql.length; i++) {
    if (quoted && sql[i] === '\\') { i++; continue }
    if (sql[i] === "'") quoted = !quoted
    if (!quoted && sql[i] === ';') { statements.push(sql.slice(start, i)); start = i + 1 }
  }
  for (const statement of statements) {
    const match = statement.match(/INSERT INTO `([^`]+)`\s*\(([^)]+)\)\s*VALUES\s*([\s\S]*)/i)
    if (!match || !tables.includes(match[1])) continue
    const columns = [...match[2].matchAll(/`([^`]+)`/g)].map(m => camel(m[1]))
    const input = match[3]
    let i = 0
    while (i < input.length) {
      if (input[i++] !== '(') continue
      const values = []
      while (i < input.length) {
        while (/\s/.test(input[i] || '') && i < input.length) i++
        let value = ''
        if (input[i] === "'") {
          i++
          while (i < input.length) {
            const c = input[i++]
            if (c === '\\') { const escaped = input[i++]; value += ({ n: '\n', r: '\r', t: '\t', b: '\b', 0: '\0' })[escaped] ?? escaped }
            else if (c === "'") { if (input[i] === "'") { value += "'"; i++ } else break }
            else value += c
          }
          value = value.replace(/https?:\/\/(?:127\.0\.0\.1|localhost):9090\/web\/download\//g, 'demo-assets/')
        } else {
          const begin = i
          while (i < input.length && ![',', ')'].includes(input[i])) i++
          const raw = input.slice(begin, i).trim()
          value = /^null$/i.test(raw) ? null : Number(raw)
        }
        values.push(value)
        while (i < input.length && /\s/.test(input[i])) i++
        if (input[i++] === ')') break
      }
      if (values.length !== columns.length) throw new Error(`Column mismatch: ${match[1]}`)
      result[camel(match[1])].push(Object.fromEntries(columns.flatMap((key, index) => /password|secret|token|apiKey/i.test(key) ? [] : [[key, values[index]]])))
    }
  }
  return result
}

if (process.argv[1] === fileURLToPath(import.meta.url)) process.stdout.write(JSON.stringify(extractDemoData(readFileSync(process.argv[2], 'utf8')), null, 2) + '\n')
