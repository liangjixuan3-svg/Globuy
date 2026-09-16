import { execFileSync } from 'node:child_process'
import { readFileSync, existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

export function auditEntries(entries, secrets = []) {
  const findings = []
  for (const { path, content } of entries) {
    const fail = reason => findings.push({ path, reason })
    const approvedFiction = path === 'vue/src/demo/original-data.json'
    const approvedImage = /^vue\/public\/demo-assets\/[^/]+\.(?:png|jpe?g|webp|gif)$/i.test(path)
    if (approvedFiction && /"(?:password|secret|token|apiKey)"\s*:/i.test(content)) fail('forbidden credential field')
    if (/^(files\/|target\/|\.idea\/)|(^|\/)(node_modules|dist)(\/|$)|(^|\/)\.env(?:\.|$)|^sql\/(?!schema\.sql$)|^src\/main\/resources\/application(?:-local)?\.(yaml|yml|properties)$/.test(path)) fail('private path')
    if (/(?:sk-[A-Za-z0-9_-]{20,}|gh[pousr]_[A-Za-z0-9]{20,}|AKIA[A-Z0-9]{16}|-----BEGIN [A-Z ]*PRIVATE KEY-----)/.test(content)) fail('credential pattern')
    const emails = content.match(/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/g) || []
    if (!approvedFiction && !approvedImage && emails.some(e => !e.endsWith('@example.invalid') && e !== 'example@usc.edu' && !e.endsWith('@users.noreply.github.com'))) fail('non-demo email')
    if (!approvedFiction && !approvedImage && /(?<![\d.])1[3-9]\d{9}(?![\d.])/.test(content)) fail('possible phone number')
    if (secrets.some(secret => secret.length >= 6 && content.includes(secret))) fail('matches private local value')
    if (path === 'sql/schema.sql' && /\bINSERT\s+INTO\b|\bREPLACE\s+INTO\b/i.test(content)) fail('schema contains records')
  }
  return findings
}

if (process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1]) {
  const revision = process.argv[2]
  const paths = execFileSync('git', revision ? ['ls-tree', '-r', '--name-only', '-z', revision] : ['ls-files', '-z'], { encoding: 'utf8' }).split('\0').filter(Boolean)
  const entries = paths.map(path => ({ path, content: execFileSync('git', ['show', revision ? `${revision}:${path}` : `:${path}`], { maxBuffer: 20 * 1024 * 1024 }).toString('utf8') }))
  const localPath = 'src/main/resources/application.yaml'
  const secrets = existsSync(localPath) ? [...readFileSync(localPath, 'utf8').matchAll(/^\s*(?:password|api-key|secret|token):\s*(.*?)\s*$/gm)].map(m => m[1].replace(/^['"]|['"]$/g, '')).filter(s => s !== 'your-api-key-here') : []
  const findings = auditEntries(entries, secrets)
  if (findings.length) {
    for (const f of findings) console.error(`${f.path}: ${f.reason}`)
    process.exitCode = 1
  } else console.log(`PASS: ${paths.length} tracked files audited; private paths, credential patterns, contact data and available local secrets checked. No secret values printed.`)
}
